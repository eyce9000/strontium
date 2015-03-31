/**
 * ConstrainableShape.java
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
package srl.recognition.constraint.constrainable;

import srl.core.sketch.BoundingBox;
import srl.core.sketch.Shape;


/**
 * This implementation of IConstrainable is for complex shapes that are fed into
 * constraints. This class wraps an Shape object.
 * 
 * @author jbjohns
 */
public class ConstrainableShape extends AbstractConstrainable {
	
	/**
	 * The shape that is being constrained.
	 */
	private Shape m_shape;
	
	/**
	 * Construct a constrainable shape wrapper around the given shape
	 * 
	 * @param shape
	 *            The shape that we want to make constrainable
	 */
	public ConstrainableShape(Shape shape) {
		super(shape);
		try{
		setShape(shape);
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	

	/**
	 * Get the shape that is being constrained.
	 * 
	 * @return The shape that is being constrained, never null.
	 */
	public Shape getShape() {
		return m_shape;
	}
	

	/**
	 * Set the shape that you want to constrain. You are not allowed to set
	 * null, or will throw an {@link IllegalArgumentException}.
	 * 
	 * @param shape
	 *            The shape you want to constrain
	 */
	public void setShape(Shape shape) {
		if (shape != null) {
			m_shape = shape;
		}
		else {
			throw new IllegalArgumentException("The shape cannot be null");
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstrainable#getBoundingBox()
	 */
	public BoundingBox getBoundingBox() {
		return m_shape.getBoundingBox();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstrainable#getShapeType()
	 */
	public String getShapeType() {
		String shapeLabel = m_shape.getInterpretation().label;
		if (shapeLabel == null) {
			shapeLabel = "ConstrainableShape";
		}
		return shapeLabel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ConstrainableShape(" + m_shape + ")";
	}

	/* (non-Javadoc)
     * @see org.ladder.recognition.constraint.IConstrainable#getParentShape()
     */
    public Shape getParentShape() {
	    return m_shape;
    }
}
