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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import org.openawt.Color;
import org.openawt.draw.awt.ShapePainter;
import org.openawt.svg.Style;

import srl.core.sketch.Point;
import srl.core.sketch.Shape;
import srl.core.sketch.Sketch;
import srl.core.sketch.Stroke;
import srl.core.sketch.controllers.SketchController;
import srl.core.sketch.controllers.event.SketchModifiedListener;


public class SketchPanel extends JPanel implements MouseListener,
		MouseMotionListener,SketchModifiedListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6696793162706187136L;

	protected SketchController sketchController;
	protected Stroke currentStroke;

	protected Color currentColor;

	protected Shape testShape;

	protected Style drawStyle = new Style().setFill(Color.NONE).setStroke(Color.BLACK);

	public SketchPanel() {
		this(new SketchController());
	}
	public SketchPanel(SketchController controller){
		sketchController = controller;
		sketchController.addSketchModifiedListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		currentColor = Color.BLACK;
	}

	public SketchPanel(Sketch s) {
		this();
		setSketch(s);
	}

	public Sketch getSketch() {
		return sketchController.getSketch();
	}
	public SketchController getSketchController(){
		return sketchController;
	}
	public void setSketch(Sketch s) {
		sketchController.setSketch(s);
		this.revalidate();
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		currentStroke = new Stroke();
		currentStroke.addPoint(new Point(e.getX(),e.getY()));
		currentStroke.setStyle((Style)drawStyle.clone());
		currentStroke.getStyle().setStroke(currentColor);
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		sketchController.addComponent(currentStroke);
		currentStroke = null;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		currentStroke.addPoint(new Point(e.getX(),e.getY(),e.getWhen()));
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void paint(Graphics g){
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		
		ShapePainter.setColor(g2, currentColor);
		ShapePainter.draw(g2, sketchController.getSketch().toSVGShape(), null);
		if(currentStroke!=null)
			ShapePainter.draw(g2, currentStroke.toSVGShape(),drawStyle);
	}

	public void setStrokeWidth(float width) {
		drawStyle = new Style().setStrokeWidth(width);
	}

	public void setStrokeColor(Color color) {
		currentColor = color;
	}

	public void clear() {
		sketchController.clearSketch();
		currentStroke = null;
	}
	@Override
	public void onSketchModified(Sketch sketch) {
		this.invalidate();
		repaint();
	}
}
