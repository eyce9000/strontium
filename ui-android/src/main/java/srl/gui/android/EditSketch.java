/*******************************************************************************
 *  Revision History:<br>
 *  SRL Member - File created
 *
 *  <p>
 *  <pre>
 *  This work is released under the BSD License:
 *  (C) 2012 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Sketch Recognition Lab, Texas A&M University 
 *        nor the names of its contributors may be used to endorse or promote 
 *        products derived from this software without specific prior written 
 *        permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  </pre>
 *  
 *******************************************************************************/
package srl.gui.android;

import java.util.ArrayList;
import java.util.List;

import org.openawt.draw.android.ShapePainter;

import srl.core.sketch.Point;
import srl.core.sketch.Sketch;
import srl.core.sketch.Stroke;
import srl.core.sketch.controllers.event.StrokeAddedListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

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
