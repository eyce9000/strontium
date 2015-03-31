/*******************************************************************************
 *  Revision History:<br>
 *  SRL Member - File created
 *
 *  <p>
 *  <pre>
 *  This work is released under the BSD License:
 *  (C) 2011 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Sketch Recognition Lab, Texas A&M University 
 *        nor the names of its contributors may be used to endorse or promote 
 *        products derived from this software without specific prior written 
 *        permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  </pre>
 *  
 *******************************************************************************/
package srl.core.sketch;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.openawt.Polygon;
import org.openawt.geom.AffineTransform;
import org.openawt.svg.Style;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Root(strict=false)
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="@type")
@JsonSubTypes(
{
    @JsonSubTypes.Type(value = Shape.class, name = "Shape"),
    @JsonSubTypes.Type(value = Stroke.class, name = "Stroke")
})
public abstract class SComponent implements Cloneable, Renderable, Serializable, Transformable {
	/**
	 * counter will be incremented by 0x10000 for each new SComponent that is
	 * created counter is used as the most significant bits of the UUID
	 * 
	 * initialized to 0x4000 (the version -- 4: randomly generated UUID) along
	 * with 3 bytes of randomness: Math.random()*0x1000 (0x0 - 0xFFF)
	 * 
	 * the randomness further reduces the chances of collision between multiple
	 * sketches created on multiple computers simultaneously
	 */
	public static long counter = 0x4000L | (long) (Math.random() * 0x1000);
	
	@Attribute(required = false)
	protected UUID id;
	
	protected transient BoundingBox boundingBox;
	protected transient Polygon convexHull;

	@ElementMap(entry="attr",key="key",required=false,attribute=true,inline=true)
	protected Map<String, String> attributes;

	@Attribute(required = false)
	private Style style = null;

	public SComponent() {
		id = nextID();
		boundingBox = null;
		convexHull = null;
		attributes = new HashMap<String, String>();

	}

	public SComponent(SComponent copyFrom) {
		id = copyFrom.id;
		boundingBox = copyFrom.boundingBox;
		convexHull = copyFrom.convexHull;
		if(copyFrom.style!=null)
			style = (Style)copyFrom.style.clone();

		attributes = new HashMap<String, String>();
		for (Map.Entry<String, String> entry : copyFrom.attributes
				.entrySet()) {
			attributes.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Checks if this SContainer has the given attribute
	 * 
	 * @param attr
	 *            the name of the attribute to check for
	 * @return true if this SContainer has the given attribute
	 */
	public boolean hasAttribute(String attr) {
		if(attributes!=null)
			return attributes.containsKey(attr);
		return false;
	}

	/**
	 * Set an attribute value. Will overwrite any value currently set for that
	 * attribute
	 * 
	 * @param attr
	 *            attribute name
	 * @param value
	 *            attribute value
	 * @return the old value of the attribute, or null if none was set
	 */
	public String setAttribute(String attr, String value) {
		return attributes.put(attr, value);
	}

	/**
	 * Get the value for the given attribute.
	 * 
	 * @param attr
	 *            name of the attribute
	 * @return the value of the given attribute, or null if that attribute is
	 *         not set.
	 */
	public String getAttribute(String attr) {
		if (!hasAttribute(attr))
			return null;
		return attributes.get(attr);
	}

	/**
	 * Remove the given attribute from this SContainer.
	 * 
	 * @param attr
	 *            the name of the attribute
	 * @return the value that was removed, or null if nothing was removed
	 */
	public String removeAttribute(String attr) {
		return attributes.remove(attr);
	}

	public void applyTransform(AffineTransform xform) {
		applyTransform(xform, new HashSet<Transformable>());
	}

	public void scale(double xfactor, double yfactor) {
		applyTransform(AffineTransform.getScaleInstance(xfactor, yfactor));
	}

	public void translate(double xincrement, double yincrement) {
		applyTransform(AffineTransform.getTranslateInstance(xincrement,
				yincrement));
	}

	public void rotate(double radians) {
		applyTransform(AffineTransform.getRotateInstance(radians));
	}

	public void rotate(double radians, double xcenter, double ycenter) {
		applyTransform(AffineTransform.getRotateInstance(radians, xcenter,
				ycenter));
	}

	public void flagExternalUpdate() {
		boundingBox = null;
		convexHull = null;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	protected abstract void calculateBBox();

	public BoundingBox getBoundingBox() {
		if (boundingBox == null)
			calculateBBox();
		return boundingBox;
	}

	public Polygon getConvexHull() {
		return convexHull;
	}

	public boolean equals(Object other) {
		if (other instanceof SComponent) {
			return id.equals(((SComponent) other).getId());
		}
		return false;
	}

	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public abstract SComponent clone();


	public static Comparator<SComponent> getXComparator() {
		return new Comparator<SComponent>() {

			@Override
			public int compare(SComponent arg0, SComponent arg1) {
				return Double.compare(arg0.getBoundingBox().getLeft(), arg1
						.getBoundingBox().getLeft());
			}

		};
	}

	public static Comparator<SComponent> getYComparator() {
		return new Comparator<SComponent>() {

			@Override
			public int compare(SComponent arg0, SComponent arg1) {
				return Double.compare(arg0.getBoundingBox().getTop(), arg1
						.getBoundingBox().getTop());
			}

		};
	}

	/**
	 * Get a copy of the attributes. Modifying what this returns won't change a
	 * thing in this component.
	 * 
	 * @return
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	public static final String NAME_ATTR_KEY = "name";

	public String getName() {
		return (hasAttribute(NAME_ATTR_KEY)) ? getAttribute(NAME_ATTR_KEY)
				: null;
	}

	public String setName(String name) {
		return setAttribute(NAME_ATTR_KEY, name);
	}

	public static final String DESCRIPTION_ATTR_KEY = "description";

	public String getDescription() {
		return (hasAttribute(DESCRIPTION_ATTR_KEY)) ? getAttribute(DESCRIPTION_ATTR_KEY)
				: null;
	}

	public String setDescription(String description) {
		return setAttribute(DESCRIPTION_ATTR_KEY, description);
	}

	/**
	 * Look deep into two components to check equality
	 * 
	 * @param other
	 * @return
	 */
	public abstract boolean equalsByContent(SComponent other);
	
	public static UUID nextID() {

		counter += 0x10000L;
		return new UUID(counter, System.nanoTime() | 0x8000000000000000L);
	}
	
	public Style getStyle(){
		return this.style;
	}
	public void setStyle(Style style){
		this.style = style;
	}
}
