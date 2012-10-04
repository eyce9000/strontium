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
