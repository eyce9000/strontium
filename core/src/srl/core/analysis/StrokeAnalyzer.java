package srl.core.analysis;

import java.util.List;

import srl.core.sketch.Point;
import srl.core.sketch.Stroke;


public interface StrokeAnalyzer {
	public double nextPoint(Point point);
	public List<DataPoint> getData();
	public String getName();
}
