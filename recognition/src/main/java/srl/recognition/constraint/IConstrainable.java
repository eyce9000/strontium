/**
 * IConstrainable.java
 * 
 * Revision History:<br>
 * Sep 17, 2008 jbjohns - File created
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
package srl.recognition.constraint;


import srl.core.sketch.BoundingBox;
import srl.core.sketch.Shape;
import srl.recognition.constraint.constrainable.ConstrainablePoint;
import srl.recognition.constraint.constrainable.ConstrainableShape;


/**
 * This interface defines the contract for things that are constrainable. That
 * is, things that implement this interface can be fed into IConstraint
 * implementations as parameters.
 * 
 * @author jbjohns
 */
public interface IConstrainable {

	/**
	 * The type of shape that this constrainable thing is. This might be a
	 * "Point", "Line", "Rectangle", or any other type of thing that can be
	 * constrained. This method should never return null.
	 * 
	 * @return The type of shape that is constrainable
	 */
	public String getShapeType();

	/**
	 * Get the bounding box around this shape.
	 * 
	 * @return The bounding box around the shape.
	 */
	public BoundingBox getBoundingBox();

	/**
	 * Get the parent shape that this {@link IConstrainable} is created from.
	 * This provides contextual information that is not present in a simple
	 * instance of {@link IConstrainable}. For instance, in the case of
	 * {@link ConstrainablePoint}, the parent shape provides context about the
	 * magnitude and range of the coordinate space. This context is useful for
	 * performing scaling and other transforms on {@link IConstrainable}
	 * instances. It might be the case, as it is with {@link ConstrainableShape}
	 * , that the parent shape is actually the same thing as what's being
	 * constrained.
	 * 
	 * @return The Shape that is the parent (or "producer") of the object that's
	 *         being wrapped by the {@link IConstrainable} instance.
	 */
	public Shape getParentShape();
}
