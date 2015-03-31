/**
 * IntersectsConstraint.java
 * 
 * Revision History:<br>
 * Jul 16, 2008 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 *     This work is released under the BSD License:
 *     (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 *     All rights reserved.
 * 
 *     Redistribution and use in source and binary forms, with or without
 *     modification, are permitted provided that the following conditions are met:
 *         * Redistributions of source code must retain the above copyright
 *           notice, this list of conditions and the following disclaimer.
 *         * Redistributions in binary form must reproduce the above copyright
 *           notice, this list of conditions and the following disclaimer in the
 *           documentation and/or other materials provided with the distribution.
 *         * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University
 *           nor the names of its contributors may be used to endorse or promote
 *           products derived from this software without specific prior written
 *           permission.
 * 
 *     THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *     EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *     WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *     DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 *     DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *     (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *     LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *     ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *     (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *     SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package srl.recognition.constraint.confidence;

import java.util.ArrayList;
import java.util.List;

import org.openawt.geom.Line2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.BoundingBox;
import srl.core.sketch.Point;
import srl.math.linear.LineComputations;
import srl.recognition.constraint.IConstrainable;
import srl.recognition.constraint.constrainable.ConstrainableLine;
import srl.recognition.constraint.constrainable.ConstrainablePoint;
import srl.recognition.constraint.constrainable.ConstrainableShape;


/**
 * See if two shapes intersect. If two points, this is just coincident. If point
 * and line, sees if the point lies on that line. If point and shape, see if the
 * point is in the shape's bounding box. If two lines, do the line segments
 * physically intersect each other. If a line and shape, does the line enter the
 * shape's bounding box. If two shapes, do the bounding boxes overlap anywhere.
 * <p>
 * This constraint requires TWO {@link IConstrainable}
 * 
 * @author jbjohns
 */
public class IntersectsConstraint extends AbstractConfidenceConstraint {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public IntersectsConstraint newInstance() {
		return new IntersectsConstraint();
	}
	
	/**
	 * Logger
	 */
	private static Logger log = LoggerFactory
	        .getLogger(IntersectsConstraint.class);
	
	/**
	 * Name for this constraint
	 */
	public static final String NAME = "Intersects";
	
	/**
	 * Default description for this constraint
	 */
	public static final String DESCRIPTION = "This constraint holds if the two shapes intersect one another";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 2;
	
	/**
	 * Distance for how close things have to be before they intersect
	 */
	public static final double DEFAULT_THRESHOLD = 0.1;
	
	/**
	 * To use if we're given two points
	 */
	protected CoincidentConstraint m_coincident = new CoincidentConstraint();
	
	
	/**
	 * Construct the constraint with the {@link #DEFAULT_THRESHOLD}
	 */
	public IntersectsConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	/**
	 * Construct the constraint with the given threshold
	 * 
	 * @param threshold
	 *            The threshold to use
	 */
	public IntersectsConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#solve()
	 */
	public double solve() {
		// two shapes
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("Intersects requires " + getNumRequiredParameters()
			          + " shapes");
			return 0;
		}
		
		IConstrainable shape1 = getParameters().get(0);
		IConstrainable shape2 = getParameters().get(1);
		
		return solve(shape1, shape2);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.ladder.recognition.constraint.confidence.AbstractConstraint#
	 * multiplyThreshold(double)
	 */
	@Override
	public void multiplyThreshold(double thresholdMultiplier) {
		super.multiplyThreshold(thresholdMultiplier);
		m_coincident.multiplyThreshold(thresholdMultiplier);
	}
	

	/**
	 * See if the two shapes intersect. If two points, this is just coincident.
	 * If point and line, sees if the point lies on that line. If point and
	 * shape, see if the point is in the shape's bounding box. If two lines, do
	 * the line segments physically intersect each other. If a line and shape,
	 * does the line enter the shape's bounding box. If two shapes, do the
	 * bounding boxes overlap anywhere.
	 * 
	 * @param shape1
	 *            The first shape
	 * @param shape2
	 *            The second shape
	 * @return The confidence that these shapes intersect
	 */
	public double solve(IConstrainable shape1, IConstrainable shape2) {
		
		// Set the parameters
		List<IConstrainable> parms = new ArrayList<IConstrainable>();
		parms.add(shape1);
		parms.add(shape2);
		setParameters(parms);
		
		log.debug("Shape 1 type = " + shape1.getShapeType());
		log.debug("Shape 2 type = " + shape2.getShapeType());
		
		double conf = 0;
		
		// two points, coincident
		if (shape1 instanceof ConstrainablePoint
		    && shape2 instanceof ConstrainablePoint) {
			log.debug("shape 1 and 2 both points, coincident");
			conf = m_coincident.solve((ConstrainablePoint) shape1,
			        (ConstrainablePoint) shape2);
		}
		// point and line, intersection
		else if ((shape1 instanceof ConstrainablePoint && shape2 instanceof ConstrainableLine)
		         || (shape1 instanceof ConstrainableLine && shape2 instanceof ConstrainablePoint)) {
			
			ConstrainablePoint point;
			ConstrainableLine line;
			if (shape1 instanceof ConstrainablePoint) {
				point = (ConstrainablePoint) shape1;
				log.debug("Shape 1 is point " + point);
				line = (ConstrainableLine) shape2;
				log.debug("Shape 2 is line " + line);
			}
			else {
				point = (ConstrainablePoint) shape2;
				log.debug("Shape 2 is point " + point);
				line = (ConstrainableLine) shape1;
				log.debug("Shape 1 is line " + line);
			}
			
			// distance from point to line
			Line2D.Double line2d = line.getLine2DDouble();
			double dist = line2d.ptLineDist(point.getX(), point.getY());
			log.debug("distance from pt to line = " + dist);
			
			double lineLength = line.getPixelLength();
			log.debug("Line length = " + lineLength);
			
			double lineRelThreshold = getRelativeThreshold(lineLength);
			
			// confidence based on how close to the line we are
			conf = solveConfidence(dist, lineRelThreshold);
		} // end point and line
		// point and shape, point falls in bounding box
		else if ((shape1 instanceof ConstrainablePoint && shape2 instanceof ConstrainableShape)
		         || (shape2 instanceof ConstrainablePoint && shape1 instanceof ConstrainableShape)) {
			
			ConstrainablePoint point;
			BoundingBox bb;
			ConstrainablePoint bbCenter;
			if (shape1 instanceof ConstrainablePoint) {
				point = (ConstrainablePoint) shape1;
				log.debug("shape 1 is point = " + point);
				bb = shape2.getBoundingBox();
				log.debug("shape 2 is shape with bounding box = " + bb);
				bbCenter = new ConstrainablePoint(bb.getCenterPoint(), shape2
				        .getParentShape());
			}
			else {
				point = (ConstrainablePoint) shape2;
				log.debug("shape 2 is point = " + point);
				bb = shape1.getBoundingBox();
				log.debug("shape 1 is shape with bounding box = " + bb);
				bbCenter = new ConstrainablePoint(bb.getCenterPoint(), shape1
				        .getParentShape());
			}
			
			double bboxRelThreshold = getRelativeThreshold(bb
			        .getDiagonalLength());
			
			// shrink the bounding box by m_threshold in every direction
			// this is so if it's right on the original box's border, we're 50%
			// certain the point is inside.
			
			// Also, make sure the original box is big enough to be shrunk this
			// much. If not treat it as a point (from the center) and
			// return coincident confidence
			if (bb.height <= 2 * bboxRelThreshold
			    || bb.width <= 2 * bboxRelThreshold) {
				log
				        .debug("boundign box too small to shrink, coincident from center of box to point");
				return m_coincident.solve(point, bbCenter);
			}
			
			// big enough, so shrink
			BoundingBox shrunkBox = bb.contract(bboxRelThreshold);
			log.debug("Shrink bounding box by " + bboxRelThreshold
			          + " in each dir, results in = " + shrunkBox);
			// distance from new bounding box
			double dist = shrunkBox.distance(point.getX(), point.getY());
			log.debug("dist = " + dist);
			
			// confidence about distance
			conf = solveConfidence(dist, bboxRelThreshold);
		} // end point and shape
		// line and line, plain old intersection
		else if (shape1 instanceof ConstrainableLine
		         && shape2 instanceof ConstrainableLine) {
			Line2D.Double line1 = ((ConstrainableLine) shape1)
			        .getLine2DDouble();
			Line2D.Double line2 = ((ConstrainableLine) shape2)
			        .getLine2DDouble();
			
			// do lines intersect? If not, compute min distance between lines?
			double dist = 0;
			if (!line1.intersectsLine(line2)) {
				log.debug("Lines do not intersect");
				dist = LineComputations.minimumDistanceBetweenSegments(line1,
				        line2);
				// System.out.println(dist);
			}
			log.debug("dist between lines = " + dist);
			
			double len1 = line1.getP1().distance(line1.getP2());
			double len2 = line2.getP1().distance(line2.getP2());
			double lineRelThreshold = getRelativeThreshold((len1 + len2) / 2.0);
			
			// confidence using that distance of intersection
			conf = solveConfidence(dist, lineRelThreshold);
			log.debug("conf = " + conf);
		}
		// line and shape, does the line itself overlap the bounding box?
		else if ((shape1 instanceof ConstrainableLine && shape2 instanceof ConstrainableShape)
		         || (shape1 instanceof ConstrainableShape && shape2 instanceof ConstrainableLine)) {
			log.debug("one line and one shape");
			
			// which is line and which is bounding box?
			Line2D.Double line;
			BoundingBox bbox;
			if (shape1 instanceof ConstrainableLine) {
				log.debug("shape1 is the line, shape2 is the shape");
				line = ((ConstrainableLine) shape1).getLine2DDouble();
				bbox = shape2.getBoundingBox();
			}
			else {
				log.debug("shape1 is the shape, shape2 is the line");
				line = ((ConstrainableLine) shape2).getLine2DDouble();
				bbox = shape1.getBoundingBox();
			}
			double lineLength = line.getP1().distance(line.getP2());
			double bboxDiagonal = bbox.getDiagonalLength();
			
			// distance from the bounding box to the line
			double dist = 0;
			// relative threshold
			double relThreshold = getRelativeThreshold((lineLength + bboxDiagonal) / 2.0);
			
			// shrink the bounding box by the threshold if it's big enough, so
			// that we can compute confidence of things that are exactly on
			// the boundary
			if (bbox.height <= 2 * relThreshold
			    || bbox.width <= 2 * relThreshold) {
				log.debug("Bounding box is too small to shrink by "
				          + relThreshold + " per side, distance from center");
				// box is too small to shrink, so distance from bbox center
				Point boxCenter = bbox.getCenterPoint();
				dist = line.ptSegDist(boxCenter.getX(), boxCenter.getY());
				
				relThreshold = getRelativeThreshold(lineLength);
			}
			else {
				// box is big enough, shrink and get the distance
				log.debug("contract bbox by " + relThreshold);
				bbox = bbox.contract(relThreshold);
				// distance from bbox to line
				dist = bbox.distance(line);
			}
			
			log.debug("distance = " + dist);
			conf = solveConfidence(dist, relThreshold);
		}
		// shape and shape, bounding boxes overlap
		else {
			log.debug("both shapes are of shape type, bbox overlap?");
			
			// min distance between the bounding boxes
			double dist = 0;
			
			// half the threshold so that between the two boxes, they shrink
			// a total of one bbox
			BoundingBox bb1 = shape1.getBoundingBox();
			BoundingBox bb2 = shape2.getBoundingBox();
			double relThreshold = getRelativeThreshold((bb1.getDiagonalLength() / bb2
			        .getDiagonalLength()) / 2.0);
			double shrinkAmount = relThreshold / 2.0;
			
			// can they shrink (they're wide and tall enough to contract)?
			boolean bb1_shrink = (bb1.getWidth() >= 2 * relThreshold)
			                     && (bb1.getHeight() >= 2 * relThreshold);
			boolean bb2_shrink = (bb2.getWidth() >= 2 * relThreshold)
			                     && (bb2.getHeight() >= 2 * relThreshold);
			
			// go through the different cases to see how we should evaluate
			
			// neither is big enough to shrink. This means the shapes are both
			// very small and we treat both as single points
			if (bb1_shrink == false && bb2_shrink == false) {
				log.debug("both shapes are too small");
				Point pt1 = bb1.getCenterPoint();
				log.debug("pt1 = " + pt1);
				Point pt2 = bb2.getCenterPoint();
				log.debug("pt2 = " + pt2);
				
				ConstrainablePoint cp1 = new ConstrainablePoint(pt1, shape1
				        .getParentShape());
				ConstrainablePoint cp2 = new ConstrainablePoint(pt2, shape2
				        .getParentShape());
				double ptptConf = m_coincident.solve(cp1, cp2);
				log.debug("Shapes reduced to points: " + ptptConf);
				
				return ptptConf;
			}
			// next two cases, one shape is large and other is small, treat
			// small shape like a single point
			else if (bb1_shrink == true && bb2_shrink == false) {
				log.debug("shape 1 contract bbox");
				bb1 = bb1.contract(shrinkAmount);
				Point pt2 = bb2.getCenterPoint();
				log.debug("shape 2 too small, pt2 = " + pt2);
				dist = bb1.distance(pt2.getX(), pt2.getY());
				relThreshold = getRelativeThreshold(bb1.getDiagonalLength());
			}
			else if (bb1_shrink == false && bb2_shrink == true) {
				Point pt1 = bb1.getCenterPoint();
				log.debug("shape 1 too small, pt1 = " + pt1);
				bb2 = bb2.contract(shrinkAmount);
				log.debug("shape 2 contract bbox");
				dist = bb2.distance(pt1.getX(), pt1.getY());
				relThreshold = getRelativeThreshold(bb2.getDiagonalLength());
			}
			// both shapes large enough, contract bboxes
			else {
				// TODO this might not actually compute intersects, better way?
				// Intersection of convex hull?
				log.debug("shrink both");
				bb1 = bb1.contract(shrinkAmount);
				bb2 = bb2.contract(shrinkAmount);
				dist = bb1.distance(bb2);
			}
			
			log.debug("bounding box dists = " + dist);
			// confidence
			conf = solveConfidence(dist, relThreshold);
		}
		
		log.debug("conf = " + conf);
		return conf;
	}// end function
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.confidence.AbstractConfidenceConstraint#
	 *      isClearlyFalse(double)
	 */
	@Override
	public boolean isClearlyFalse(double value) {
		return value < 0.2;
	}
	

	/**
	 * Get a threshold value that is relative to thresh. Thresh is some
	 * measurement like the length of a line or the size of a parent shape. The
	 * scaling to get a value relative to thresh is taken from
	 * {@link #getThreshold()}. If this value is not in the range (0, 1], then
	 * a default threshold of 15.0 is returned.
	 * 
	 * @param thresh
	 *            Relative value to make a threshold from (this will be
	 *            something like the length of the line or average size of
	 *            parent shapes).
	 * @return Value that is some percentage (determined by
	 *         {@link #getThreshold()}) of thresh. If {@link #getThreshold()}
	 *         is outside the range (0, 1] this value returns a default of 15.0;
	 */
	private double getRelativeThreshold(double thresh) {
		// threshold that is relative to the length of the line
		double relThreshold = 15;
		if (getThreshold() <= 1 && getThreshold() > 0) {
			relThreshold = getThreshold() * thresh;
		}
		log.debug("relative threshold = " + relThreshold);
		
		return relThreshold;
	}
}