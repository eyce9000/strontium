/**
 * GroupingGroupFeatureComputation.java
 * 
 * Revision History:<br>
 * Dec 12, 2008 jbjohns - File created
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.BoundingBox;
import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;
import srl.patternrec.features.Feature;
import srl.patternrec.features.FeatureVector;
import srl.patternrec.features.IFeatureComputation;

/**
 * 
 * @author jbjohns
 */
public class GroupingGroupFeatureComputation implements
        IFeatureComputation<Shape> {
	
	/**
	 * Logger for this class;
	 */
	private static final Logger log = LoggerFactory
	        .getLogger(GroupingGroupFeatureComputation.class);
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.patternrec.features.IFeatureComputation#computeFeatures(java
	 * .lang.Object)
	 */
	@Override
	public FeatureVector computeFeatures(Shape input) {
		
		if (input == null) {
			log.error("Shape to compute features for cannot be null");
			throw new IllegalArgumentException("Cannot compute features on null shape");
		}
		
		FeatureVector vector = new FeatureVector();
		
		// compute the bounding box for the group
		BoundingBox groupBox = input.getBoundingBox();
		Feature minxFeature = new Feature("MinX", groupBox.getMinX());
		vector.add(minxFeature);
		Feature minyFeature = new Feature("MinY", groupBox.getMinY());
		vector.add(minyFeature);
		Feature maxxFeature = new Feature("MaxX", groupBox.getMaxX());
		vector.add(maxxFeature);
		Feature maxyFeature = new Feature("MaxY", groupBox.getMaxY());
		vector.add(maxyFeature);

		double sumPathLengths = 0;
		double minPathLength = Double.MAX_VALUE;
		double maxPathLength = Double.MIN_VALUE;
		long minStartTime = Long.MAX_VALUE;
		long maxEndTime = Long.MIN_VALUE;
		for (Stroke stroke : input.getStrokes()) {
			double pathLength = stroke.getPathLength();
			sumPathLengths += pathLength;
			minPathLength = (minPathLength <= pathLength) ? minPathLength : pathLength;
			maxPathLength = (maxPathLength >= pathLength) ? maxPathLength : pathLength;
			
			long startTime = stroke.getTimeStart();
			minStartTime = (minStartTime <= startTime) ? minStartTime : startTime;
			long endTime = stroke.getTimeEnd();
			maxEndTime = (maxEndTime >= endTime) ? maxEndTime : endTime;
		}
		
		Feature averagePathLengthFeature = new Feature("AveragePathLength", (sumPathLengths / input.getStrokes().size()));
		vector.add(averagePathLengthFeature);
		Feature minPathLengthFeature = new Feature("MinPathLength", minPathLength);
		vector.add(minPathLengthFeature);
		Feature maxPathLengthFeature = new Feature("MaxPathLength", maxPathLength);
		vector.add(maxPathLengthFeature);
		
		Feature startTimeFeature = new Feature("EarliestTime", minStartTime);
		vector.add(startTimeFeature);
		Feature endTimeFeature = new Feature("LatestTime", maxEndTime);
		vector.add(endTimeFeature);
		
		return vector;
	}
	
}
