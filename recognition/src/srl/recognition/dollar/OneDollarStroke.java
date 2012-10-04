/**
 * OneDollarStroke.java
 * 
 * Revision History:<br>
 * November 20, 2008 bpaulson - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
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
package srl.recognition.dollar;

import java.util.ArrayList;
import java.util.List;


import srl.core.sketch.BoundingBox;
import srl.core.sketch.Point;
import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;
import srl.recognition.IRecognitionResult;
import srl.recognition.RecognitionResult;


/**
 * Implementation of the $1 recognizer Algorithm by: Jacob Wobbrock, Andrew
 * Wilson, Yang Li
 * 
 * @author bpaulson
 */
public class OneDollarStroke extends Stroke {
	
	/**
	 * Generated id
	 */
	private static final long serialVersionUID = -599543951750675305L;
	
	/**
	 * Resample size -- number of points in the stroke
	 */
	public static final int RESAMPLE_SIZE = 64;
	
	/**
	 * Size of the square for rescaling
	 */
	public static final int SQUARE_SIZE = 8;
	
	/**
	 * For rotation invariance, max amount to rotate to find a match
	 */
	public static final double MAX_DEGREE = Math.toRadians(45.0);
	
	/**
	 * For rotation invariance, how much we rotate at each step to find a match
	 */
	public static final double DELTA_DEGREE = Math.toRadians(2.0);
	
	/**
	 * Some parameter for rotating and getting the best match
	 */
	public static final double PHI = 0.5 * (-1.0 + Math.sqrt(5.0));
	
	/**
	 * How many results do we return in the n-best list?
	 */
	public static final int S_NUM_NBEST_RESULTS = 5;
	
	private Point centroid;
	
	/**
	 * Recognition result for this stroke, given a set of templates, if
	 * recognizing.
	 */
	private IRecognitionResult m_recognitionResults;
	

	/**
	 * Create a TEMPLATE one dollar stroke that can be used to recognize other
	 * strokes. Recognition results is set to null.
	 * 
	 * @param ts
	 *            The stroke to use as the template
	 * @param name
	 *            The name of the template, for classification purposes
	 */
	public OneDollarStroke(Stroke ts, String name) {
		super();
		setName(name);
		m_recognitionResults = null;
		
		points = new ArrayList<Point>(ts.getPoints());

		resample();
		rotateToZero();
		scaleToSquare();
		translateToOrigin();
	}
	

	/**
	 * Create a one dollar stroke for the given stroke. This can either be used
	 * as a template if you {@link #setName(String)}. Or you can use this to get
	 * recognized from existing templates using {@link #recognize(List)}.
	 * 
	 * @param ts
	 *            The stroke to create a one dollar stroke from
	 */
	public OneDollarStroke(Stroke ts) {
		super();
		setName("");
		m_recognitionResults = null;
		
		points = new ArrayList<Point>(ts.getPoints());
		
		resample();
		rotateToZero();
		scaleToSquare();
		translateToOrigin();
	}
	
	/**
	 * Get the center of mass of the stroke
	 * @return
	 */
	public Point getCentroid()
	{
		if (centroid != null)
			return centroid;
		return centroid = calcCentroid();
	}
	
	/**
	 * Calculate the center of mass
	 * @return
	 */
	private Point calcCentroid()
	{
		double x=0.0, y=0.0;
		
		for (Point p: points){
			x += p.x;
			y += p.y;
		}
		
		x /= points.size();
		y /= points.size();
		return new Point(x, y);
	}
	

	/**
	 * Get the resampled/rotated/scaled/translated points after transformations
	 * are performed on the points in the original IStroke
	 * 
	 * @return The transformed points
	 */
	public List<Point> getTemplatePoints() {
		return points;
	}
	

	/**
	 * Get the results of recognition AFTER a call to {@link #recognize(List)}.
	 * Otherwise will return null
	 * 
	 * @return The results of recognition, if {@link #recognize(List)} has been
	 *         called, or null if recognition has not been performed.
	 */
	public IRecognitionResult getRecognitionResults() {
		return m_recognitionResults;
	}
	

	/**
	 * Are the names the same?
	 * 
	 * @param s
	 *            The stroke to compare to
	 * @return True if they have the same name, false otherwise, case
	 *         INSENSITIVE
	 */
	public boolean isSameAs(OneDollarStroke s) {
		return s.getName().compareToIgnoreCase(getName()) == 0;
	}
	

	/**
	 * Is {@link #getName()} the same as the given string?
	 * 
	 * @param s
	 *            The string of the other's name
	 * @return True if the names are the same, case insensitive
	 */
	public boolean isSameAs(String s) {
		return s.compareToIgnoreCase(getName()) == 0;
	}
	

	/**
	 * Resample the points so that there are {@link #RESAMPLE_SIZE} of them in
	 * the list of resampled points, spaced approriately and linearly
	 * interpolated where needed.
	 */
	protected void resample()
	{
		List<Point> newPts = resamplePoints(points, getPathLength() / (double)(RESAMPLE_SIZE-1));
		if (newPts.size() == RESAMPLE_SIZE-1)
			newPts.add(getLastPoint());
		if (newPts.size() != RESAMPLE_SIZE) {
			throw new RuntimeException("resample error");
		}
		points = newPts;
	}
	
	/**
	 * Resample a list of points to be evenly spaced.
	 * @param points list of points
	 * @param I desired distance between points
	 * @return list of evenly spaced points
	 */
	public static List<Point> resamplePoints(List<Point> points, double I) {
		double D = 0;
		ArrayList<Point> newPts = new ArrayList<Point>();
		newPts.add(points.get(0));
		for (int i = 1; i < points.size(); i++) {
			Point pim1 = points.get(i-1);
			Point pi = points.get(i);
			double d = pim1.distance(pi);
			if ((D + d) >= I) {
				double I_D_d = (I-D)/d;
				double x = pim1.x + 
							I_D_d * (pi.x - pim1.x);
				double y = pim1.getY() + 
							I_D_d * (pi.y - pim1.y);
				
				long t =  pim1.getTime() + 
						(long)(I_D_d * (pi.getTime() - pim1.getTime()));
				
				Point q = new Point(x, y, t);
				newPts.add(q);
				points.add(i, q);
				D = 0;
			}
			else {
				D = D + d;
			}
		}
		
		return newPts;
		
	}
	

	/**
	 * Rotate the stroke so that the angle between endpoints is 0.
	 */
	protected void rotateToZero() {

		Point c = getCentroid();
		double theta = Math.atan2(c.y - points.get(0).y,
		        c.x - points.get(0).x);
		rotateBy(-theta, points);
	}
	

	/**
	 * Rotate the points around the center by the given amount, and return the
	 * rotated results.
	 * 
	 * @param theta
	 *            The angle, in radians, to rotate the points by
	 * @return The rotated set of points.
	 */
	protected void rotateBy(double theta, List<Point> pts) {
		Point c = getCentroid();
		
		double cosTheta = Math.cos(theta);
		double sinTheta = Math.sin(theta);
		
		for (int i = 0; i < points.size(); ++i) {
			Point p = points.get(i);
			Point mod = pts.get(i);
			double pxcx = p.x-c.x;
			double pycy = p.y-c.y;
			mod.x = (pxcx) * cosTheta
			           - (pycy) * sinTheta + c.x;
			mod.y = (pxcx) * sinTheta
			           + (pycy) * cosTheta + c.y;
		}
	}
	

	/**
	 * Scale the points to fit into a square of side length ==
	 * {@link #SQUARE_SIZE}
	 */
	protected void scaleToSquare() {
		double size = (double) SQUARE_SIZE;
		BoundingBox bb = getBoundingBox();
		double factorX = size / bb.getWidth();
		double factorY = size / bb.getHeight();
		for (Point p : points) {
			p.x = p.x * factorX;
			p.y = p.y * factorY;
		}
	}
	

	/**
	 * Translate the points so that the average point (geometric center) of the
	 * stroke is at 0, 0.
	 */
	protected void translateToOrigin() {
		Point c = getCentroid();
		for (Point p : points) {
			p.x -= c.x;
			p.y -= c.y;
		}
	}

	/**
	 * Recognize THIS one dollar stroke using the given list of templates. The
	 * n-best list in the recognition results is trimmed
	 * {@link IRecognitionResult#trimToNInterpretations(int)} to
	 * {@link #S_NUM_NBEST_RESULTS}
	 * 
	 * @param templates
	 *            The templates to match this stroke against
	 */
	public void recognize(List<OneDollarStroke> templates) {
		m_recognitionResults = new RecognitionResult();
		
		for (OneDollarStroke T : templates) {
			double d = distanceAtBestAngle(T, -1 * MAX_DEGREE, MAX_DEGREE,
			        DELTA_DEGREE);
			
			d /= RESAMPLE_SIZE;
			
			double conf = 1 - d / 0.5;
			conf *= SQUARE_SIZE_FACTOR;
			
			Shape recShape = new Shape();
			recShape.setInterpretation(T.getName(), conf);
			
			m_recognitionResults.addShapeToNBestList(recShape);
		}
		
		m_recognitionResults.trimToNInterpretations(S_NUM_NBEST_RESULTS);
	}
	public static final double SQUARE_SIZE_FACTOR = Math.sqrt(2 * SQUARE_SIZE * SQUARE_SIZE);
	

	/**
	 * Distance from this stroke to the given stroke at the best angle of
	 * rotation.
	 * 
	 * @param T
	 *            The other stroke to compute the distance from
	 * @param thetaA
	 *            Some angular parameter
	 * @param thetaB
	 *            Some angular parameter
	 * @param thetaD
	 *            Some angular parameter
	 * @return
	 */
	protected double distanceAtBestAngle(OneDollarStroke T, double thetaA,
	        double thetaB, double thetaD) {
		double x1 = PHI * thetaA + (1 - PHI) * thetaB;
		double x2 = (1 - PHI) * thetaA + PHI * thetaB;
		
		List<Point> TPointsModifiable = new ArrayList<Point>(T.points.size());
		for (Point p: T.points)
		{
			TPointsModifiable.add(new Point(p.x, p.y, -1L));
		}
		
		double f1 = distanceAtAngle(T, x1, TPointsModifiable);
		double f2 = distanceAtAngle(T, x2, TPointsModifiable);
		while (Math.abs(thetaA - thetaB) > thetaD) {
			if (f1 < f2) {
				thetaB = x2;
				x2 = x1;
				f2 = f1;
				x1 = PHI * thetaA + (1 - PHI) * thetaB;
				f1 = distanceAtAngle(T, x1, TPointsModifiable);
			}
			else {
				thetaA = x1;
				x1 = x2;
				f1 = f2;
				x2 = (1 - PHI) * thetaA + PHI * thetaB;
				f2 = distanceAtAngle(T, x2, TPointsModifiable);
			}
		}
		if (f1 < f2)
			return f1;
		else
			return f2;
	}
	

	/**
	 * Rotate THIS stroke by theta (in radians) and compute the distance between
	 * rotated THIS and the other stroke T
	 * 
	 * @param T
	 *            The other stroke to compute distance from
	 * @param theta
	 *            The amount to rotate THIS by
	 * @return The distance between this, after rotation, and T
	 */
	protected double distanceAtAngle(OneDollarStroke T, double theta, List<Point> pts) {
		rotateBy(theta, pts);
		double d = pathDistance(pts, T.getTemplatePoints());
		return d;
	}
	

	/**
	 * Compute distance between the two sets of points on a parallel, point by
	 * point basis
	 * 
	 * @param A
	 *            First set of points
	 * @param B
	 *            Second set of points
	 * @return Sum of the point-by-point distances
	 */
	protected double pathDistance(List<Point> A, List<Point> B) {
		double d = 0;
		for (int i = 0; i < A.size(); i++) {
			d += A.get(i).distance(B.get(i));
		}
		return d;
	}
}
