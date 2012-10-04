package srl.core.analysis;

import java.util.ArrayList;
import java.util.List;

import srl.core.sketch.Point;
import srl.core.sketch.Stroke;


public class VelocityAnalyzer implements StrokeAnalyzer {
	private List<DataPoint> velocities = new ArrayList<DataPoint>();
	private Point prevPoint;

	@Override
	public double nextPoint(Point point) {
		DataPoint dataPoint;
		if(prevPoint==null){
			dataPoint = new DataPoint(0.0,point.getTime());
		}
		else{
			double dt = point.getTime()-prevPoint.getTime();
			double dxdt = point.distance(prevPoint)/dt;
			dataPoint = new DataPoint(dxdt,point.getTime());
		}
		prevPoint = point;
		velocities.add(dataPoint);
		return dataPoint.getValue();
	}

	@Override
	public List<DataPoint> getData() {
		return velocities;
	}

	@Override
	public String getName() {
		return "Velocity";
	}


}
