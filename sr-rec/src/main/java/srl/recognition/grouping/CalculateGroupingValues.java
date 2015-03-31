package srl.recognition.grouping;

import java.util.ArrayList;
import java.util.List;

import srl.core.sketch.Point;
import srl.core.sketch.Shape;
import srl.core.sketch.Stroke;
import srl.recognition.collision.CollisionDetection;

public class CalculateGroupingValues {
	
	public static List<String> getValues(Stroke stroke, Shape shape) {
		List<String> values = new ArrayList<String>();
		
		values.add(checkIntersection(stroke, shape).toString());
		values.add(timeBetweenLastStroke(stroke, shape).toString());
		values.add(distanceBetweenLast(stroke, shape).toString());
		values.add(ratioToBoundingBoxSize(stroke, shape).toString());
		values.add(ratioToBoundingBoxWidth(stroke, shape).toString());
		values.add(ratioToBoundingBoxHeight(stroke, shape).toString());
		values.add(ratioToAverageStrokeLength(stroke, shape).toString());
		values.add(strokeCountIfIncluded(stroke, shape).toString());
		values.add(areaIncreaseRatio(stroke, shape).toString());
		values.add(heightIncreaseRatio(stroke, shape).toString());
		values.add(widthIncreaseRatio(stroke, shape).toString());
		return values;
		
	}
	
	public static Boolean checkIntersection(Stroke stroke, Shape shape) {
		for(Stroke shapeStroke: shape.getStrokes()) {
			if(CollisionDetection.detectCollision(shapeStroke, stroke))
				return true;
		}
		
		return false;
		
	}
	
	public static Long timeBetweenLastStroke(Stroke stroke, Shape shape) {
		return stroke.getTimeEnd() - shape.getLastStroke().getTimeEnd();
		
	}
	
	public static Double distanceBetweenLast(Stroke stroke, Shape shape) {
		Point lastShapePoint = shape.getLastStroke().getLastPoint();
		
		Point lastStrokePoint = stroke.getLastPoint();
		
		return distance(lastShapePoint, lastStrokePoint);
		
	}
	
	public static Double ratioToBoundingBoxSize(Stroke stroke, Shape shape) {
		return stroke.getBoundingBox().getArea()/shape.getBoundingBox().getArea();
	}
	
	public static Double ratioToBoundingBoxWidth(Stroke stroke, Shape shape) {
		return stroke.getBoundingBox().getWidth()/shape.getBoundingBox().getWidth();
	}
	
	public static Double ratioToBoundingBoxHeight(Stroke stroke, Shape shape) {
		return stroke.getBoundingBox().getHeight()/shape.getBoundingBox().getHeight();
	}
	
	public static Double ratioToAverageStrokeLength(Stroke stroke, Shape shape) {
		double total = 0.0;
		
		for(Stroke st: shape.getStrokes()) {
			total += st.getPathLength();
		}
		
		return stroke.getPathLength()/(total/shape.getStrokes().size());
	}
	
	public static Integer strokeCountIfIncluded(Stroke stroke, Shape shape) {
		return shape.getStrokes().size() + 1;
	}

	public static Double areaIncreaseRatio(Stroke stroke, Shape shape) {
		Shape holderShape = new Shape();
		
		holderShape.setStrokes(shape.getStrokes());
		holderShape.add(stroke);
	
		return holderShape.getBoundingBox().getArea()/shape.getBoundingBox().getArea();
	}
	
	public static Double heightIncreaseRatio(Stroke stroke, Shape shape) {
		Shape holderShape = new Shape();
		holderShape.setStrokes(shape.getStrokes());
		holderShape.add(stroke);
		
		return holderShape.getBoundingBox().getHeight()/shape.getBoundingBox().getHeight();
	}
	
	public static Double widthIncreaseRatio(Stroke stroke, Shape shape) {
		Shape holderShape = new Shape();
		holderShape.setStrokes(shape.getStrokes());
		holderShape.add(stroke);
		
		return holderShape.getBoundingBox().getWidth()/shape.getBoundingBox().getWidth();
	}
	
	
	private static Double distance(Point p1, Point p2) {
		return Math.sqrt(Math.pow((p2.getX() - p1.getX()),2) + Math.pow((p2.getY() - p1.getY()),2));

	}
}
