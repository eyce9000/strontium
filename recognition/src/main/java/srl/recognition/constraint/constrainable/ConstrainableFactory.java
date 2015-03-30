/**
 * ConstrainableFactory.java
 * 
 * Revision History:<br>
 * Sep 23, 2008 jbjohns - File created
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

import srl.core.sketch.BoundingBox;
import srl.core.sketch.Shape;
import srl.recognition.constraint.IConstrainable;
import srl.recognition.constraint.domains.ComponentSubPart;


/**
 * This factory creates the correct IConstrainable object based on the type of
 * Shape, or other information, that is presented.
 * 
 * @author jbjohns
 */
public class ConstrainableFactory {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory
	        .getLogger(ConstrainableFactory.class);
	
	/**
	 * Shape type for lines.
	 * 
	 * @see ConstrainableLine
	 */
	public static final String LINE_SHAPE_TYPE = "Line";
	
	/**
	 * Shape type for points
	 * 
	 * @see ConstrainablePoint
	 */
	public static final String POINT_SHAPE_TYPE = "Point";
	
	
	/**
	 * Looks at {@link Shape#getLabel()} and decides what is the best
	 * {@link IConstrainable} implementation to wrap the shape in. This decision
	 * is based on the accepted shape types that are present in this class, and
	 * the {@link IConstrainable} implementations this factory is aware of.
	 * <p>
	 * If you pass in a null object, this method returns null back.
	 * 
	 * @param shape
	 *            The shape to wrap
	 * @return An {@link IConstrainable} implementation that wraps the
	 *         information in the shape the best, as determined by
	 *         {@link Shape#getLabel()}
	 */
	public static IConstrainable buildConstrainable(Shape shape) {
		if (shape == null) {
			return null;
		}
		
		IConstrainable constrainable = null;
		
		if (LINE_SHAPE_TYPE.equalsIgnoreCase(shape.getInterpretation().label)) {
			// line between the first and last points in the shape
			constrainable = new ConstrainableLine(shape.getFirstStroke()
			        .getFirstPoint(), shape.getLastStroke().getLastPoint(),
			        shape);
		}
		else if (POINT_SHAPE_TYPE.equalsIgnoreCase(shape.getInterpretation().label)) {
			// the first point in the shape
			constrainable = new ConstrainablePoint(shape.getFirstStroke()
			        .getFirstPoint(), shape);
		}
		else {
			// else just a plain old shape
			constrainable = new ConstrainableShape(shape);
		}
		
		log.debug("Built " + constrainable + " from " + shape + " with parent "
		          + constrainable.getParentShape());
		
		return constrainable;
	}
	

	/**
	 * Get the subpart of the given constrainable. If the given constrainable is
	 * a {@link ConstrainablePoint}, return the point itself. If the given
	 * constrainable is a {@link ConstrainableLine}, check to see if the
	 * specified subPart is a valid line reference point. If the sub-part still
	 * has not been found (not specifying a line's sub-part, or the
	 * constrainable is neither a point nor a line), then we check
	 * {@link BoundingBox} reference points.
	 * <p>
	 * If the sub-part for the constrainable cannot be found, or is not valid
	 * (specifying line sub-parts for non-line constrainables is NOT valid),
	 * throws an {@link IllegalArgumentException}.
	 * 
	 * @param constrainable
	 *            Constrainable to get the sub-part of
	 * @param subPart
	 *            Sub-part of the constrainable to get
	 * @return A constrainable that is the sub-part of the given constrainable
	 * @throws IllegalArgumentException
	 *             If the sub-part is not valid for the type of constrainable
	 *             (getting line sub-parts of a non-line constrainable).
	 */
	public static IConstrainable getConstrainableSubPart(
	        IConstrainable constrainable, ComponentSubPart subPart)
	        throws IllegalArgumentException {
		
		if (constrainable == null || subPart == null) {
			throw new IllegalArgumentException(
			        "Constrainable and/or sub-part may not be null");
		}
		
		IConstrainable ret = null;
		
		if (constrainable instanceof ConstrainablePoint
		    || subPart == ComponentSubPart.None) {
			ret = constrainable;
		}
		else if (constrainable instanceof ConstrainableLine) {
			ConstrainableLine line = (ConstrainableLine) constrainable;
			BoundingBox bb = line.getBoundingBox();
			
			switch (subPart) {
				case End1:
					ret = line.getEnd1();
					break;
				case End2:
					ret = line.getEnd2();
					break;
				case BottomLeft:
					ret = new ConstrainablePoint(bb.getBottomLeftPoint(), line
					        .getParentShape());
					break;
				case BottomRight:
					ret = new ConstrainablePoint(bb.getBottomRightPoint(), line
					        .getParentShape());
					break;
				case BottomCenter:
					ret = new ConstrainablePoint(bb.getBottomCenterPoint(),
					        line.getParentShape());
					break;
				case BottomMostEnd:
					ret = line.getBottomMostEnd();
					break;
				case LeftMostEnd:
					ret = line.getLeftMostEnd();
					break;
				case RightMostEnd:
					ret = line.getRightMostEnd();
					break;
				case TopLeft:
					ret = new ConstrainablePoint(bb.getTopLeftPoint(), line
					        .getParentShape());
					break;
				case TopRight:
					ret = new ConstrainablePoint(bb.getTopRightPoint(), line
					        .getParentShape());
					break;
				case TopCenter:
					ret = new ConstrainablePoint(bb.getTopCenterPoint(), line
					        .getParentShape());
					break;
				case TopMostEnd:
					ret = line.getTopMostEnd();
					break;
				case CenterLeft:
					ret = new ConstrainablePoint(bb.getCenterLeftPoint(), line
					        .getParentShape());
					break;
				case CenterRight:
					ret = new ConstrainablePoint(bb.getCenterRightPoint(), line
					        .getParentShape());
					break;
				case Center:
					ret = new ConstrainablePoint(bb.getCenterPoint(), line
					        .getParentShape());
					break;
				default:
					ret = null;
					break;
			}
		}
		
		if (ret == null) {
			BoundingBox bb = constrainable.getBoundingBox();
			Shape parent = constrainable.getParentShape();
			
			switch (subPart) {
				case BottomMostEnd:
				case BottomCenter:
					ret = new ConstrainablePoint(bb.getBottomCenterPoint(),
					        parent);
					break;
				case End1:
				case BottomLeft:
					ret = new ConstrainablePoint(bb.getBottomLeftPoint(),
					        parent);
					break;
				case BottomRight:
					ret = new ConstrainablePoint(bb.getBottomRightPoint(),
					        parent);
					break;
				case Center:
					ret = new ConstrainablePoint(bb.getCenterPoint(), parent);
					break;
				case LeftMostEnd:
				case CenterLeft:
					ret = new ConstrainablePoint(bb.getCenterLeftPoint(),
					        parent);
					break;
				case RightMostEnd:
				case CenterRight:
					ret = new ConstrainablePoint(bb.getCenterRightPoint(),
					        parent);
					break;
				case TopMostEnd:
				case TopCenter:
					ret = new ConstrainablePoint(bb.getTopCenterPoint(), parent);
					break;
				case TopLeft:
					ret = new ConstrainablePoint(bb.getTopLeftPoint(), parent);
					break;
				case End2:
				case TopRight:
					ret = new ConstrainablePoint(bb.getTopRightPoint(), parent);
					break;
				default:
					ret = null;
					break;
			}
		}
		
		if (ret == null) {
			throw new IllegalArgumentException("Invalid sub-part (" + subPart
			                                   + ") for constrainable ("
			                                   + constrainable.getShapeType()
			                                   + ") ");
		}
		
		return ret;
	}
}
