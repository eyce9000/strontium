package srl.core.sketch.controllers;

import java.util.Stack;

import srl.core.sketch.SComponent;
import srl.core.sketch.Sketch;
import srl.core.sketch.controllers.event.RedoableModification;
import srl.core.sketch.controllers.event.SComponentAddedModification;
import srl.core.sketch.controllers.event.SComponentRemovedModification;
import srl.core.sketch.controllers.event.SContainerModification;
import srl.core.sketch.controllers.event.UndoableModification;

public class UndoSketchController extends SketchController{

	private Stack<UndoableModification<Sketch>> undoStack = new Stack<UndoableModification<Sketch>>();
	private Stack<RedoableModification<Sketch>> redoStack = new Stack<RedoableModification<Sketch>>();
	
	@Override
	public synchronized void addComponent(SComponent component){
		redoStack.clear();
		undoStack.push(new SComponentAddedModification(component));
		super.addComponent(component);
	}
	@Override
	public synchronized boolean removeComponent(SComponent component){
		if(super.removeComponent(component)){
			redoStack.clear();
			undoStack.push(new SComponentRemovedModification(component));
			return true;
		}
		return false;
	}
	
	@Override
	public synchronized void clearSketch(){
		undoStack.clear();
		redoStack.clear();
		super.clearSketch();
	}
	
	public SContainerModification getModifications(){
		SContainerModification mod = new SContainerModification();
		for(UndoableModification undo:undoStack){
			if(undo instanceof SComponentAddedModification)
				mod.appendAdded(((SComponentAddedModification) undo).getComponent());
			else if(undo instanceof SComponentRemovedModification)
				mod.appendRemoved(((SComponentRemovedModification) undo).getComponent());
		}
		return mod;
	}
	
	public synchronized boolean undo(){
		if(undoStack.isEmpty()){
			return false;
		}
		else{
			UndoableModification<Sketch> undoMod = undoStack.pop();
			undoMod.undo(getSketch());
			if(undoMod instanceof RedoableModification){
				redoStack.push((RedoableModification<Sketch>)undoMod);
			}
			
			onSketchModified();
			return !undoStack.isEmpty();
		}
	}
	public boolean canUndo(){
		return !undoStack.isEmpty();
	}
	
	public synchronized boolean redo(){
		if(redoStack.isEmpty())
			return false;
		else{
			RedoableModification<Sketch> redoMod = redoStack.pop();
			redoMod.redo(getSketch());
			if(redoMod instanceof UndoableModification){
				undoStack.push((UndoableModification<Sketch>)redoMod);
			}
			
			onSketchModified();
			return !redoStack.isEmpty();
		}
	}
	public boolean canRedo(){
		return !redoStack.isEmpty();
	}
}
