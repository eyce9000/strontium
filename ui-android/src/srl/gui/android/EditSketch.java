package srl.gui.android;

import java.util.ArrayList;
import java.util.List;

import org.openawt.draw.android.ShapePainter;

import srl.core.sketch.Point;
import srl.core.sketch.Sketch;
import srl.core.sketch.Stroke;
import srl.core.sketch.controllers.event.StrokeAddedListener;

import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class EditSketch extends SketchView implements View.OnTouchListener{
	/**
	 * The current stroke
	 */
	private Stroke currentStroke;
	
	/**
	 * A list of registered stroke listeners
	 */
	private List<StrokeAddedListener> strokeListeners = new ArrayList<StrokeAddedListener>();

	private int mCurrentStrokeLength;

	private boolean mStopStroke;

	private Point mPrevPoint;

	public EditSketch(Context context) {
		super(context);
		init();
	}
	public EditSketch(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public EditSketch(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init(){
		this.setSketch(new Sketch());
		this.setOnTouchListener(this);
	}
	
	public Stroke getCurrentStroke(){
		return currentStroke;
	}
	
	public void addStrokeDrawnListener(StrokeAddedListener listener){
		strokeListeners.add(listener);
	}
	
	public void removeStrokeDrawnListener(StrokeAddedListener listener){
		strokeListeners.remove(listener);
	}
	
	private void fireStrokeEvent(Stroke s){
		for(StrokeAddedListener listener: strokeListeners){
			listener.onStrokeAdded(s);
		}
	}
	
	private void stopStroke(){
		mStopStroke = true;
		currentStroke = null;
	}
	
	private void onSingleTouch(MotionEvent m) {
		float pressure = m.getPressure();
		if (m.getAction() == MotionEvent.ACTION_DOWN)
		{
			currentStroke = new Stroke();
			mCurrentStrokeLength = 0;
			mStopStroke = false;
		}
		else if (m.getAction() == MotionEvent.ACTION_UP)
		{
			if(mCurrentStrokeLength > 30){
				if(!mStopStroke){
					fireStrokeEvent(currentStroke);
				}
				getSketch().add(currentStroke);
			}
			stopStroke();
		}
		if(!mStopStroke){
			Point pt = new Point(m.getX(), m.getY());
			pt.setTime(System.currentTimeMillis());
			pt.setPressure(new Double(m.getPressure()));
			currentStroke.addPoint(pt);

			if(mPrevPoint!=null){
				mCurrentStrokeLength += mPrevPoint.distance(pt);
			}
			
			mPrevPoint = pt;
		}
		this.invalidate();
	}
	
	@Override
	public void onDraw(Canvas c) {
		super.onDraw(c);
		if(currentStroke!=null){
			Paint paint = new Paint();
			paint.setColor(Color.RED);
			ShapePainter.draw(c, currentStroke.toSVGShape());
		}
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent m) {
		onSingleTouch(m);
		return true;
	}
}
