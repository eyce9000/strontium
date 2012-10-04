package srl.core.sketch.controllers.event;

import srl.core.sketch.SComponent;
import srl.core.sketch.Sketch;

public class SComponentRemovedModification implements RedoableModification<Sketch>,UndoableModification<Sketch>,Comparable<Modification>{
	private SComponent component;
	private long timestamp;
	public SComponentRemovedModification(SComponent component){
		this.component = component;
		this.timestamp = System.currentTimeMillis();
	}
	
	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public int compareTo(Modification mod) {
		return (int)(timestamp - mod.getTimestamp());
	}

	@Override
	public void undo(Sketch target) {
		target.add(component);
	}

	/**
	 * @return the component
	 */
	public SComponent getComponent() {
		return component;
	}
	
	@Override
	public void redo(Sketch target) {
		target.remove(component);
	}

}
