/**
 * GroupingStrokeFeatureComputation.java
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


import srl.core.sketch.BoundingBox;
import srl.core.sketch.Stroke;
import srl.patternrec.features.Feature;
import srl.patternrec.features.FeatureVector;
import srl.patternrec.features.IFeatureComputation;

/**
 * Compute features of a stroke used for grouping.
 * 
 * @author jbjohns
 */
public class GroupingStrokeFeatureComputation implements
        IFeatureComputation<Stroke> {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.patternrec.features.IFeatureComputation#computeFeatures(java
	 * .lang.Object)
	 */
	public FeatureVector computeFeatures(Stroke stroke) {
		
		if (stroke == null) {
			throw new IllegalArgumentException(
			        "Cannot compute the features of a null stroke");
		}
		
		FeatureVector vector = new FeatureVector();
		
		BoundingBox bbox = stroke.getBoundingBox();
		
		Feature minxFeature = new Feature("BBoxMinX", bbox.getMinX());
		vector.add(minxFeature);
		
		Feature minyFeature = new Feature("BBoxMinY", bbox.getMinY());
		vector.add(minyFeature);
		
		Feature maxxFeature = new Feature("BBoxMaxX", bbox.getMaxX());
		vector.add(maxxFeature);
		
		Feature maxyFeature = new Feature("BBoxMaxY", bbox.getMaxY());
		vector.add(maxyFeature);
		
//		Feature startTimeFeature = new Feature("StartTime", stroke
//		        .getFirstPoint().getTime());
//		vector.add(startTimeFeature);
//		
//		Feature endTimeFeature = new Feature("EndTime", stroke.getLastPoint()
//		        .getTime());
//		vector.add(endTimeFeature);
		
		Feature pathLengthFeature = new Feature("PathLength", stroke
		        .getPathLength());
		vector.add(pathLengthFeature);
		
		return vector;
	}
}
