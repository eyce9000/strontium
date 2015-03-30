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

import java.util.Set;
import java.util.UUID;

import org.openawt.geom.AffineTransform;
import org.openawt.geom.Point2D;
import org.openawt.svg.SVGCircle;
import org.openawt.svg.SVGShape;
import org.simpleframework.xml.Attribute;


public class Point extends SComponent implements TimeInstant, Comparable<TimeInstant>{
	public transient final int radius = 5;

	@Attribute
	public double x, y;
	@Attribute(required=false)
	public Double pressure;
	@Attribute(required=false)
	public Double tiltX,tiltY;
	@Attribute
	public long time;
	
	public UUID id;
	
	public Point() {
		this(0.0, 0.0);
	}

	public Point(double x, double y) {
		this(x, y, -1L);
	}

	public Point(double x, double y, double pressure) {
		this(x, y, -1L, pressure);
	}
	public Point(double x, double y, long time) {
		this.x = x;
		this.y = y;
		this.time = time;
		id = SComponent.nextID();
	}

	public Point(double x, double y, long time, UUID id) {
		this(x, y, time);
		this.id = id;
	}

	public Point(double x, double y, long time, double pressure) {
		this(x, y, time);
		this.pressure = pressure;
	}
	
	public Point(Point copyFrom) {
		this(copyFrom.x, copyFrom.y, copyFrom.time);
		this.id = copyFrom.id;
		this.pressure = copyFrom.pressure;
	}

	public Point(IPoint point) {
		this(point.getX(), point.getY(), point.getTime());
		id = point.getID();
	}
	
	public Point2D toPoint2D(){
		return new Point2D.Double(x,y);
	}
	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}
	
	public void setTime(long time){
		this.time = time;
	}
	
	public Double getPressure(){
		return pressure;
	}
	public void setPressure(Double pressure){
		this.pressure = pressure;
	}

	/**
	 * @return the tiltX
	 */
	public Double getTiltX() {
		return tiltX;
	}

	/**
	 * @param tiltX the tiltX to set
	 */
	public void setTiltX(Double tiltX) {
		this.tiltX = tiltX;
	}

	/**
	 * @return the tiltY
	 */
	public Double getTiltY() {
		return tiltY;
	}

	/**
	 * @param tiltY the tiltY to set
	 */
	public void setTiltY(Double tiltY) {
		this.tiltY = tiltY;
	}

	@Override
	public void applyTransform(AffineTransform xform, Set<Transformable> xformed) {
		if (xformed.contains(this))
			return;

		xformed.add(this);

		Point2D point = xform.transform(new Point2D.Double(x, y),
				new Point2D.Double());
		x = point.getX();
		y = point.getY();
	}
	
	@Override
	public Point clone() {
		return new Point(this);
	}

	

	/**
	 * Distance between two points
	 * 
	 * @param other
	 * @return
	 */
	public double distance(Point other) {
		return distance(other.x, other.y);

	}

	/**
	 * Distance between this point and (x,y)
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public double distance(double x, double y) {
		double Xdist = this.x - x;
		double Ydist = this.y - y;

		return Math.sqrt(Xdist * Xdist + Ydist * Ydist);
	}
	
	public double distanceSquared(double x, double y){
		return ((this.x-x)*(this.x-x)+(this.y-y)*(this.y-y));
	}
	
	public double distanceSquared(Point pt){
		return distanceSquared(pt.x,pt.y);
	}

	public boolean equalsByContent(SComponent o) {
		if (o instanceof Point) {
			Point other = (Point) o;
			return (Math.abs(other.x-x+other.y-y) < .00001) && time == other.time;
		}
		return false;
	}

	public String toString() {
		return super.toString() + " (" + x + "," + y + ")";
	}


	@Override
	public long getTime() {
		return time;
	}

	@Override
	public int compareTo(TimeInstant other) {
		return (int)(this.getTime()-other.getTime());
	}

	@Override
	public SVGShape toSVGShape() {
		return new SVGCircle(x,y,5);
	}

	@Override
	protected void calculateBBox() {
	}

}
