/**
 * AbstractConstrainable.java
 * 
 * Revision History:<br>
 * Dec 5, 2008 jbjohns - File created
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.Shape;
import srl.recognition.constraint.IConstrainable;


/**
 * Abstract class for constrainables that provides base functionality common to
 * all instances of {@link IConstrainable} (like parent shapes).
 * 
 * @author jbjohns
 */
public abstract class AbstractConstrainable implements IConstrainable {
	
	/**
	 * Logger for this class
	 */
	private static Logger log = LoggerFactory
	        .getLogger(AbstractConstrainable.class);
	
	/**
	 * The parent shape of this constrainable object
	 */
	private Shape m_parentShape;
	
	/**
	 * Construct with a null parent, if you don't want to specify one.
	 */
	public AbstractConstrainable() {
		this(null);
	}
	
	/**
	 * Base class constructor that sets the parent shape for you. This field is
	 * immutable after construction time. Parent shape may not be set to null.
	 * 
	 * @param parentShape
	 *            The shape that is the parent of this constsrainable object.
	 */
	public AbstractConstrainable(Shape parentShape) {
		setParentShape(parentShape);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstrainable#getParentShape()
	 */
	@Override
	public Shape getParentShape() {
		return m_parentShape;
	}
	

	/**
	 * Set the parent shape of this constrainable object.
	 * 
	 * @param parentShape
	 *            The parent shape of this constrainable object.
	 */
	private void setParentShape(Shape parentShape) {
		//log.warn("Setting parent shape to null");
		// if (parentShape == null) {
		// log.error("Trying to set parent shape to null");
		// throw new IllegalArgumentException(
		// "Parent shape is not allowed to be null");
		// }
		m_parentShape = parentShape;
	}
}
