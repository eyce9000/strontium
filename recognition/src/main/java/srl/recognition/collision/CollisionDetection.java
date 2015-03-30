/**
 * CollisionDetection.java
 * 
 * Revision History:<br>
 * Sep 17, 2008 rgraham - File created
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
package srl.recognition.collision;

import java.util.ArrayList;
import java.util.List;

import srl.core.sketch.Point;
import srl.core.sketch.Stroke;

public class CollisionDetection {
	
	public static double POINT_THRESHOLD = 6.0;
	
	public static boolean detectCollision(Point p1, Point p2) {
		if (p1.distance(p2) < POINT_THRESHOLD) {
			return true;
		}
		return false;
	}
	
	public static boolean detectCollision(Stroke s, Point p) {
		if (s.getNumPoints() == 1) {
			return detectCollision(s.getPoint(0), p);			
		} else if (s.getNumPoints() > 0) {
			List<Point> firstHalf = new ArrayList<Point>();
			List<Point> secondHalf = new ArrayList<Point>();
			List<Point> tmp = s.getPoints();
			
			for (int i = 0; i < tmp.size(); i++) {
				if (i < (tmp.size()/2)) {
					firstHalf.add(tmp.get(i));
				} else {
					secondHalf.add(tmp.get(i));
				}
			}
			
			
			Stroke firstS1 = new Stroke(firstHalf);
			Stroke lastS1 = new Stroke(secondHalf);
			
			return detectCollision(firstS1, p) || detectCollision(lastS1, p);
		} else {
			return false;
		}
	}
	
	public static boolean detectCollision(Stroke s1, Stroke s2) {
		if (s1.getNumPoints() == 1 && s2.getNumPoints() == 1) {
			return detectCollision(s1.getPoint(0), s2.getPoint(0));			
		} else if (s1.getNumPoints() == 1 && s2.getNumPoints() > 1) {
			return detectCollision(s2, s1.getFirstPoint());
		} else if (s1.getNumPoints() > 1 && s2.getNumPoints() == 1) {
			return detectCollision(s1, s2.getFirstPoint());
		} else if (s1.getNumPoints() > 1 && s2.getNumPoints() > 1) {
			List<Point> s1_firstHalf = new ArrayList<Point>();
			List<Point> s1_secondHalf = new ArrayList<Point>();
			List<Point> s1_tmp = s1.getPoints();
			
			for (int i = 0; i < s1_tmp.size(); i++) {
				if (i < (s1_tmp.size()/2)) {
					s1_firstHalf.add(s1_tmp.get(i));
				} else {
					s1_secondHalf.add(s1_tmp.get(i));
				}
			}
			
			
			Stroke firstS1 = new Stroke(s1_firstHalf);
			Stroke lastS1 = new Stroke(s1_secondHalf);
			
			List<Point> s2_firstHalf = new ArrayList<Point>();
			List<Point> s2_secondHalf = new ArrayList<Point>();
			List<Point> s2_tmp = s2.getPoints();
			
			for (int i = 0; i < s2_tmp.size(); i++) {
				if (i < (s2_tmp.size()/2)) {
					s2_firstHalf.add(s2_tmp.get(i));
				} else {
					s2_secondHalf.add(s2_tmp.get(i));
				}
			}
			
			
			Stroke firstS2 = new Stroke(s2_firstHalf);
			Stroke lastS2 = new Stroke(s2_secondHalf);
			
			return detectCollision(firstS1, firstS2) || 
				   detectCollision(lastS1, firstS2) || 
				   detectCollision(lastS1, lastS2) || 
				   detectCollision(firstS1, lastS2);
		} else {
			return false;
		}
	}

}
