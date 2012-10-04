package srl.gui.android;


import org.openawt.draw.android.ShapePainter;
import org.openawt.svg.Style;

import srl.core.sketch.SComponent;
import srl.core.sketch.Sketch;
import srl.core.sketch.Stroke;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public abstract class SketchView extends View {
	
	public SketchView(Context context) {
		super(context);
		initialize(null);
	}
	public SketchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(attrs);
	}
	public SketchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(attrs);
	}

	private Sketch sketch;
	private Style defaultStyle = new Style();
	
	
	private void initialize(AttributeSet attrs){
		defaultStyle.setStroke(org.openawt.Color.BLUE);
	}
	
	public void setSketch(Sketch sketch){
		this.sketch = sketch;
		this.invalidate();
	}
	
	
	/**
	 * Gets the sketch object drawn by this sketch view
	 * @return
	 */
	public Sketch getSketch(){
		return sketch;
	}
	
	/**
	 * Empties the sketch
	 */
	public void clearSketch(){
		sketch = new Sketch();
		this.invalidate();
	}

	@Override
	public void onDraw(Canvas c) {
		c.drawColor(Color.WHITE);
		for(SComponent comp:sketch){
			ShapePainter.draw(c, comp.toSVGShape(),defaultStyle);
		}
		
	}
	
}
