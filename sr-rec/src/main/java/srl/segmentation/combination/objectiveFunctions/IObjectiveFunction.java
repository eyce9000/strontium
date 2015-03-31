package srl.segmentation.combination.objectiveFunctions;

import java.util.List;

import srl.core.sketch.Stroke;


/**
 * Objective function for Feature Subset Selection
 * 
 * @author awolin
 */
public interface IObjectiveFunction {

	/**
	 * Solve the objective function
	 * 
	 * @param corners
	 *            Corner indices of the stroke
	 * @param stroke
	 *            Stroke to segment
	 * @return Value of the objective function
	 */
	public double solve(List<Integer> corners, Stroke stroke);
}
