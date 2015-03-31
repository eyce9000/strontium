/**
 * 
 */
package srl.gui.swing.tools.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import srl.core.sketch.Point;


/**
 * Generic line plot class (from original tamudrg code base); is able to plot
 * multiple series on the same panel
 * 
 * @author bpaulson
 */
public class Plot extends JPanel {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -8768269091955387946L;

	/**
	 * List of arrays of x values (1 array per series to plot)
	 */
	private List<double[]> m_xList = new ArrayList<double[]>();

	/**
	 * List of arrays of y values (1 array per series to plot)
	 */
	private List<double[]> m_yList = new ArrayList<double[]>();

	/**
	 * List of colors (one color per series to plot)
	 */
	private List<Color> m_colorList = new ArrayList<Color>();

	/**
	 * Boolean denoting whether or not should be shown
	 */
	private boolean m_keepdim = false;

	/**
	 * Boolean denoting whether or not plot points should be painted as ovals
	 */
	private boolean m_drawOvals = true;

	/**
	 * Default constructor
	 */
	public Plot() {
		JFrame frame = new JFrame();
		frame.setSize(330, 350);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(this, BorderLayout.CENTER);
		setBackground(Color.white);
		frame.setVisible(true);
	}

	/**
	 * Constructor for plot that takes a title
	 * 
	 * @param title
	 *            title of the plot
	 */
	public Plot(String title) {
		JFrame frame = new JFrame(title);
		frame.setSize(330, 350);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(this, BorderLayout.CENTER);
		setBackground(Color.white);
		frame.setVisible(true);
	}

	/**
	 * Save the plot as a JPG image
	 * 
	 * @param filename
	 *            file name of where to save the plot
	 */
	public void saveJPG(String filename) {
		try {
			VolatileImage vvi = createVolatileImage(getWidth(), getHeight());
			paint(vvi.createGraphics());
			BufferedImage vi = vvi.getSnapshot();
			File jpgFile = new File(filename.replaceAll("txt", "jpg"));
			ImageIO.write(vi, "jpg", jpgFile);
			vi.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Manually force a plot re-scale
	 */
	public void rescale() {
		if (m_xList.size() == 0) {
			return;
		}
		if (m_xList.get(0).length == 0) {
			return;
		}
		double minx = m_xList.get(0)[0];
		double maxx = m_xList.get(0)[0];
		double miny = m_yList.get(0)[0];
		double maxy = m_yList.get(0)[0];
		for (int i = 0; i < m_xList.size(); i++) {
			double[] xarray = m_xList.get(i);
			double[] yarray = m_yList.get(i);
			for (int j = 0; j < xarray.length; j++) {
				minx = Math.min(minx, xarray[j]);
				miny = Math.min(miny, yarray[j]);
				maxx = Math.max(maxx, xarray[j]);
				maxy = Math.max(maxy, yarray[j]);
			}
		}
		double xscale = 300 / (maxx - minx);
		double yscale = 300 / (maxy - miny);
		if (m_keepdim) {
			xscale = 300 / Math.max(maxx - minx, maxy - miny);
			yscale = 300 / Math.max(maxx - minx, maxy - miny);
		}
		for (int i = 0; i < m_xList.size(); i++) {
			double[] xarray = m_xList.get(i);
			double[] yarray = m_yList.get(i);
			for (int j = 0; j < xarray.length; j++) {
				xarray[j] = (xarray[j] - minx) * xscale + 10;
				yarray[j] = (yarray[j] - miny) * yscale + 10;
			}
		}
	}

	/**
	 * Update the plot and repaint
	 */
	public void plot() {
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g_old) {
		Graphics2D g = (Graphics2D) g_old;
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		rescale();
		for (int i = 0; i < m_xList.size(); i++) {
			g.setColor(m_colorList.get(i));
			double[] xarray = m_xList.get(i);
			double[] yarray = m_yList.get(i);
			for (int j = 0; j < xarray.length - 1; j++) {
				g.drawLine((int) xarray[j], (int) yarray[j],
						(int) xarray[j + 1], (int) yarray[j + 1]);
				if (m_drawOvals) {
					g.drawOval((int) xarray[j] - 1, (int) yarray[j] - 1, 2, 2);
					g.drawOval((int) xarray[j + 1] - 1,
							(int) yarray[j + 1] - 1, 2, 2);
				}
			}
		}
	}

	/**
	 * Boolean denoting if plot is currently visible or not
	 * 
	 * @return true if visible; else false
	 */
	public boolean isKeepdim() {
		return m_keepdim;
	}

	/**
	 * Set whether or not plot should be visible or not
	 * 
	 * @param keepdim
	 *            boolean specifying whether or not the plot is visible
	 */
	public void setKeepdim(boolean keepdim) {
		m_keepdim = keepdim;
	}

	/**
	 * Flag denoting if ovals should be drawn where points are specified
	 * 
	 * @param drawOvals
	 *            true if points should be drawn as ovals, else false
	 */
	public void setDrawOvals(boolean drawOvals) {
		m_drawOvals = drawOvals;
	}

	/**
	 * Add a line/series to the plot
	 * 
	 * @param x
	 *            x values of the line
	 * @param y
	 *            y values of the line
	 * @param c
	 *            color of the line
	 */
	public void addLine(double x[], double y[], Color c) {
		m_xList.add(x);
		m_yList.add(y);
		m_colorList.add(c);
	}

	/**
	 * Add a line/series consisting of one point
	 * 
	 * @param x
	 *            x value of point
	 * @param y
	 *            y value of point
	 * @param c
	 *            color of line
	 */
	public void addLine(double x, double y, Color c) {
		double[] xd = new double[1];
		double[] yd = new double[1];
		xd[0] = x;
		yd[0] = y;
		addLine(xd, yd, c);
	}

	/**
	 * Add a line/series to the plot
	 * 
	 * @param x
	 *            x values of the line
	 * @param y
	 *            y values of the line
	 * @param c
	 *            color of the line
	 */
	public void addLine(int[] xpoints, int[] ypoints, Color c) {
		double[] x = new double[xpoints.length];
		double[] y = new double[ypoints.length];
		for (int i = 0; i < xpoints.length; i++) {
			x[i] = xpoints[i];
			y[i] = ypoints[i];
		}
		addLine(x, y, c);
	}

	/**
	 * Add a line/series to the plot
	 * 
	 * @param pts
	 *            points of the line
	 * @param c
	 *            color of the line
	 */
	public void addLine(Point[] pts, Color c) {
		double[] x = new double[pts.length];
		double[] y = new double[pts.length];
		for (int i = 0; i < pts.length; i++) {
			x[i] = pts[i].getX();
			y[i] = pts[i].getY();
		}
		addLine(x, y, c);
	}

	/**
	 * Add a line/series to the plot
	 * 
	 * @param pts
	 *            points of the line
	 * @param c
	 *            color of the line
	 */
	public void addLine(List<Point> pts, Color c) {
		double[] x = new double[pts.size()];
		double[] y = new double[pts.size()];
		for (int i = 0; i < pts.size(); i++) {
			x[i] = pts.get(i).getX();
			y[i] = pts.get(i).getY();
		}
		addLine(x, y, c);
	}

	/**
	 * Add a line/series to the plot
	 * 
	 * @param pts
	 *            points of the line
	 * @param c
	 *            color of the line
	 */
	public void addLine(Point2D[] pts, Color c) {
		double[] x = new double[pts.length];
		double[] y = new double[pts.length];
		for (int i = 0; i < pts.length; i++) {
			x[i] = pts[i].getX();
			y[i] = pts[i].getY();
		}
		addLine(x, y, c);
	}
}
