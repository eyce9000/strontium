package srl.gui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;


import srl.core.sketch.Sketch;
import srl.core.sketch.controllers.UndoSketchController;
import srl.recognition.IRecognitionResult;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.PaleoSketchRecognizer;


public class TestApplication extends JFrame implements WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6161673017193491959L;
	
	private SketchCanvas canvas;

	private SketchToolbar sketchToolbar;

	public TestApplication() {
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		canvas = new SketchCanvas(new UndoSketchController());

		canvas.setPreferredSize(new Dimension(500, 500));
		canvas.drawPanel.addMouseListener(new MouseAdapter() {
			final PaleoSketchRecognizer psr = new PaleoSketchRecognizer(PaleoConfig.allOn());
			public void mouseReleased(MouseEvent e) {
				IRecognitionResult res = psr.recognize(canvas.getSketch().getLastStroke());
				System.out.println("paleo says: " + res.getBestShape().getInterpretation().label);
			}
		});
		
		
		sketchToolbar = new SketchToolbar((UndoSketchController)canvas.getSketchController());
		
		mainPanel.add(canvas, BorderLayout.CENTER);
		mainPanel.add(sketchToolbar, BorderLayout.PAGE_START);
		
		add(mainPanel);

		try {
			canvas.setSketch(Sketch.deserializeXML(new File("test.xml")));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		addWindowListener(this);
		pack();
	}

	public static void main(String[] args) {
		TestApplication ta = new TestApplication();
		ta.setVisible(true);
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		try {
			canvas.getSketch().serializeXML(new File("test.xml"));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("done");
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

}
