/**
 * IPoint.java
 * 
 * Revision History: <br>
 * (5/23/08) bpaulson - interface created <br>
 * (5/23/08) awolin - added the distance(IPoint) function to the interface<br>
 * (5/26/08) bpaulson - added setters <br>
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

import java.util.UUID;

/**
 * Interface for a basic X, Y, Time point. There is no semantic meaning to these
 * values stored in this interface. It is up to the user of concrete point
 * implementations to assign semantic meaning to the X, Y, and time values.
 * <p>
 * In the general usage of IPoint implementations, X and Y are usually the
 * screen coordinates (pixels) where the ink is drawn. However, as long as X and
 * Y values are relatively consistent in magnitude and value with one another,
 * the actual values or semantic meaning to the values does not matter. This
 * means you can use IPoint implementations which store latitude/longitude or
 * screen coordinates, either one, as long as all your points were consistent.
 * <p>
 * Points are identified by a unique {@link UUID}. Though points consist of an
 * (x, y, timestamp) tuple, this information alone might not identify a point
 * uniquely. However, we cannot assume even random UUIDs are always unique,
 * especially across multiple systems. Therefore, equality and hash code are
 * both based on the information: ID, X, Y, and Time.
 * 
 * @author bpaulson
 */
public interface IPoint extends Comparable<IPoint>, Cloneable {

	/**
	 * Get the X value of the point.
	 * 
	 * @return x value of the point.
	 */
	public double getX();

	/**
	 * Get the Y value of the point.
	 * 
	 * @return y value of the point.
	 */
	public double getY();

	/**
	 * Get the Time value of the point.
	 * 
	 * @return time value of the point.
	 */
	public long getTime();

	/**
	 * Get the UUID of the IPoint. This is the unique identifier that identifies
	 * this specific point.
	 * 
	 * @return UUID of the IPoint.
	 */
	public UUID getID();

	/**
	 * Determine if two points are equal (same X, Y, and Time values)
	 * 
	 * @param p
	 *            point to compare against.
	 * @return true if points have the same x, y, time; else false.
	 */
	public boolean equalsXYTime(IPoint p);

	/**
	 * Get the Euclidean distance between two points.
	 * 
	 * @param p
	 *            second point.
	 * @return Euclidean distance between two points.
	 */
	public double distance(IPoint p);

	/**
	 * Get the Euclidean distance between two points.
	 * 
	 * @param x
	 *            x coordinate of second point.
	 * @param y
	 *            y coordinate of second point.
	 * @return Euclidean distance between two points.
	 */
	public double distance(double x, double y);

	/**
	 * Clone an IPoint.
	 * 
	 * @return a new, deep copied IPoint.
	 */
	public Object clone();

	public void translate(double x, double y);

	public void scale(double x, double y);
}
