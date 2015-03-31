/**
 * GroupManager.java
 * 
 * Revision History:<br>
 * Nov 13, 2008 jbjohns - File created
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
package srl.recognition.grouping;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.BoundingBox;
import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;
import srl.patternrec.features.Feature;
import srl.patternrec.features.FeatureVector;

/**
 * Manager to handle grouping.
 * 
 * @author jbjohns
 */
public class GroupManager {
	
	/**
	 * Logger for this class
	 */
	private static final Logger log = LoggerFactory
	        .getLogger(GroupManager.class);
	
	protected SortedSet<Shape> m_strokeGroups;
	
	protected Stroke m_lastStrokeAdded = null;
	
	
	public GroupManager() {
		m_strokeGroups = new TreeSet<Shape>();
	}
	

	public void addStroke(Stroke stroke) {
		
		if (stroke == null) {
			log.error("Cannot add a null stroke");
			throw new IllegalArgumentException("Stroke cannot be null");
		}
		
		// which grouping might this stroke have membership in?
		List<Shape> candidateGroups = new ArrayList<Shape>();
		for (Shape existantGroup : m_strokeGroups) {
			
			FeatureVector strokeFeatures = computeFeatures(stroke,
			        existantGroup, m_lastStrokeAdded);
			
			if (mightBeMemberOfGroup(strokeFeatures)) {
				candidateGroups.add(existantGroup);
			}
		}
		
		if (candidateGroups.size() == 0) {
			Shape newGroup = new Shape();
			newGroup.add(stroke);
			
			m_strokeGroups.add(newGroup);
		}
		else {
			// merge all the candidate groups
			// TODO do this in a smart way and not a MERGE ALL
			Shape mergedGroup = new Shape();
			for (Shape toMerge : candidateGroups) {
				for (Stroke s : toMerge.getStrokes()) {
					mergedGroup.add(s);
				}
				
				m_strokeGroups.remove(toMerge);
			}
			mergedGroup.add(stroke);
			
			m_strokeGroups.add(mergedGroup);
		}
		
		m_lastStrokeAdded = stroke;
	}
	

	public FeatureVector computeFeatures(Stroke stroke, Shape group,
	        Stroke lastStrokeAdded) {
		
		FeatureVector groupFeatures = new GroupingGroupFeatureComputation()
		        .computeFeatures(group);
		
//		FeatureVector strokeFeatures = new GroupingStrokeFeatureComputation()
//		        .computeFeatures(stroke);
		FeatureVector strokeFeatures = new FeatureVector();
		long timeDelta = 0;
		if (lastStrokeAdded != null) {
			timeDelta = stroke.getFirstPoint().getTime()
			            - lastStrokeAdded.getLastPoint().getTime();
		}
		Feature timeDeltaFeature = new Feature("TimeDelta", timeDelta);
		strokeFeatures.add(timeDeltaFeature);
		
		String changeAreaFeatureName = "AreaChangeRatio";
		String changePerimeterFeatureName = "PerimeterChangeRatio";
		BoundingBox strokeBox = stroke.getBoundingBox();
		
		BoundingBox groupBox = group.getBoundingBox();
		double groupArea = groupBox.getArea();
		double groupPerimeter = groupBox.getPerimeter();
		
		double minx = (groupBox.getMinX() < strokeBox.getMinX()) ? groupBox
		        .getMinX() : strokeBox.getMinX();
		double miny = (groupBox.getMinY() < strokeBox.getMinY()) ? groupBox
		        .getMinY() : strokeBox.getMinY();
		double maxx = (groupBox.getMaxX() < strokeBox.getMaxX()) ? groupBox
		        .getMaxX() : strokeBox.getMaxX();
		double maxy = (groupBox.getMaxY() < strokeBox.getMaxY()) ? groupBox
		        .getMaxY() : strokeBox.getMaxY();
		
		double deltaX = maxx - minx;
		double deltaY = maxy - miny;
		
		double newArea = deltaX * deltaY;
		double newPerimeter = 2 * deltaX + 2 * deltaY;
		
		double areaChangeRatio = newArea / groupArea;
		Feature areaChangeRatioFeature = new Feature(changeAreaFeatureName,
		        areaChangeRatio);
		strokeFeatures.add(areaChangeRatioFeature);
		double perimeterChangeRatio = newPerimeter / groupPerimeter;
		Feature perimeterChangeRatioFeature = new Feature(
		        changePerimeterFeatureName, perimeterChangeRatio);
		strokeFeatures.add(perimeterChangeRatioFeature);
		
//		double strokePathLength = strokeFeatures.get("PathLength").getValue();
		double strokePathLength = stroke.getPathLength();
		
		double groupAveragePathLength = groupFeatures.get("AveragePathLength")
		        .getValue();
		double pathToGroupAverage = strokePathLength / groupAveragePathLength;
		Feature pathToAverageFeature = new Feature(
		        "StrokePathLengthToGroupAverage", pathToGroupAverage);
		strokeFeatures.add(pathToAverageFeature);
		
		double groupMinPathLength = groupFeatures.get("MinPathLength")
		        .getValue();
		double pathToGroupMin = strokePathLength / groupMinPathLength;
		Feature pathToMinFeature = new Feature("StrokePathLengthToGroupMin",
		        pathToGroupMin);
		strokeFeatures.add(pathToMinFeature);
		
		double groupMaxPathLength = groupFeatures.get("MaxPathLength")
		        .getValue();
		double pathToGroupMax = strokePathLength / groupMaxPathLength;
		Feature pathToMaxFeature = new Feature("StrokePathLengthToGroupMax",
		        pathToGroupMax);
		strokeFeatures.add(pathToMaxFeature);
		
		return strokeFeatures;
	}
	

	private boolean mightBeMemberOfGroup(FeatureVector strokeFeatures) {
		// TODO decision tree
		return false;
	}
	

	/**
	 * @return the strokeGroups
	 */
	public SortedSet<Shape> getStrokeGroups() {
		return m_strokeGroups;
	}
}
