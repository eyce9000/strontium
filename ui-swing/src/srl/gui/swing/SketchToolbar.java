/**
 * SketchToolbar.java
 * 
 * Revision History:<br>
 * Nov 8, 2011 fvides - File created
 *
 * <p>
 * <pre>
 * This work is released under the BSD License:
 * (C) 2011 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&M University 
 *       nor the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */

package srl.gui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import srl.core.sketch.Sketch;
import srl.core.sketch.controllers.UndoSketchController;
import srl.core.sketch.controllers.event.SketchModifiedListener;

public class SketchToolbar extends JPanel implements ActionListener, SketchModifiedListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6652340186152118749L;
	
	private JButton btnUndoStroke;
	
	private JButton btnRedoStroke;
	
	private JButton btnClearSketch;
	
	private UndoSketchController controller;
	
	public SketchToolbar(UndoSketchController controller){
		
		this.controller = controller;
		controller.addSketchModifiedListener(this);
		
		btnUndoStroke = new JButton("undo");
		btnUndoStroke.addActionListener(this);
		
		btnRedoStroke = new JButton("redo");
		btnRedoStroke.addActionListener(this);
		
		btnClearSketch = new JButton("clear");		
		btnClearSketch.addActionListener(this);
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		add(btnUndoStroke);
		add(btnRedoStroke);
		add(btnClearSketch);
		
		this.onSketchModified(controller.getSketch());
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnUndoStroke) {
			controller.undo();

		} else if (e.getSource() == btnRedoStroke) {
			controller.redo();

		}else if (e.getSource() == btnClearSketch) {
			controller.clearSketch();
		}
		
	}

	@Override
	public void onSketchModified(Sketch sketch) {
		btnUndoStroke.setEnabled(controller.canUndo());
		btnRedoStroke.setEnabled(controller.canRedo());
		btnClearSketch.setEnabled(sketch.size() > 0);
	}

}
