package srl.core.sketch.controllers.event;


public interface RedoableModification<T> extends Modification<T>{
	public void redo(T target);
}
