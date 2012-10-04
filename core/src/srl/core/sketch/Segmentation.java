package srl.core.sketch;

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
