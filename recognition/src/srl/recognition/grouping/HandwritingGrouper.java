package srl.recognition.grouping;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.BoundingBox;
import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;
import srl.recognition.handwriting.HandwritingRecognizer;



public class HandwritingGrouper {

	private static Logger log = LoggerFactory
			.getLogger(HandwritingRecognizer.class);

	public HandwritingGrouper() {

	}

	public BoundingBox getGroupingBox(Stroke stroke) {
		double ratioHW1 = stroke.getBoundingBox().getHeight()
				/ stroke.getBoundingBox().getWidth();
		double growW1 = 1.00;
		growW1 = Math.max(growW1, growW1 * ratioHW1);
		double growH1 = .05;
		if (ratioHW1 > 2)
			growH1 = 10;
		growH1 = Math.max(growH1, growH1 / ratioHW1);
		return stroke.getBoundingBox().growWidth(growW1).growHeight(growH1);

	}

	public List<BoundingBox> getGroupingBox(Shape shape) {
		List<BoundingBox> boxes = new ArrayList<BoundingBox>();
		for (Stroke s : shape.getStrokes()) {
			double ratioHW1 = s.getBoundingBox().getHeight()
					/ shape.getBoundingBox().getWidth();
			double growW1 = 1.00;
			growW1 = Math.max(growW1, growW1 * ratioHW1);
			double growH1 = .05;
			growH1 = Math.max(growH1, growH1 / ratioHW1);
			BoundingBox bb1 = s.getBoundingBox().increment();
			if (bb1.getWidth() > 3 * bb1.getHeight()) {
				bb1 = bb1.growHeight(growH1 * 2);
			} else {
				bb1 = bb1.growHeight(growH1);
			}
			bb1 = bb1.growWidth(growW1);
			boxes.add(bb1);
		}
		return boxes;

	}

	public List<Shape> group(List<Stroke> strokes) {

		ArrayList<Shape> shapegroups = new ArrayList<Shape>();
		for (int i = 0; i < strokes.size(); i++) {
			Shape textShape = new Shape();
			textShape.add(strokes.get(i));
			shapegroups.add(textShape);
		}

		boolean merged = true;
		while (merged) {
			merged = false;
			ArrayList<Shape> newshapes = new ArrayList<Shape>();
			newshapes.addAll(shapegroups);

			for (int i = 0; i < newshapes.size(); i++) {
				Shape shape1 = newshapes.get(i);

				for (int k = 0; k < newshapes.size(); k++) {
					Shape shape2 = newshapes.get(k);

					if (shape1 == shape2) {
						continue;
					}
					// This right here, is the whole grouping function
					// if (shape1.getBoundingBox().growWidth(.5).growHeight(.1)
					// .intersects(
					// shape2.getBoundingBox().growWidth(.5)
					// .growHeight(.1))) {

					for (int j = 0; j < shape1.getStrokes().size(); j++) {
						Stroke stroke1 = shape1.getStrokes().get(j);

						if (merged)
							break;

						double ratioHW1 = (stroke1.getBoundingBox().getHeight() + 1)
								/ (shape1.getBoundingBox().getWidth() + 1);
						double growW1 = 1.00;
						growW1 = Math.max(growW1, growW1 * ratioHW1);
						double growH1 = .05;
						growH1 = Math.max(growH1, growH1 / ratioHW1);
						BoundingBox bb1 = stroke1.getBoundingBox().increment();
						if (bb1.getWidth() > 3 * bb1.getHeight())
							bb1 = bb1.growHeight(growH1 * 2);
						else
							bb1 = bb1.growHeight(growH1);
						bb1 = bb1.growWidth(growW1);

						for (int l = 0; l < shape2.getStrokes().size(); l++) {
							Stroke stroke2 = shape2.getStrokes().get(l);

							double ratioHW2 = (stroke2.getBoundingBox()
									.getHeight() + 1)
									/ (shape2.getBoundingBox().getWidth() + 1);
							double growW2 = 1.00;
							growW2 = Math.max(growW2, growW2 * ratioHW2);
							double growH2 = .05;
							growH2 = Math.max(growH2, growH2 / ratioHW2);
							BoundingBox bb2 = stroke2.getBoundingBox()
									.increment();
							if (bb2.getWidth() > 3 * bb2.getHeight())
								bb2 = bb2.growHeight(growH2 * 2);
							else
								bb2 = bb2.growHeight(growH2);
							bb2 = bb2.growWidth(growW2);
							if (bb1.intersects(bb2)) {
								log.debug("Bouding Boxes Intersect");
								merged = true;
							}
						}
					}
					if (merged) {
						for (int l = 0; l < shape2.getStrokes().size(); l++) {
							Stroke stroke = shape2.getStrokes().get(l);
							shape1.add(stroke);
						}
						shapegroups.remove(shape2);
						merged = true;
						break;
					}

				}
				if (merged) {
					break;
				}
			}
		}

		return shapegroups;
	}

	public List<Shape> groupIntersection(List<Stroke> strokes) {

		List<Shape> groups = new ArrayList<Shape>();

		for (Stroke stroke : strokes) {

			if (groups.size() == 0) {
				Shape sh = new Shape();
				sh.add(stroke);
				groups.add(sh);
			}

			else {

				// TODO Consider how to handle if it overlaps with more than one
				boolean overlap = false;

				for (Shape sh : groups) {
					if (sh.getBoundingBox().intersects(stroke.getBoundingBox())) {
						sh.add(stroke);
						overlap = true;
						break;
					}
				}

				if (!overlap) {
					Shape sh = new Shape();
					sh.add(stroke);
					groups.add(sh);
				}
			}
		}

		return groups;

	}

	// public List<Shape> groupIntersection(List<Stroke> strokes) {
	//		
	// List<Shape> strokeGroups = new ArrayList<Shape>();
	//		
	// for(Stroke st : strokes) {
	//			
	// if(strokeGroups.size() == 0) {
	// Shape newShape = new Shape();
	// newShape.add(st);
	// strokeGroups.add(newShape);
	// continue;
	// }
	//			
	// List<Shape> groupMembers = new ArrayList<Shape>();
	//			
	// for(Shape sh: strokeGroups) {
	// if(sh.getBoundingBox().intersects(st.getBoundingBox())) {
	// groupMembers.add(sh);
	// }
	// }
	//			
	// if(groupMembers.size() == 1) {
	//				
	// }
	//			
	// }
	// return null;
	// }

}
