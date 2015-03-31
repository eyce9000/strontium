package srl.recognition.grouping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.core.sketch.BoundingBox;
import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;
import srl.core.sketch.comparators.TimePeriodComparator;

public class CivilGrouper {

	private static Logger log = LoggerFactory.getLogger(CivilGrouper.class);

	/**
	 * Default constructor.
	 */
	public CivilGrouper() {
		// Nothing to do
	}

	/**
	 * Group a set of strokes into possible CivilSketch characters. Uses some
	 * general techniques and some character-specific groupings.
	 * 
	 * @param strokes
	 *            the strokes to group.
	 * @return the character groups.
	 */
	public static List<Shape> groupIntoCharacters(List<Stroke> strokes) {

		// Nothing from nothing
		if (strokes == null) {
			return null;
		}

		List<Shape> characterGroups = new ArrayList<Shape>();

		Collections.sort(strokes, new TimePeriodComparator());

		// 0-1 strokes
		if (strokes.size() <= 1) {
			Shape onlyShape = new Shape();
			onlyShape.setStrokes(strokes);
			characterGroups.add(onlyShape);

			return characterGroups;
		}

		// Calculate some average values
		double avgBBWidth = 0.0;
		double avgDeltaTime = 0.0;

		for (int i = 0; i < strokes.size(); i++) {
			double bbWidth = strokes.get(i).getBoundingBox().getWidth();
			avgBBWidth += bbWidth;

			if (i > 0) {
				double deltaTime = strokes.get(i).getTimeEnd()
						- strokes.get(i - 1).getTimeEnd();
				avgDeltaTime += deltaTime;
			}
		}

		avgBBWidth /= strokes.size();
		avgDeltaTime /= strokes.size() - 1;

		// Use average values with grouping
		double bbWidthThreshold = 0.15;

		Shape currShape = new Shape();
		currShape.add(strokes.get(0));
		BoundingBox currBB = currShape.getBoundingBox();

		for (int i = 1; i < strokes.size(); i++) {
			BoundingBox bb = strokes.get(i).getBoundingBox();

			// Expand bounding boxes
			BoundingBox expandedCurrBB = currBB.growWidth(0.30);
			BoundingBox expandedBB = bb.growWidth(0.30);

			// Dist threshold
			double firstLastDistThreshold = expandedCurrBB.getHeight() * 1.0;

			// Check whether a stroke is contained within the current shape.
			// The withinWidth check should handle '=' symbols.
			if (expandedCurrBB.contains(expandedBB.getCenterPoint())
					|| expandedCurrBB.withinWidth(expandedBB.getCenterPoint())
					|| expandedCurrBB.withinWidth(expandedBB
							.getCenterLeftPoint())
					|| expandedCurrBB.withinWidth(expandedBB
							.getCenterRightPoint())) {

				currShape.add(strokes.get(i));
				currShape.flagExternalUpdate();
			}

			else if (expandedBB.contains(expandedCurrBB.getCenterPoint())
					|| expandedBB.withinWidth(expandedCurrBB.getCenterPoint())
					|| expandedBB.withinWidth(expandedCurrBB
							.getCenterLeftPoint())
					|| expandedBB.withinWidth(expandedCurrBB
							.getCenterRightPoint())) {

				currShape.add(strokes.get(i));
				currShape.flagExternalUpdate();
			}

			// Check whether the end and start points of consecutive strokes are
			// far away. Also, bb area check for periods
//			else if (strokes.get(i - 1).getLastPoint().distance(
//					strokes.get(i).getFirstPoint()) < firstLastDistThreshold
//					&& bb.getArea() > 10) {
//
//				currShape.addStroke(strokes.get(i));
//				currShape.flagExternalUpdate();
//			}

			// Finalize the previous character, create a new one
			else {
				characterGroups.add(currShape);

				currShape = new Shape();
				currShape.add(strokes.get(i));
				currBB = currShape.getBoundingBox();
			}
		}

		characterGroups.add(currShape);

		return characterGroups;
	}
}
