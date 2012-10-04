package srl.core.sketch.controllers.event;

public interface UndoableModification<T> extends Modification<T>{

	public void undo(T target);
}
