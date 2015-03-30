/**
 * LineComputations.java
 * 
 * Revision History:<br>
 * Jul 18, 2008 jbjohns - File created
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
package srl.math.linear;

import org.openawt.geom.Line2D;



/**
 * Class to perform various calculations on Line2D objects that are not included
 * in the Line2D API
 * 
 * @author jbjohns
 */
public class LineComputations {

	/**
	 * How far from zero can we be before we declare something to be zero? This
	 * helps avoid divide by zero and division overflow.
	 */
	public static final double EPSILON = 1e-5;

	/**
	 * Compute the minimum distance between two line segments defined in the two
	 * Line2D objects.
	 * <p>
	 * The algorithm used is that described in
	 * http://geometryalgorithms.com/Archive/algorithm_0106/algorithm_0106.htm.
	 * It is ported from C++ to Java. The original copyright notice from the C++
	 * version is:
	 * <p>
	 * Copyright 2001, softSurfer (www.softsurfer.com) This code may be freely
	 * used and modified for any purpose providing that this copyright notice is
	 * included with it. SoftSurfer makes no warranty for this code, and cannot
	 * be held liable for any real or imagined damage resulting from its use.
	 * Users of this code must verify correctness for their application.
	 * <p>
	 * This method runs in O(1): just a lot of 2 dimensional calculations.
	 * 
	 * @param line1
	 *            The first line segment
	 * @param line2
	 *            The second line segment
	 * @return The minimum distance between the two line segments
	 */
	public static double minimumDistanceBetweenSegments(Line2D line1,
			Line2D line2) {

		// direction vector for the first line, from p1 to p2
		Vector2D line1Dir = Vector2D.differenceVector(line1.getP2(),
				line1.getP1());
		// direction vector for second line, from p1 to p2
		Vector2D line2Dir = Vector2D.differenceVector(line2.getP2(),
				line2.getP1());
		// Projection of line1 onto line2, to help find where the minimum
		// distance occurs
		Vector2D line1_line2_proj = Vector2D.differenceVector(line1.getP1(),
				line2.getP1());

		// set the algorithm web site (in method Javadoc) for description on
		// these variables. They're used to find the closed-form solution to the
		// min distance
		// squared length of line1
		double a = Vector2D.dotProduct(line1Dir, line1Dir);
		// projection of line1 onto line2
		double b = Vector2D.dotProduct(line1Dir, line2Dir);
		// squared length of line2
		double c = Vector2D.dotProduct(line2Dir, line2Dir);
		// project line1 onto the projection
		double d = Vector2D.dotProduct(line1Dir, line1_line2_proj);
		// project line2 onto the projection
		double e = Vector2D.dotProduct(line2Dir, line1_line2_proj);

		// denominator for our closed-form solution to the closest points on the
		// segments.
		double D = a * c - b * b;

		// how far along line1 is the closest point?
		double line1Mult = 0; // 0...1
		// line1Mult = line1N / line1D
		double line1N = 0; // line1N is like how far along line1 to go
		double line1D = D; // line1D is like how long line1 segment is

		// how far along line2 is the closest point?
		double line2Mult = 0; // 0...1
		// line2Mult = line2N / line2D
		double line2N = 0; // line2N is like how far along line2 to go
		double line2D = D; // line2D is like how long line2 segment is

		// if the line segments are close to parallel, the denominator value
		// D will be very close to 0. This is because the dot product
		// b = line1*line2 = |line1||line2| cos(theta). If parallel, theta = 0
		// and cos(theta) = 1. Thus, the dot product will be the multiplications
		// of the lengths of the vectors.
		// b*b = |line1||line2|*|line1||line2|
		// AND, since a and c are the squared lengths of line1 and line2, we
		// have
		// a*c = |line1||line1| * |line2||line2| = b*b
		// We probably won't be EXACTLY a*c=b*b, since it's doubtful the
		// segments are EXACTLY parallel. So we use an epsilon from 0.
		if (D < EPSILON) {
			// start by using first point of line1
			line1N = 0;
			line1D = 1.0;
			// initial values for line2
			line2N = e;
			line2D = c;
		}
		// segments not parallel. Get closest points on inifinite lines and
		// constrain
		else {
			// closest points on infinite line
			line1N = (b * e - c * d);
			line2N = (a * e - b * d);

			// constrain closest points based on line 1
			// closest point is beyond first endpoint of line1
			if (line1N < 0) {
				// line1Mult at start of line1
				line1N = 0.0;
				line2N = e;
				line2D = c;
			}
			// closest point beyond second endpoint of line1
			else if (line1N > line1D) {
				// line1Mult at end of line1
				line1N = line1D;
				line2N = e + b;
				line2D = c;
			}
		}

		// closest point beyond first endpoint of line2
		if (line2N < 0) {
			// line2mult at start of line
			line2N = 0;

			// recompute line1Mult based on line2's limitations
			// d = <line1, line1_line2_proj> = project line1 onto
			// line1_line2_proj
			if (-d < 0) {
				// d not long enough, beyond first endpoint of line1
				line1N = 0;
			}
			// a = length of line1
			else if (-d > a) {
				// d too long, beyond endpoint of line1
				line1N = line1D;
			}
			// d falls somewhere on line1
			else {
				// where does projection of line1 on line2 fall? closest point
				// will occur there
				line1N = -d;
				// length of line1
				line1D = a;
			}
		}
		// closest point beyond second endpoint of line2
		else if (line2N > line2D) {
			// line2mult at end of line
			line2N = line2D;

			// recompute line1Mult based on line2's limitations
			// d = projection of line1 onto line1_line2_proj
			// b = squared length of line2
			// d not long enough, falls short of line1 first endpoint
			if ((-d + b) < 0) {
				// use line1's first endpoint
				line1N = 0;
			}
			// d too long and past second endpoint of line1
			// a == length of line1
			else if ((-d + b) > a) {
				// use line1's second endpoint
				line1N = line1D;
			} else {
				// just the right length, use a point on line1 segment
				line1N = (-d + b);
				// length of line1
				line1D = a;
			}
		}

		// FINALLY compute how far along each of the lines the closest point
		// lies. We use the ternary check ?: to ensure that something that
		// "should be zero" actually is zero, for handling numerical
		// instability.
		line1Mult = (Math.abs(line1N) < EPSILON) ? 0 : (line1N / line1D);
		line2Mult = (Math.abs(line2N) < EPSILON) ? 0 : (line2N / line2D);

		// we know closest points lie line1Mult and line2Mult magical units
		// along lines 1 and 2. Get the vector between the points on these lines
		Vector2D line1Vector = line1Dir.scalarMultiply(line1Mult);
		Vector2D line2Vector = line2Dir.scalarMultiply(line2Mult);
		// projection of line1 onto line2, added to the difference vector
		// between line2 vector and line1 vector
		Vector2D shortestVector = line1_line2_proj.addTo(line1Vector
				.differenceFrom(line2Vector));

		// min distance between two lines is the norm of the shortest vector
		return shortestVector.norm();
	}

}
