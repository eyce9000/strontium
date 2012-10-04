package srl.core.sketch.controllers;

import java.util.LinkedList;
import java.util.List;

import srl.core.sketch.SComponent;
import srl.core.sketch.Sketch;
import srl.core.sketch.Stroke;
import srl.core.sketch.controllers.event.SketchModifiedListener;
import srl.core.sketch.controllers.event.StrokeAddedListener;
public class SketchController {
	private Sketch sketch;

	/*
	 * Listeners
	 */
	private List<StrokeAddedListener> strokeAddedListeners = new LinkedList<StrokeAddedListener>();
	private List<SketchModifiedListener> sketchModifiedListeners = new LinkedList<SketchModifiedListener>();

	public SketchController(){
		this(new Sketch());
	}
	public SketchController(Sketch sketch){
		setSketch(sketch);
	}
	public void setSketch(Sketch sketch){
		this.sketch = sketch;
		onSketchModified();
	}
	public synchronized void addComponent(SComponent component){
		if(component != null){
			this.sketch.add(component);
			if(component instanceof Stroke){
				for(StrokeAddedListener listener:strokeAddedListeners){
					listener.onStrokeAdded((Stroke)component);
				}
			}
			onSketchModified();
		}
	}
	public synchronized boolean removeComponent(SComponent component){
		if(component!=null){
			if(this.sketch.remove(component)){
				return true;
			}
		}
		return false;
	}

	protected void onSketchModified(){
		for(SketchModifiedListener listener:sketchModifiedListeners){
			listener.onSketchModified(sketch);
		}
	}

	public synchronized void clearSketch(){
		sketch.clear();
		onSketchModified();
	}

	public Sketch getSketch(){
		return sketch;
	}


	/*
	 * Listener controls
	 */
	public synchronized  void addSketchModifiedListener(SketchModifiedListener listener){
		sketchModifiedListeners.add(listener);
	}
	public synchronized  void removeSketchModifiedListener(SketchModifiedListener listener){
		sketchModifiedListeners.remove(listener);
	}

	public synchronized  void addStrokeAddedListener(StrokeAddedListener listener){
		strokeAddedListeners.add(listener);
	}
	public synchronized  void removeStrokeAdddedListener(StrokeAddedListener listener){
		strokeAddedListeners.remove(listener);
	}
}
