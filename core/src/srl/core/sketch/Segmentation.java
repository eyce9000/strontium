package srl.core.sketch;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class Segmentation {

	@Attribute
	protected UUID id;
	@Attribute
	public double confidence;
	@Attribute
	public String label;
	@Attribute
	public String segmenterName;

	@ElementList
	protected List<Stroke> segmentedStrokes;

	public Segmentation() {
		id = UUID.randomUUID();
		segmentedStrokes = new ArrayList<Stroke>();
	}

	public Segmentation(ISegmentation seg) {
		this();

		id = seg.getID();
		for (IStroke stroke : seg.getSegmentedStrokes())
			segmentedStrokes.add(new Stroke(stroke));
	}

	public Segmentation(Segmentation copyFrom) {
		id = copyFrom.id;
		confidence = copyFrom.confidence;
		label = copyFrom.label;

		segmentedStrokes = new ArrayList<Stroke>();
		for (Stroke s : copyFrom.segmentedStrokes)
			segmentedStrokes.add(s.clone());
	}

	/**
	 * Add a stroke to the segmentation
	 * 
	 * @param stroke
	 *            stroke to add to the segmentation
	 */
	public void addSegmentedStroke(Stroke stroke) {
		segmentedStrokes.add(stroke);
	}
	public void setId(UUID id){
		this.id = id;
	}
	public UUID getId() {
		return id;
	}

	public List<Stroke> getSegmentedStrokes() {
		return segmentedStrokes;
	}

	public void setSegmentedStrokes(List<Stroke> segmentedStrokes) {
		this.segmentedStrokes = new ArrayList<Stroke>(segmentedStrokes);
	}

	public void setSegmenterName(String name) {
		segmenterName = name;
	}

	@Override
	public Segmentation clone() {
		return new Segmentation(this);
	}
}
