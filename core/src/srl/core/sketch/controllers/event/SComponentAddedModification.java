package srl.core.sketch.controllers.event;

import srl.core.sketch.SComponent;
import srl.core.sketch.Sketch;

public class SComponentAddedModification implements RedoableModification<Sketch>,UndoableModification<Sketch>,Comparable<Modification>{
	
	private SComponent component;
	private long timestamp;
	
	public SComponentAddedModification(SComponent component){
		this.component = component;
		this.timestamp = System.currentTimeMillis();
	}
	
	@Override
	public void undo(Sketch sketch) {
		sketch.remove(component);
	}

	@Override
	public void redo(Sketch sketch) {
		sketch.add(component);
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the component
	 */
	public SComponent getComponent() {
		return component;
	}

	@Override
	public int compareTo(Modification mod) {
		return (int)(getTimestamp()-mod.getTimestamp());
	}
	
}
