package srl.segmentation.combination.objectiveFunctions;

import java.util.Collections;
import java.util.List;

import srl.core.sketch.Point;
import srl.core.sketch.Stroke;
import srl.recognition.paleo.Fit;
import srl.recognition.paleo.OrigPaleoSketchRecognizer;
import srl.recognition.paleo.PaleoConfig;


/**
 * Objective function for the FSS Combination algorithm that uses PaleoSketch
 * error fits. Doesn't work yet due to PaleoSketch having non-uniform errors
 * 
 * @author awolin
 */
@Deprecated
public class PaleoErrorObjectiveFunction implements IObjectiveFunction {

	/**
	 * Configuration file for what primitives we support when calculating the
	 * error
	 */
	private PaleoConfig m_paleoConfig;

	/**
	 * Default constructor
	 */
	public PaleoErrorObjectiveFunction() {

		// We want only the basic primitives when calculating our error
		m_paleoConfig = PaleoConfig.basicPrimsOnly();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.segmentation.combination.objectiveFunctions.IObjectiveFunction
	 * #solve(java.util.List, edu.tamu.core.sketch.Stroke)
	 */
	public double solve(List<Integer> corners, Stroke stroke) {

		Collections.sort(corners);
		double totalError = 0.0;

		OrigPaleoSketchRecognizer paleoRecognizer = new OrigPaleoSketchRecognizer(
				m_paleoConfig);

		for (int c = 1; c < corners.size(); c++) {

			int corner1 = corners.get(c - 1);
			int corner2 = corners.get(c);

			if (corner2 - corner1 < 2) {
				totalError += 0.1;
			} else {
				List<Point> actualSegment = stroke.getPoints().subList(corner1,
						corner2);

				// Run the stroke segment through Paleo
				paleoRecognizer.setStroke(new Stroke(actualSegment));
				paleoRecognizer.recognize();
				List<Fit> fits = paleoRecognizer.getFits();

				// Find the best fit (i.e., the fit with the least error)
				Fit bestFit = null;
				for (Fit f : fits) {
					if (bestFit == null || bestFit.getError() < f.getError())
						bestFit = f;
				}

				double err = 0.0;
				if (bestFit != null)
					err = bestFit.getError();
				else
					err = Double.MAX_VALUE;

				totalError += err;
			}
		}

		return totalError;
	}

}
