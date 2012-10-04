package srl.gui.swing;

import org.openawt.Color;
import org.openawt.draw.awt.ShapePainter;
import org.openawt.svg.Style;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

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
