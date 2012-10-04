package srl.core.analysis;

import java.util.ArrayList;
import java.util.List;

import srl.core.sketch.Point;


public class CurvatureAnalyzer implements StrokeAnalyzer{
	private ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();
	Point prevPoint1 = null,prevPoint2 = null;
	@Override
	public double nextPoint(Point point) {
		double value;
		if (prevPoint2 == null) {
			value = 0;
		} else {
			value = Math.abs(Math.atan2(point.y-prevPoint1.y, point.x-prevPoint1.x) - 
					Math.atan2(prevPoint1.y-prevPoint2.y, prevPoint1.x-prevPoint2.x));
		}
		prevPoint2 = prevPoint1;
		prevPoint1 = point;
		
		dataPoints.add(new DataPoint(value,point.getTime()));
		
		return value;
	}

	@Override
	public List<DataPoint> getData() {
		return dataPoints;
	}

	@Override
	public String getName() {
		return "Curvature";
	}

}
