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
