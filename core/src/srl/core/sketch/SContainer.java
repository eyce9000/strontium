package srl.core.sketch;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.openawt.geom.AffineTransform;
import org.openawt.geom.GeneralPath;
import org.openawt.geom.PathIterator;
import org.openawt.svg.SVGGroup;
import org.openawt.svg.SVGShape;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;




@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="@type")
@JsonSubTypes(
{
    @JsonSubTypes.Type(value = Shape.class, name = "Shape"),
    @JsonSubTypes.Type(value = Sketch.class, name = "Sketch")
})
public abstract class SContainer extends SComponent implements
		Iterable<SComponent> ,TimePeriod, Comparable<TimePeriod> {
	
	protected List<SComponent> contents;
	
	private transient long timeStart = -1L;
	private transient long timeEnd = -1L;
	

	public SContainer() {
		super();

		contents = new ArrayList<SComponent>();
	}

	public SContainer(SContainer copyFrom) {
		super(copyFrom);

		contents = new ArrayList<SComponent>();

		for (SComponent comp : copyFrom.contents) {
			contents.add(comp.clone());
		}
	}

	public int size() {
		return contents.size();
	}

	@Override
	public SVGShape toSVGShape(){
		SVGGroup group = new SVGGroup();
		for (SComponent sub : contents) {
			SVGShape subshape = sub.toSVGShape();
			group.addShape(subshape);
		}
		group.setStyle(this.getStyle());
		return group;
	}
	
	@Override
	public void applyTransform(AffineTransform xform, Set<Transformable> xformed) {
		if (xformed.contains(this))
			return;

		xformed.add(this);
		for (SComponent comp : contents)
			comp.applyTransform(xform, xformed);
	}

	/**
	 * Add a subcomponent to this container.
	 * 
	 * @param subcomponent
	 */
	public void add(SComponent subcomponent) {
		contents.add(subcomponent);
	}

	/**
	 * Add a subcomponent to this container at the specified index.
	 * 
	 * @param index
	 * @param subcomponent
	 */
	public void add(int index, SComponent subcomponent) {
		contents.add(index, subcomponent);
	}

	/**
	 * Add a bunch of subcomponents to this container.
	 * 
	 * @param subcomponents
	 */
	public void addAll(Collection<? extends SComponent> subcomponents) {
		contents.addAll(subcomponents);
	}
	
	/**
	 * Remove a subcomponent from this container.
	 * 
	 * @param subcomponent
	 *            subcomponent to remove
	 * @return true if something was removed
	 */
	public boolean remove(SComponent subcomponent) {
		return contents.remove(subcomponent);
	}

	/**
	 * Remove the ith Component from this SContainer
	 * 
	 * @param i
	 * @return the value removed
	 */
	public SComponent remove(int i) {
		return contents.remove(i);
	}

	/**
	 * Remove a component by its id
	 * @param id the id of the subcomponent to remove
	 * @return the removed subcomponent. If the container had no subcomponent with that id, the return value is null.
	 */
	public synchronized SComponent remove(UUID id){
		SComponent comp = get(id);
		if(comp!=null)
			remove(comp);
		return comp;
	}
	
	/**
	 * Remove a bunch of subcomponents from this SContainer.
	 * 
	 * @param subcomponents
	 * @return
	 */
	public boolean removeAll(Collection<? extends SComponent> subcomponents) {
		return contents.removeAll(subcomponents);
	}

	/**
	 * Find the subcomponents of a certain class.
	 * 
	 * @param clazz
	 * @return
	 */
	public List<? extends SComponent> getByClassExact(
			Class<? extends SComponent> clazz) {
		ArrayList<SComponent> res = new ArrayList<SComponent>();

		for (SComponent sub : contents)
			if (clazz.equals(sub.getClass()))
				res.add(sub);

		return res;
	}

	/**
	 * Find the subcomponents that are or extend a certain class.
	 * 
	 * @see getByClassExact
	 * @param clazz
	 * @return
	 */
	public List<? extends SComponent> getByClassAssignable(
			Class<? extends SComponent> clazz) {
		ArrayList<SComponent> res = new ArrayList<SComponent>();

		for (SComponent sub : contents)
			if (clazz.isInstance(sub))
				res.add(sub);

		return res;
	}

	/**
	 * Recursively descend through the container and its contents.
	 * 
	 * @param clazz
	 * @return
	 */
	public List<? extends SComponent> getRecursiveByClassExact(
			Class<? extends SComponent> clazz) {
		List<SComponent> res = new ArrayList<SComponent>();

		for (SComponent sub : contents) {
			if (clazz.equals(sub.getClass()))
				res.add(sub);
			if (sub instanceof SContainer)
				res.addAll(((SContainer) sub).getRecursiveByClassExact(clazz));
		}

		return res;
	}

	/**
	 * Recursively descend through the container and its contents.
	 * 
	 * @param clazz
	 * @return
	 */
	public List<? extends SComponent> getRecursiveByClassAssignable(
			Class<? extends SComponent> clazz) {
		List<SComponent> res = new ArrayList<SComponent>();

		for (SComponent sub : contents) {
			if (clazz.isInstance(sub))
				res.add(sub);
			if (sub instanceof SContainer)
				res.addAll(((SContainer) sub)
						.getRecursiveByClassAssignable(clazz));
		}

		return res;
	}
	
	public List<SComponent> getAllComponents(){
		return new ArrayList<SComponent>(contents);
	}
	
	/**
	 * Get a recursive list of all subcomponents;
	 * 
	 * @return
	 */
	public List<SComponent> getRecursiveSubcomponents() {
		List<SComponent> res = new ArrayList<SComponent>();
		for (SComponent sub : contents) {
			res.add(sub);
			if (sub instanceof SContainer)
				res.addAll(((SContainer) sub).getRecursiveSubcomponents());
		}
		return res;
	}

	/**
	 * Check if the given component is contained by this container or any
	 * container it contains.
	 * 
	 * @param component
	 * @return
	 */
	public boolean containsRecursive(SComponent component) {
		return getRecursiveSubcomponents().contains(component);
	}

	/**
	 * Recursively descend through the container and its contents.
	 * 
	 * @param clazz
	 * @return
	 */
	public List<? extends SComponent> getRecursiveLeavesByClassExact(
			Class<? extends SComponent> clazz) {
		List<SComponent> res = new ArrayList<SComponent>();

		for (SComponent sub : contents) {
			if (sub instanceof SContainer) {
				List<? extends SComponent> toAdd = ((SContainer) sub)
						.getRecursiveByClassExact(clazz);
				res.addAll(toAdd);

				if (toAdd.size() > 0)
					continue;
			}

			if (clazz.equals(sub.getClass())) {
				res.add(sub);
			}
		}

		return res;
	}

	/**
	 * Recursively descend through the container and its contents.
	 * 
	 * @param clazz
	 * @return
	 */
	public List<? extends SComponent> getRecursiveLeavesByClassAssignable(
			Class<? extends SComponent> clazz) {
		List<SComponent> res = new ArrayList<SComponent>();

		for (SComponent sub : contents) {
			if (sub instanceof SContainer) {
				List<? extends SComponent> toAdd = ((SContainer) sub)
						.getRecursiveLeavesByClassAssignable(clazz);
				res.addAll(toAdd);

				if (toAdd.size() > 0)
					continue; // if nothing was added, then check sub
			}

			if (clazz.isInstance(sub)) {
				res.add(sub);
			}
		}

		return res;
	}

	/**
	 * Get a recursive list of the strokes.
	 * 
	 * @return
	 */
	public List<Stroke> getRecursiveStrokes() {
		return (List<Stroke>) getRecursiveByClassAssignable(Stroke.class);
	}

	/**
	 * Get a recursive list of the parent strokes.
	 * 
	 * if stroke.parent == null, use stroke instead
	 * 
	 * @return
	 */
	public List<Stroke> getRecursiveParentStrokes() {
		ArrayList<Stroke> res = new ArrayList<Stroke>();

		for (Stroke s : getRecursiveStrokes())
			res.add((s.getParent() != null) ? s.getParent() : s);

		return res;
	}

	/**
	 * Get a recursive list of the leaf-shapes (ie shapes that don't contain
	 * more shapes)
	 * 
	 * @return
	 */
	public List<Shape> getRecursiveShapes() {
		ArrayList<Shape> res = new ArrayList<Shape>();

		for (SComponent s : getRecursiveLeavesByClassAssignable(Shape.class))
			res.add((Shape) s);

		return res;
	}

	/**
	 * Get a list of all points recursively contained
	 * 
	 * @return
	 */
	public List<Point> getPoints() {
		List<Point> res = new ArrayList<Point>();

		for (Stroke s : getRecursiveStrokes())
			res.addAll(s.getPoints());

		return res;
	}
	
	/**
	 * Get a list of the strokes in this SContainer
	 * 
	 * @return
	 */
	@ElementList(required=false,entry="stroke",inline=true)
	public List<Stroke> getStrokes() {
		ArrayList<Stroke> res = new ArrayList<Stroke>();

		for (SComponent s : getByClassAssignable(Stroke.class))
			res.add((Stroke) s);

		return res;
	}

	public int getNumStrokes() {
		int i = 0;

		for (SComponent s : getByClassAssignable(Stroke.class))
			++i;

		return i;
	}


	/**
	 * Set the list of strokes in this SContainer
	 * 
	 * @param strokes
	 */
	@ElementList(required=false,entry="stroke",inline=true)
	public void setStrokes(List<? extends Stroke> strokes) {
		removeAll(getByClassAssignable(Stroke.class));
		addAll(strokes);
	}

	/**
	 * Return the first stroke in this SContainer
	 * 
	 * @return
	 */
	public Stroke getFirstStroke() {
		List<Stroke> strokes = getStrokes();
		return (strokes == null || strokes.size() == 0) ? null : strokes.get(0);
	}

	/**
	 * Return the last stroke in this SContainer
	 * 
	 * @return
	 */
	public Stroke getLastStroke() {
		List<Stroke> strokes = getStrokes();
		return (strokes == null || strokes.size() == 0) ? null : strokes
				.get(strokes.size() - 1);
	}

	/**
	 * Get a list of the subshapes in this SContainer
	 * 
	 * @return
	 */
	@ElementList(required=false,entry="shape",inline=true)
	public List<Shape> getShapes() {
		ArrayList<Shape> res = new ArrayList<Shape>();

		for (SComponent s : getByClassAssignable(Shape.class))
			res.add((Shape) s);

		return res;
	}

	/**
	 * Set the list of subshapes in this SContainer
	 * 
	 * @param subshapes
	 */
	@ElementList(required=false,entry="shape",inline=true)
	public void setShapes(List<? extends Shape> subshapes) {
		removeAll(getByClassAssignable(Shape.class));
		addAll(subshapes);
	}

	/**
	 * Get the ith shape
	 * 
	 * @param i
	 */
	public Shape getShape(int i) {
		return getShapes().get(i);
	}

	/**
	 * get the ith stroke
	 * 
	 * @param i
	 * @return
	 */
	public Stroke getStroke(int i) {
		return getStrokes().get(i);
	}

	/**
	 * Find a component by ID
	 * 
	 * @param id
	 * @return
	 */
	public SComponent get(UUID id) {
		return get(id, false);
	}

	/**
	 * Find a component by ID
	 * 
	 * @param id
	 *            ID to search for
	 * @param searchRecursive
	 *            look inside other containers?
	 * @return
	 */
	public SComponent get(UUID id, boolean searchRecursive) {
		for (SComponent comp : contents) {
			if (comp.getId().equals(id))
				return comp;
			if (searchRecursive && comp instanceof SContainer) {
				SComponent maybe = ((SContainer) comp).get(id, true);
				if (maybe != null)
					return maybe;
			}
		}
		return null;
	}

	/**
	 * Get the ith component.
	 * 
	 * @param i
	 * @return
	 */
	public SComponent get(int i) {
		return contents.get(i);
	}

	/**
	 * Find a Stroke by ID
	 * 
	 * @param id
	 * @return
	 */
	public Stroke getStroke(UUID id) {
		for (SComponent comp : contents)
			if (comp instanceof Stroke && comp.getId().equals(id))
				return (Stroke) comp;

		return null;
	}

	/**
	 * Find a Shape by ID
	 * 
	 * @param id
	 * @return
	 */
	public Shape getShape(UUID id) {
		for (SComponent comp : contents)
			if (comp instanceof Shape && comp.getId().equals(id))
				return (Shape) comp;

		return null;
	}

	/**
	 * Get the number of components;
	 * 
	 * @return
	 */
	public int getComponentCount() {
		return contents.size();
	}


	/**
	 * Checks if this container contains a component
	 * 
	 * @param component
	 * @return
	 */
	public boolean contains(SComponent component) {
		for (SComponent sub : contents)
			if (sub.equals(component))
				return true;

		return false;
	}
	
	public boolean contains(UUID id, boolean recursive){
		for(SComponent sub : contents){
			if(sub.getId().equals(id))
				return true;
			if(recursive && sub instanceof SContainer)
				if(((SContainer) sub).contains(id, recursive))
					return true;
		}
		return false;
	}

	/**
	 * Checks if this container contains all of the given components
	 * 
	 * @param component
	 * @return
	 */
	public boolean containsAll(Collection<? extends SComponent> components) {
		for (SComponent component : components) {
			if (!contains(component))
				return false;
		}

		return true;
	}

	/**
	 * Checks if this container contains any of the given components
	 * 
	 * @param component
	 * @return
	 */
	public boolean containsAny(Collection<? extends SComponent> components) {
		for (SComponent component : components)
			if (contains(component))
				return true;

		return false;
	}

	/**
	 * Clear this container
	 */
	public void clear() {
		contents.clear();
		flagExternalUpdate();
	}

	/**
	 * Find all SContainers in this container that recursively contain the given
	 * component
	 * 
	 * @param comp
	 * @return
	 */
	public List<SContainer> getContainersForComponent(SComponent comp) {
		List<SContainer> res = new ArrayList<SContainer>();

		for (SComponent c : contents) {
			if (c instanceof SContainer
					&& ((SContainer) c).containsRecursive(comp))
				res.add((SContainer) c);
		}

		return res;
	}

	/**
	 * Find all SContainers in this container that recursively contain the given
	 * components
	 * 
	 * @param comp
	 * @return
	 */
	public Set<? extends SContainer> getContainersForComponents(
			Collection<? extends SComponent> comps) {
		Set<SContainer> res = new HashSet<SContainer>();

		for (SComponent c : contents) {
			boolean found = true;
			for (SComponent cc : comps) {
				if (!(c instanceof SContainer && ((SContainer) c)
						.containsRecursive(cc))) {
					found = false;
					break;
				}
			}
			if (found)
				res.add((SContainer) c);
		}

		return res;
	}

	@Override
	protected void calculateBBox() {
		double minX = Double.POSITIVE_INFINITY;
		double minY = minX;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = maxX;

		for (SComponent c : contents) {
			BoundingBox b = c.getBoundingBox();
			if (b.getLeft() < minX)
				minX = b.getLeft();
			if (b.getRight() > maxX)
				maxX = b.getRight();
			if (b.getBottom() < minY)
				minY = b.getBottom();
			if (b.getTop() > maxY)
				maxY = b.getTop();
		}

		boundingBox = new BoundingBox(minX, minY, maxX, maxY);
	}

	/**
	 * Check if two SContainers equal each other based on the contents
	 * 
	 * @param other
	 * @return
	 */
	public boolean equalsByContent(SComponent o) {
		if (this == o)
			return true;

		if (!getClass().isAssignableFrom(o.getClass()))
			return false;

		SContainer other = (SContainer) o;

		if (contents.size() != other.contents.size())
			return false;

		ArrayList<SComponent> otherCopy = new ArrayList<SComponent>(
				other.contents);

		for (SComponent comp : contents) {
			boolean found = false;
			Iterator<SComponent> it = otherCopy.iterator();
			while (it.hasNext()) {
				if (comp.equalsByContent(it.next())) {
					it.remove();
					found = true;
					break;
				}
			}

			if (!found)
				return false;
		}

		return true;
	}
	
	public Iterator<SComponent> iterator() {
		return contents.iterator();
	}

	private synchronized void rebuildTime(){
		timeStart = Long.MAX_VALUE;
		timeEnd = Long.MIN_VALUE;
		for(SComponent comp:this){
			if(comp instanceof TimePeriod){
				TimePeriod timed = (TimePeriod)comp;
				long otherTimeEnd,otherTimeStart;
				otherTimeEnd = timed.getTimeEnd();
				otherTimeStart = timed.getTimeEnd();
				timeStart = Math.min(timeStart, otherTimeStart);
				timeEnd = Math.max(timeEnd, otherTimeEnd);
			}
		}
	}
	@Override
	public long getTimeStart() {
		if(timeStart == -1)
			rebuildTime();
		return timeStart;
	}

	@Override
	public long getTimeEnd() {
		if(timeEnd == -1)
			rebuildTime();
		return timeEnd;
	}

	@Override
	public long getTimeLength() {
		return getTimeEnd() - getTimeStart();
	}
	

	public static Comparator<TimePeriod> getTimeComparator(){
		return timeComparator;
	}
	
	private static Comparator<TimePeriod> timeComparator = new Comparator<TimePeriod>()  {

		@Override
		public int compare(TimePeriod first, TimePeriod second) {

			if(first.getTimeStart() < second.getTimeStart())
				return -1;
			else if(first.getTimeStart() > second.getTimeStart())
				return 1;
			else
				return 0;
		}
		
	};
	
	public void clearSubStyles(){
		for(SComponent subcomp:this){
			subcomp.setStyle(null);
			if(subcomp instanceof SContainer){
				((SContainer) subcomp).clearSubStyles();
			}
		}
	}
}
