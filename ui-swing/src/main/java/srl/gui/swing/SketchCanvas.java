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
package srl.gui.swing;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLayeredPane;
import javax.swing.OverlayLayout;

import srl.core.sketch.Sketch;
import srl.core.sketch.controllers.SketchController;


public class SketchCanvas extends JLayeredPane implements MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8757954344808440542L;

	public static final int SKETCH = 1;
	public static final int BACKGROUND = 2;
	public static final int EVERYTHING = SKETCH | BACKGROUND;

	protected BackgroundImagePanel background;
	protected SketchPanel drawPanel;

	public SketchCanvas() {
		this(new Sketch());
	}
	public SketchCanvas(Sketch sketch){
		this(new SketchController(sketch));
	}
	public SketchCanvas(SketchController controller){

		setLayout(new OverlayLayout(this));

		background = new BackgroundImagePanel();
		add(background, 0);

		drawPanel = new SketchPanel(controller);
		drawPanel.setOpaque(false);
		add(drawPanel, 0);

		drawPanel.addMouseListener(this);
	}
	public void setSketch(Sketch s) {
		drawPanel.setSketch(s);
	}

	public Sketch getSketch() {
		return drawPanel.getSketch();
	}
	public SketchController getSketchController(){
		return drawPanel.getSketchController();
	}

	@Override
	public void setPreferredSize(Dimension d) {
		super.setPreferredSize(d);
		drawPanel.setPreferredSize(d);
		background.setPreferredSize(d);
	}

	public void setStrokeWidth(float width) {
		drawPanel.setStrokeWidth(width);
	}

	public void setStrokeColor(org.openawt.Color color) {
		drawPanel.setStrokeColor(color);
	}

	public void clear() {
		clear(EVERYTHING);
	}

	public void clear(int howMuch) {
		if ((howMuch & BACKGROUND) != 0) {
			background.clear();
		}

		if ((howMuch & SKETCH) != 0) {
			drawPanel.clear();
		}
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		repaint();

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		clear(BACKGROUND);
		repaint();

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}
}
