/**
 * IShape.java
 * 
 * Revision History <br>
 * (5/23/08) bde - Created the interface <br>
 * (5/24/08) jbjohns - Templating corrections, get/set subshapes <br>
 * (5/29/08) bde - Added clone() to the interface. <br>
 * 2008/07/02 : jbjohns : cached values <br>
 * 2008/09/02 : jbjohns : Control points, get stroke/subshape by UUID <br>
 * 2008/09/04 : jbjohns : getConfidence() <br>
 * 2008/09/08 : jbjohns : get/setAttribute, Aliases instead of control points <br>
 * 2008/10/09 : jbjohns : Recognition time <br>
 * 
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
 *       nor the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */

package srl.core.sketch;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openawt.Color;
import org.openawt.Polygon;



/**
 * IShape interface. A shape is a group of strokes that have been recognized and
 * given some sort of semantic identifier. Shapes can be very simple (a one
 * stroke Line shape) or of arbitrary complexity (a Smiley Face shape with eye
 * subshapes and a Line for a mouth).
 * 
 * @author bde
 */
public interface IShape extends Comparable<IShape>, Cloneable {

	/**
	 * Add the given alias into this shape. Aliases are uniquely identified by
	 * name, and this alias will replace any existing aliases with the same
	 * name.
	 * 
	 * @param alias
	 *            the alias to set for this shape.
	 */
	public void addAlias(Alias alias);

	/**
	 * Add a stroke to the list of strokes. Strokes should be added in ascending
	 * temporal order to the shape. We don't enforce this, so it's your job to
	 * do book keeping. Temporal ordering of strokes means that the last points
	 * in the strokes have ascending timestamps. We don't care if strokes
	 * overlap some in time, just as long as the stroke that was completed last
	 * <p>
	 * This method updates cached values in an iterative, O(1) manner.
	 * 
	 * @param stroke
	 *            the stroke to add to the list of strokes.
	 */
	public void addStroke(IStroke stroke);

	/**
	 * Clone an IShape.
	 * 
	 * @return a new, deep copied IShape.
	 */
	public Object clone();

	/**
	 * Returns if a shape contains a stroke
	 * 
	 * @param stroke
	 *            The stroke to check for
	 * 
	 * @return boolean, stating if stroke is a member of the shape
	 */
	public boolean containsStroke(IStroke stroke);

	/**
	 * Returns if a shape or its subshapes contain a stroke
	 * 
	 * @param stroke
	 *            The stroke to check for
	 * 
	 * @return boolean, stating if stroke is a member of the shape
	 */
	public boolean containsStrokeRecursive(IStroke stroke);

	/**
	 * If you modify any strokes this shape holds, certain cached values may no
	 * longer be valid (such as a bounding box). Use this method to let the
	 * shape know it needs to recompute these cached values the next time they
	 * are requested.
	 */
	public void flagExternalUpdate();

	/**
	 * Get the alias with the given name. If an alias with the given name does
	 * not exist, this method will return null.
	 * 
	 * @param name
	 *            the name of the alias to get.
	 * @return the alias with the given name that's been set for this shape, or
	 *         null if there is no such named alias.
	 */
	public Alias getAlias(String name);

	/**
	 * Get all the aliases that have been set for this shape. There is no
	 * guaranteed ordering for the aliases. If no aliases have been set, this
	 * method will return an empty collection.
	 * <p>
	 * This method returns a
	 * {@link Collections#unmodifiableCollection(Collection)}, so you cannot
	 * modify its elements outside this class.
	 * 
	 * @return a collection of all the aliases set for this shape.
	 */
	public Collection<Alias> getAliases();

	/**
	 * Get the value of the attribute with the given key from this shape's
	 * attribute map. If no attribute with that key exists, returns null.
	 * 
	 * @param key
	 *            the key of the attribute to get.
	 * @return the String value for the attribute with the given key, or null if
	 *         there is no such attribute.
	 */
	public String getAttribute(String key);

	/**
	 * Get the attribute map for this shape
	 * 
	 * @return The map of attributes
	 */
	public Map<String, String> getAttributes();

	/**
	 * Get the bounding box for this shape. If no bounding box has been computed
	 * yet, or if {@link #flagExternalUpdate()} has been called, this method
	 * will loop over all strokes to get the bounding box that encompasses all
	 * strokes, possibly taking O(m*n) time (m strokes, n points each). Else, it
	 * just returns the iteratively computed bounding box that's updated with
	 * each call to {@link #addStroke(IStroke)}.
	 * 
	 * @return the bounding box.
	 */
	public BoundingBox getBoundingBox();

	/**
	 * Get the convex hull for this shape. If no convex hull has been computed
	 * yet, or if {@link #flagExternalUpdate()} has been called, this method
	 * will loop over all strokes to get the convex hull that encompasses all
	 * strokes, possibly taking O(n*log(n)) time (n points in shape).
	 * 
	 * @return the convex hull
	 */
	public Polygon getConvexHull();

	/**
	 * Get the confidence that this shape interpretation holds. If no confidence
	 * has been set, or is not used by the implementing class, this method may
	 * return null.
	 * 
	 * @return the confidence that the interpretation of this set of strokes is
	 *         correct, or null if no confidence has been set/is not used.
	 */
	public Double getConfidence();

	/**
	 * Returns a string description of the shape.
	 * 
	 * @return a String of the shape description.
	 */
	public String getDescription();

	/**
	 * Get the first stroke in the shape, using {@link List#get(int)} with an
	 * index of 0. Throws an exception if there are no strokes. You need to call
	 * {@link #flagExternalUpdate()} if you update the stroke.
	 * 
	 * @return the first stroke.
	 */
	public IStroke getFirstStroke();

	/**
	 * Get the ID of the IShape.
	 * 
	 * @return UUID of the IShape.
	 */
	public UUID getID();

	/**
	 * Return the label associated with the IShape.
	 * 
	 * @return label of this shape.
	 */
	public String getLabel();

	/**
	 * Return shape's label associated with the IShape. It is far too annoying
	 * to re-type shape.getAttribute("shape label text") every time and far too
	 * likely there will be a typo
	 * 
	 * @return whatever string is associated with this IShape
	 */
	public String getShapeLabelText();

	/**
	 * Return shape's label associated with the IShape. It is far too annoying
	 * to re-type shape.getAttribute("shape label location") every time and far
	 * too likely there will be a typo, plus you have to calculate the
	 * substrings and it is nasty
	 * 
	 * @return the location of whatever string is associated with this IShape
	 */
	public Point getShapeLabelLocation();

	/**
	 * Get the last stroke in the shape, using {@link List#get(int)} with an
	 * index of size-1. Throws an exception if there are no strokes. You need to
	 * call {@link #flagExternalUpdate()} if you update the stroke.
	 * 
	 * @return the last stroke.
	 */
	public IStroke getLastStroke();

	/**
	 * Get the time when this shape was recognized by the recognizer. This field
	 * is optional and if not set, will return null.
	 * 
	 * @return the time the shape was recognized.
	 */
	public Long getRecognitionTime();

	/**
	 * Returns the list of strokes in the shape, as well as all strokes in any
	 * subshapes.
	 * 
	 * @return the strokes used in this shape and all subshapes.
	 */
	public List<IStroke> getRecursiveStrokes();

	/**
	 * Returns the list of bottom level subshapes in the shape
	 * 
	 * @return all bottom-level subshapes
	 */
	public List<IShape> getRecursiveSubShapes();

	/**
	 * Returns the list of all shapes recursively contained by this shape and
	 * its children
	 * 
	 * @return
	 */
	public List<IShape> getAllRecursiveSubShapes();

	/**
	 * Returns the list of parent strokes in the shape, as well as all parent
	 * strokes in any subshapes down to a given {@code level}. A parent stroke
	 * is either a stroke with no substrokes, or it is found using the
	 * {@link Stroke#getParent()}.
	 * 
	 * @return the parent strokes used in this shape and all subshapes.
	 */
	public List<IStroke> getRecursiveParentStrokes();

	/**
	 * Returns the stroke in a shape, found by index in the shape's stroke list.
	 * If you modify the stroke, you need to {@link #flagExternalUpdate()} so
	 * this object knows it needs to update cached value.
	 * 
	 * @param index
	 *            the position of the stroke in the Shape's stroke list.
	 * @return a Stroke (IStroke).
	 * 
	 */
	public IStroke getStroke(int index);

	/**
	 * Get the stroke in the shape that has the given UUID. If there is no such
	 * stroke, return null.
	 * <p>
	 * See the note about the list of strokes at {@link #getStrokes()}.
	 * 
	 * @see UUID#equals(Object)
	 * @param strokeId
	 *            the UUID of the stroke you wish to get from this shape.
	 * @return the stroke that has the given UUID (using equals()), or null if
	 *         there is no such stroke in this shape.
	 */
	public IStroke getStroke(UUID strokeId);

	/**
	 * Gets the list of the IStroke for the IShape. If you modify any strokes in
	 * the list, you need to {@link #flagExternalUpdate()} so this object knows
	 * it needs to update cached values.
	 * <p>
	 * The list of strokes returned is only the list of strokes that are present
	 * at this level of recognition. That is, this list will NOT contain any
	 * strokes in the subshapes of this shape. To get these strokes, you'll have
	 * to traverse the subshape tree recursively and gather all the strokes.
	 * 
	 * @return a list of the strokes in this shape.
	 */
	public List<IStroke> getStrokes();

	/**
	 * Returns the subshape in a shape, found by index in the shape's subshape
	 * list. If you modify the subshape, you need to
	 * {@link #flagExternalUpdate()} so this object knows it needs to update
	 * cached value.
	 * 
	 * @param index
	 *            the position of the subshape in the Shape's stroke list.
	 * @return a Shape (IShape).
	 * 
	 */
	public IShape getSubShape(int index);

	/**
	 * Get the subshape from the list of subshapes that has the given UUID. If
	 * there is no such subshape, return null.
	 * 
	 * @param shapeId
	 *            the ID of the subshape you're looking for.
	 * @return the shape that has the given UUID (based on equals), else null if
	 *         there is no such subshape.
	 */
	public IShape getSubShape(UUID shapeId);

	/**
	 * Gets the list of subshapes in this shape.
	 * 
	 * @return the list of subshapes.
	 */
	public List<IShape> getSubShapes();

	/**
	 * Gets the creation time of the shape. This creation time is defined as the
	 * maximum time of the strokes (the stroke completed last). UPDATING THE
	 * STROKES OR POINTS THROUGH NON-APPEND METHODS CAN BREAK THIS
	 * FUNCTIONALITY. This value is cached and updated through append methods
	 * {@link #addStroke(IStroke)}. If you modify strokes manually with
	 * {@link #getStrokes()} or any variant of {@link #getStroke(int)}, you need
	 * to call {@link #flagExternalUpdate()} to force an update of this cached
	 * value.
	 * 
	 * @return the creation time of the shape.
	 */
	public long getTime();

	/**
	 * Does this shape have an attribute with the given key?
	 * 
	 * @param key
	 *            The key
	 * @return True if the attribute map has the key, false if not.
	 */
	public boolean hasAttribute(String key);

	/**
	 * Removes the attribute from the shape&#39;s list of attributes.
	 * 
	 * @param name
	 *            attribute to remove.
	 * @return value of the removed attribute if it existed; {@code null}
	 *         otherwise.
	 */
	public String removeAttribute(String name);

	/**
	 * Set the given attribute in the shape's attribute map with the given key.
	 * Replaces any prior value on key conflict.
	 * 
	 * @param key
	 *            the key of the attribute.
	 * @param value
	 *            the value of the attribute.
	 */
	public void setAttribute(String key, String value);

	/**
	 * Set the color of the stroke.
	 * 
	 * @param c
	 *            the Color to set the stroke for.
	 */
	public void setColor(Color c);

	/**
	 * Set the confidence.
	 * 
	 * @param confidence
	 *            Double confidence value for the shape.
	 */
	public void setConfidence(Double confidence);

	/**
	 * Sets the description of the shape.
	 * 
	 * @param description
	 *            A String describing the shape.
	 */
	public void setDescription(String description);

	/**
	 * Sets the label associated with the IShape.
	 * 
	 * @param label
	 *            string representing the label of the IShape.
	 */
	public void setLabel(String label);

	/**
	 * Sets the "shape label text" attribute
	 * 
	 * @param text
	 *            A string to be displayed next to the shape
	 */
	public void setShapeLabelText(String text);

	/**
	 * Sets the "shape label location" attribute
	 * 
	 * @param location
	 *            A point representing where to display the shape's user-defined
	 *            label
	 */
	public void setShapeLabelLocation(Point newPoint);

	/**
	 * Set the time when this shape was recognized by the recognizer. This field
	 * is optional and may be set to null if you're not using recognition time.
	 * 
	 * @param recTime
	 *            the time the shape was recognized.
	 */
	public void setRecognitionTime(Long recTime);

	/**
	 * Sets the List of IStroke for the IShape. Automatically calls
	 * {@link #flagExternalUpdate()} to invalidate cached values and force
	 * recalculation.
	 * 
	 * @param strokes
	 *            the list of strokes to set to the IShape.
	 */
	public void setStrokes(List<IStroke> strokes);

	/**
	 * Set the list of subshapes.
	 * 
	 * @param subShapes
	 *            the list of subshapes to set.
	 */
	public void setSubShapes(List<IShape> subShapes);

	/**
	 * Returns whether two shapes are equal by comparing their strokes and all
	 * the strokes of their SubShapes.
	 * 
	 * @param os
	 *            The other shape to compare to
	 * 
	 * @return True if the shapes are equal, false otherwise
	 */
	public boolean equalsByContent(IShape os);

}
