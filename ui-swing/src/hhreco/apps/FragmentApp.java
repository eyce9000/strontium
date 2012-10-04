package hhreco.apps;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;

import org.hhreco.fragmentation.Basis;
import org.hhreco.fragmentation.EllipseBasis;
import org.hhreco.fragmentation.FitData;
import org.hhreco.fragmentation.Fragmenter;
import org.hhreco.fragmentation.LineBasis;
import org.hhreco.recognition.MSTrainingModel;
import org.hhreco.recognition.MSTrainingParser;
import org.hhreco.recognition.TimedStroke;
import org.hhreco.toolbox.Util;

public class FragmentApp extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 536349258774319201L;
	public final static int FRAME_HEIGHT = 300;
	public final static int FRAME_WIDTH = 800;
	public static double SYMBOL_HEIGHT = 80;
	public static double SYMBOL_WIDTH = 80;
	JFileChooser _dialog = null;

	SymbolSetsDisplay _upperPanel = null;
	JCheckBoxMenuItem _showOrigDataCB = null;
	JCheckBoxMenuItem _showOrigDataPointsCB = null;
	JCheckBoxMenuItem _showBreakPointsCB = null;
	JCheckBoxMenuItem _showFitsCB = null;
	JPanel _wholePanel = null;// the base panel in the window
	JTextField _templateField = null;
	JTextField _numEInput = null;
	JTextField _numLInput = null;
	SCDisplay _fragDisplay = null;

	public static void main(String argv[]) {
		if (argv.length == 1) {
			FragmentApp gui = new FragmentApp(argv[0]);
		} else {
			System.out.println("Please enter a file name");
		}
	}

	public FragmentApp(String dfile) {
		setTitle(dfile);
		MSTrainingModel model = null;
		if ((dfile != null) && (dfile.endsWith(".sml"))) {// display data
			try {
				BufferedReader br = new BufferedReader(new FileReader(dfile));
				MSTrainingParser parser = new MSTrainingParser();
				model = (MSTrainingModel) parser.parse(br);
				br.close();

			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}
		}
		// menu
		JMenuBar mb = new JMenuBar();
		// file menu
		JMenu m1 = new JMenu("File");
		JMenuItem mi1 = new JMenuItem("Open...");
		String dataDir = System.getProperty("user.dir") + "/data";
		_dialog = new JFileChooser(dataDir);
		_dialog.setFileFilter(new SMLFileFilter());
		mi1.addActionListener(new OpenActionListener(this));
		JMenuItem mi2 = new JMenuItem("Exit");
		mi2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(1);
			}
		});
		m1.add(mi1);
		m1.addSeparator();
		m1.add(mi2);
		// view menu
		JMenu m2 = new JMenu("View");
		_showOrigDataCB = new JCheckBoxMenuItem("Show original strokes", true);
		_showOrigDataCB.addItemListener(new ShowODItemListener());
		_showOrigDataPointsCB = new JCheckBoxMenuItem(
				"Show original stroke points", true);
		_showOrigDataPointsCB.addItemListener(new ShowODPItemListener());
		_showBreakPointsCB = new JCheckBoxMenuItem("Show break points", true);
		_showBreakPointsCB.addItemListener(new ShowBPItemListener());
		_showFitsCB = new JCheckBoxMenuItem("Show fits", true);
		_showFitsCB.addItemListener(new ShowFitsItemListener());
		m2.add(_showOrigDataCB);
		m2.add(_showOrigDataPointsCB);
		m2.add(_showBreakPointsCB);
		m2.add(_showFitsCB);

		mb.add(m1);
		mb.add(m2);
		setJMenuBar(mb);

		_wholePanel = new JPanel();
		_wholePanel.setLayout(new BorderLayout());
		_upperPanel = new SymbolSetsDisplay(model, _showOrigDataCB.getState(),
				_showOrigDataPointsCB.getState(),
				_showBreakPointsCB.getState(), _showFitsCB.getState());
		JPanel lowerPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		JPanel lowerLeft = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Enter Template:");
		_templateField = new JTextField(10);
		JButton but1 = new JButton("Fragment Selected Symbol");
		JButton but2 = new JButton("Fragment All Symbols");
		but1.addActionListener(new FragmentActionListener());
		but2.addActionListener(new FragmentAllActionListener());
		lowerLeft.add("North", label);
		lowerLeft.add("Center", _templateField);
		JPanel buttons = new JPanel(new GridLayout(1, 2, 5, 5));
		buttons.add(but1);
		buttons.add(but2);
		lowerLeft.add("South", buttons);

		JPanel lowerRight = new JPanel(new BorderLayout());

		JPanel p1 = new JPanel(new GridLayout(1, 2, 5, 5));
		JPanel p1_1 = new JPanel(new BorderLayout());
		p1_1.add("North", new JLabel("# of E's:"));
		_numEInput = new JTextField("0", 10);
		p1_1.add("Center", _numEInput);
		JPanel p1_2 = new JPanel(new BorderLayout());
		p1_2.add("North", new JLabel("# of L's:"));
		_numLInput = new JTextField("0", 10);
		p1_2.add("Center", _numLInput);
		p1.add(p1_1);
		p1.add(p1_2);
		but1 = new JButton("Fragment Selected Symbol");
		but2 = new JButton("Fragment All Symbols");
		but1.addActionListener(new DPActionListener());
		but2.addActionListener(new DPAllActionListener());
		buttons = new JPanel(new GridLayout(1, 2, 5, 5));
		buttons.add(but1);
		buttons.add(but2);
		lowerRight.add("Center", p1);
		lowerRight.add("South", buttons);

		lowerPanel.add(lowerLeft);
		lowerPanel.add(lowerRight);
		_wholePanel.add("Center", _upperPanel);
		_wholePanel.add("South", lowerPanel);
		getContentPane().add("Center", _wholePanel);
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setVisible(true);
	}

	/**
	 * Segment the selected symbol with the template.
	 */
	private class DPActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int numE = Integer.parseInt(_numEInput.getText());
			int numL = Integer.parseInt(_numLInput.getText());
			SD symbol = _upperPanel.getCurrentSymbolClass().getSelectedSymbol();
			String type = _upperPanel.getCurrentSymbolClass().getType();
			System.out
					.println("Segment with " + numE + " E's, " + numL + "L's");
			long t1 = System.currentTimeMillis();
			SD segSymbol = new SD(symbol, numE, numL);
			SD[] symArray = new SD[1];
			symArray[0] = segSymbol;
			_fragDisplay = new SCDisplay(type, symArray);
			JFrame f = new JFrame();
			f.getContentPane().add(new JScrollPane(_fragDisplay));
			f.setSize((int) segSymbol.getSymbolWidth() + 100,
					(int) segSymbol.getSymbolHeight() + 100);
			f.setLocation(0, FRAME_HEIGHT);// under the initial frame
			f.setVisible(true);
		}
	}

	/**
	 * Segment all shapes with the template of the selected symbol.
	 */
	private class DPAllActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int numE = Integer.parseInt(_numEInput.getText());
			int numL = Integer.parseInt(_numLInput.getText());
			SCDisplay symClass = _upperPanel.getCurrentSymbolClass();
			String type = symClass.getType();
			int n = symClass.getSymbolCount();
			SD[] symbols = symClass.getSymbols();
			SD[] segSymbols = new SD[n];
			System.out
					.println("Segment with " + numE + " E's, " + numL + "L's");
			for (int i = 0; i < n; i++) {
				System.out.println("symbol #" + i);
				segSymbols[i] = new SD(symbols[i], numE, numL);
			}
			_fragDisplay = new SCDisplay(type, segSymbols);
			JFrame f = new JFrame();
			f.getContentPane().add(new JScrollPane(_fragDisplay));
			f.setSize(FRAME_WIDTH, FRAME_HEIGHT);
			f.setLocation(0, FRAME_HEIGHT);// under the initial frame
			f.setVisible(true);
		}
	}

	/**
	 * Segment the selected symbol with the template.
	 */
	private class FragmentActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String template = _templateField.getText().toUpperCase();
			SD symbol = _upperPanel.getCurrentSymbolClass().getSelectedSymbol();
			String type = _upperPanel.getCurrentSymbolClass().getType();
			System.out.println("Segment with:" + template);
			SD segSymbol = new SD(symbol, template);
			SD[] symArray = new SD[1];
			symArray[0] = segSymbol;
			_fragDisplay = new SCDisplay(type, symArray);
			JFrame f = new JFrame();
			f.getContentPane().add(new JScrollPane(_fragDisplay));
			f.setSize((int) segSymbol.getSymbolWidth() + 100,
					(int) segSymbol.getSymbolHeight() + 100);
			f.setLocation(0, FRAME_HEIGHT);// under the initial frame
			f.setVisible(true);
		}
	}

	/**
	 * Segment all shapes with the template of the selected symbol.
	 */
	private class FragmentAllActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String template = _templateField.getText().toUpperCase();
			SCDisplay symClass = _upperPanel.getCurrentSymbolClass();
			String type = symClass.getType();
			int n = symClass.getSymbolCount();
			SD[] symbols = symClass.getSymbols();
			SD[] segSymbols = new SD[n];
			System.out.println("Segment with:" + template);
			for (int i = 0; i < n; i++) {
				System.out.println("symbol #" + i);
				segSymbols[i] = new SD(symbols[i], template);
			}
			_fragDisplay = new SCDisplay(type, segSymbols);
			JFrame f = new JFrame();
			f.getContentPane().add(new JScrollPane(_fragDisplay));
			f.setSize(FRAME_WIDTH, FRAME_HEIGHT);
			f.setLocation(0, FRAME_HEIGHT);// under the initial frame
			f.setVisible(true);
		}

	}

	private class OpenActionListener implements ActionListener {
		private JFrame _topFrame = null;

		public OpenActionListener(JFrame top) {
			_topFrame = top;
		}

		public void actionPerformed(ActionEvent e) {
			_dialog.showOpenDialog((Component) e.getSource());
			File dataFile = _dialog.getSelectedFile();
			try {
				BufferedReader br = new BufferedReader(new FileReader(dataFile));
				MSTrainingParser parser = new MSTrainingParser();
				MSTrainingModel model = (MSTrainingModel) parser.parse(br);
				br.close();
				_topFrame.setTitle(dataFile.getName());
				_showBreakPointsCB.setState(true);
				_showFitsCB.setState(true);
				_wholePanel.remove(_upperPanel);
				_upperPanel = new SymbolSetsDisplay(model,
						_showOrigDataCB.getState(),
						_showOrigDataPointsCB.getState(),
						_showBreakPointsCB.getState(), _showFitsCB.getState());
				_wholePanel.add(_upperPanel);
				_wholePanel.setVisible(false);
				_wholePanel.setVisible(true);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	// set show orig strokes
	private class ShowODItemListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				_showOrigDataPointsCB.setEnabled(false);
			} else {
				_showOrigDataPointsCB.setEnabled(true);
			}
			_upperPanel.toggleShowOrigData();
			_upperPanel.repaint();
			if (_fragDisplay != null) {
				_fragDisplay.setShowOrigData(_showOrigDataCB.getState());
			}
		}
	}

	// set show orig data points
	private class ShowODPItemListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			_upperPanel.toggleShowOrigDataPoints();
			_upperPanel.repaint();
			if (_fragDisplay != null) {
				_fragDisplay.setShowOrigDataPoints(_showOrigDataPointsCB
						.getState());
			}
		}
	}

	// set show break points
	private class ShowBPItemListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			_upperPanel.toggleShowBreakPoints();
			_upperPanel.repaint();
			if (_fragDisplay != null) {
				_fragDisplay.setShowBreakPoints(_showBreakPointsCB.getState());
			}
		}
	}

	// set show fits
	private class ShowFitsItemListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			_upperPanel.toggleShowFits();
			_upperPanel.repaint();
			if (_fragDisplay != null) {
				_fragDisplay.setShowFits(_showFitsCB.getState());
			}
		}
	}

	/**
	 * Fragment the shapes in the dataFile and display them, class by class. The
	 * ComboBox lists all the classes in the training model and by default,
	 * selects the first in the list. Below the ComboBox, the symbols in the
	 * selected class are shown in a GridLayout panel.
	 */
	private class SymbolSetsDisplay extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2875141167951436609L;
		private JScrollPane _scrollPane = null;
		private HashMap _typeToImages = new HashMap();
		private boolean _showOrigData = true;
		private boolean _showOrigDataPoints = true;
		private boolean _showBreakPoints = true;
		private boolean _showFits = true;
		private SCDisplay _currentSymbolClass = null;

		public SymbolSetsDisplay(MSTrainingModel model, boolean showOrigData,
				boolean showOrigDataPoints, boolean showBreakPoints,
				boolean showFits) {
			_showOrigData = showOrigData;
			_showOrigDataPoints = showOrigDataPoints;
			_showBreakPoints = showBreakPoints;
			_showFits = showFits;
			String[] typeStrings = new String[model.getTypeCount()];
			int i = 0;
			Dimension dim = new Dimension(600, 350);
			for (Iterator types = model.types(); types.hasNext();) {
				String t = (String) types.next();
				typeStrings[i++] = t;
				int n = model.positiveExampleCount(t);
				TimedStroke[][] examples = new TimedStroke[n][];
				int ct = 0;
				for (Iterator exs = model.positiveExamples(t); exs.hasNext();) {
					examples[ct++] = (TimedStroke[]) exs.next();
				}
				System.out.println(t + " " + ct + " examples");
				TimedStroke[][] filteredExamples = preprocess(examples);// filter
				SD[] symbols = new SD[n];
				for (int j = 0; j < n; j++) {
					symbols[j] = new SD(examples[j], filteredExamples[j]);
				}
				SCDisplay scd = new SCDisplay(t, symbols);
				scd.setShowOrigData(_showOrigData);
				scd.setShowOrigDataPoints(_showOrigDataPoints);
				scd.setShowBreakPoints(_showBreakPoints);
				scd.setShowFits(_showFits);
				scd.addMouseListener(new SelectMouseListener());
				_typeToImages.put(t, scd);
			}
			JComboBox classList = new JComboBox(typeStrings);
			classList.setSelectedIndex(0);
			classList.addActionListener(new SwitchClassActionListener());
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createEtchedBorder());
			add("North", classList);
			_currentSymbolClass = (SCDisplay) _typeToImages.get(typeStrings[0]);
			// createEnlargedSymbol(_currentSymbolClass.getSelectedSymbol());
			_scrollPane = new JScrollPane(_currentSymbolClass);
			add("Center", _scrollPane);
		}

		/**
		 * Normalize the translation and scaling of the examples in place.
		 */
		private TimedStroke[][] preprocess(TimedStroke[][] examples) {
			// TimedStroke[][] preprocessedStrokes = new
			// TimedStroke[examples.length][];
			for (int i = 0; i < examples.length; i++) {
				TimedStroke[] orig = examples[i];
				Util.normScaling(orig, SYMBOL_HEIGHT, SYMBOL_WIDTH);// modify in
																	// place
			}
			return examples;

			/*
			 * TimedStroke[] filtered = new TimedStroke[orig.length]; for(int
			 * j=0; j<orig.length; j++){ filtered[j] =
			 * ApproximateStrokeFilter.approximate(orig[j],1.0); filtered[j] =
			 * InterpolateStrokeFilter.interpolate(orig[j],10.0); }
			 * preprocessedStrokes[i]=filtered; } //return examples; return
			 * preprocessedStrokes;
			 */
		}

		public SCDisplay getCurrentSymbolClass() {
			return _currentSymbolClass;
		}

		private class SelectMouseListener extends MouseAdapter {
			public void mouseClicked(MouseEvent evt) {
				SD sd = (SD) _currentSymbolClass
						.findComponentAt(evt.getPoint());
				if (sd != _currentSymbolClass.getSelectedSymbol()) {
					_currentSymbolClass.setSelectedSymbol(sd);
				}
			}
		}

		/**
		 * Invoked when selection happens to ComboBox. The current image panel
		 * is swapped.
		 */
		private class SwitchClassActionListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				String type = (String) cb.getSelectedItem();
				_currentSymbolClass = (SCDisplay) _typeToImages.get(type);
				_currentSymbolClass.setShowOrigData(_showOrigData);
				_currentSymbolClass.setShowOrigDataPoints(_showOrigDataPoints);
				_currentSymbolClass.setShowBreakPoints(_showBreakPoints);
				_currentSymbolClass.setShowFits(_showFits);
				remove(_scrollPane);
				_scrollPane = new JScrollPane(_currentSymbolClass);
				add("Center", _scrollPane);
				// force the component to redraw itself (repaint doesn't work)
				setVisible(false);
				setVisible(true);
			}
		}

		public void toggleShowOrigData() {
			_showOrigData = !_showOrigData;
			_currentSymbolClass.setShowOrigData(_showOrigData);
		}

		public void toggleShowOrigDataPoints() {
			_showOrigDataPoints = !_showOrigDataPoints;
			_currentSymbolClass.setShowOrigDataPoints(_showOrigDataPoints);
		}

		public void toggleShowBreakPoints() {
			_showBreakPoints = !_showBreakPoints;
			_currentSymbolClass.setShowBreakPoints(_showBreakPoints);
		}

		public void toggleShowFits() {
			_showFits = !_showFits;
			_currentSymbolClass.setShowFits(_showFits);
		}
	}

	/**
	 * (this probably should go under diva/sketch/toolbox directory) Accept all
	 * directories and all .sml files.
	 */
	private class SMLFileFilter extends FileFilter {
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			boolean ret = (f.getName().endsWith(".sml")) ? true : false;
			return ret;
		}

		// The description of this filter
		public String getDescription() {
			return "Just training files";
		}
	}

	/**
	 * Given a set of SD objects that are of one class, arrange them in a
	 * GridLayout panel.
	 */
	public static class SCDisplay extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2028305977003590469L;
		
		private SD[] _symbols = null;
		private SD _selectedSymbol = null;
		private String _type = null;

		public SCDisplay(String type, SD[] sds) {
			_type = type;
			_symbols = sds;

			// gui layout
			int col = 4;
			int row = (int) Math.ceil(_symbols.length / col);
			setLayout(new GridLayout(row, col, 5, 5));
			for (int i = 0; i < _symbols.length; i++) {
				add(_symbols[i]);
			}
			setSelectedSymbol(_symbols[0]);
		}

		/**
		 * Return the selected symbol in this class.
		 */
		public SD getSelectedSymbol() {
			return _selectedSymbol;
		}

		/**
		 * Deselect the previous symbol and set the selection to be the
		 * specified symbol.
		 */
		public void setSelectedSymbol(SD sd) {
			if (_selectedSymbol == null) {
				_selectedSymbol = sd;
				_selectedSymbol.select();
			} else if (sd != _selectedSymbol) {
				_selectedSymbol.deselect();
				_selectedSymbol = sd;
				_selectedSymbol.select();
			}
		}

		public int getSymbolCount() {
			return _symbols.length;
		}

		/**
		 * Return the symbols in this class.
		 */
		public SD[] getSymbols() {
			return _symbols;
		}

		public String getType() {
			return _type;
		}

		public void setShowOrigData(boolean val) {
			for (int i = 0; i < _symbols.length; i++) {
				_symbols[i].setShowOrigData(val);
			}
		}

		public void setShowOrigDataPoints(boolean val) {
			for (int i = 0; i < _symbols.length; i++) {
				_symbols[i].setShowOrigDataPoints(val);
			}
		}

		public void setShowFilteredData(boolean val) {
			for (int i = 0; i < _symbols.length; i++) {
				_symbols[i].setShowFilteredData(val);
			}
		}

		public void setShowFilteredDataPoints(boolean val) {
			for (int i = 0; i < _symbols.length; i++) {
				_symbols[i].setShowFilteredDataPoints(val);
			}
		}

		public void setShowBreakPoints(boolean val) {
			for (int i = 0; i < _symbols.length; i++) {
				_symbols[i].setShowBreakPoints(val);
			}
		}

		public void setShowFits(boolean val) {
			for (int i = 0; i < _symbols.length; i++) {
				_symbols[i].setShowFits(val);
			}
		}
	}

	/**
	 * Given a set of strokes, display them on a panel.
	 */
	public static class SD extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2826296905491268426L;
		public final static Border EMPTY_BORDER = BorderFactory
				.createEmptyBorder();
		public final static Border SELECTION_BORDER = BorderFactory
				.createLineBorder(Color.orange, 2);

		public final static int INSETS = 5;
		TimedStroke[] _orig = null;
		TimedStroke[] _strokes = null;

		boolean _showOrigData = true;
		boolean _showOrigDataPoints = true;
		boolean _showFilteredData = false;
		boolean _showFilteredDataPoints = false;
		boolean _showBreakPoints = true;
		boolean _showFits = true;

		boolean _selected = false;
		double _symbolWidth = 0;// does not include insets
		double _symbolHeight = 0;// does not include insets
		boolean _scaled = true;
		// double _scaleFactor = 1.0;
		double _scaleFactor = 1.5;
		FitData _fd = null;

		public SD(SD symbol, int numE, int numL) {
			_fd = Fragmenter.fragmentWithTemplate(symbol._strokes, numE, numL);
			setBackground(Color.white);
			_orig = symbol._orig;
			_strokes = symbol._strokes;
			_selected = symbol._selected;
			_symbolWidth = symbol._symbolWidth;
			_symbolHeight = symbol._symbolHeight;
			_scaled = symbol._scaled;
			_scaleFactor = symbol._scaleFactor;

			setPreferredSize(new Dimension((int) getSymbolWidth() + 3 * INSETS,
					(int) getSymbolHeight() + 3 * INSETS));

		}

		/**
		 * Copy constructor. Make a copy of the data in the specified symbol and
		 * segment this symbol with the given template
		 */
		public SD(SD symbol, String template) {
			_fd = Fragmenter.fragmentWithTemplate(symbol._strokes, template);
			if (_fd != null) {
				System.out.println("Fit err = " + _fd.getTotalFitError());
			} else {
				System.out.println("Null fit data");
			}
			setBackground(Color.white);
			_orig = symbol._orig;
			_strokes = symbol._strokes;
			_selected = symbol._selected;
			_symbolWidth = symbol._symbolWidth;
			_symbolHeight = symbol._symbolHeight;
			_scaled = symbol._scaled;
			_scaleFactor = symbol._scaleFactor;

			setPreferredSize(new Dimension((int) getSymbolWidth() + 3 * INSETS,
					(int) getSymbolHeight() + 3 * INSETS));

		}

		/**
		 * Instantiate a SD that paints the given set of strokes.
		 */
		public SD(TimedStroke[] orig, TimedStroke[] strokes) {
			_orig = orig;
			_strokes = strokes;// scaled
			setBackground(Color.white);
			double xmin = Double.MAX_VALUE;
			double ymin = Double.MAX_VALUE;
			double xmax = Double.MIN_VALUE;
			double ymax = Double.MIN_VALUE;
			for (int i = 0; i < _strokes.length; i++) {
				org.openawt.geom.Rectangle2D bounds = _strokes[i].getBounds2D();
				xmin = Math.min(xmin, bounds.getMinX());
				xmax = Math.max(xmax, bounds.getMaxX());
				ymin = Math.min(ymin, bounds.getMinY());
				ymax = Math.max(ymax, bounds.getMaxY());
			}
			for (int i = 0; i < strokes.length; i++) {
				// left upper starts at (7,7)
				_orig[i].translate(INSETS, INSETS);
				_strokes[i].translate(INSETS, INSETS);
			}
			_symbolWidth = xmax - xmin;
			_symbolHeight = ymax - ymin;
			int width = (int) _symbolWidth + 3 * INSETS;
			int height = (int) _symbolHeight + 3 * INSETS;
			setPreferredSize(new Dimension(width, height));
		}

		public double getSymbolWidth() {
			if (_scaled) {
				return _symbolWidth * _scaleFactor;
			} else {
				return _symbolWidth;
			}
		}

		public double getSymbolHeight() {
			if (_scaled) {
				return _symbolHeight * _scaleFactor;
			} else {
				return _symbolHeight;
			}
		}

		public String getTemplate() {
			return _fd.getTemplate();
		}

		public void select() {
			_selected = true;
			setBorder(SELECTION_BORDER);
		}

		public void deselect() {
			_selected = false;
			setBorder(EMPTY_BORDER);
		}

		public void paint(Graphics g) {
			super.paint(g);
			if (_strokes == null) {
				return;
			}
			if (_showOrigData) {
				if (_fd != null) {
					g.setColor(Color.lightGray);
				} else {
					g.setColor(Color.darkGray);
				}
				for (int i = 0; i < _orig.length; i++) {
					paintStroke(g, _orig[i], _showOrigDataPoints, _scaleFactor);
				}
			}
			if (_showFilteredData) {
				g.setColor(Color.magenta);
				for (int i = 0; i < _strokes.length; i++) {
					paintStroke(g, _strokes[i], _showFilteredDataPoints,
							_scaleFactor);
				}
			}
			if (_fd != null) {
				if (_showBreakPoints) {
					// g.setColor(Color.blue);
					g.setColor(Color.red);
					for (int i = 0; i < _strokes.length; i++) {
						TimedStroke s = _strokes[i];
						int[] bpts = _fd.getBreakpointsOnStroke(i);
						if (bpts != null) {
							for (int j = 0; j < bpts.length; j++) {
								int k = bpts[j];
								double x = s.getX(k);
								double y = s.getY(k);
								if (_scaled) {
									x = (x - INSETS) * _scaleFactor + INSETS;
									y = (y - INSETS) * _scaleFactor + INSETS;
								}
								// g.drawOval((int)x-4,(int)y-4,8,8);
								g.drawOval((int) x - 7, (int) y - 7, 14, 14);
							}
						}
					}
				}
				if (_showFits) {
					// g.setColor(Color.blue);
					g.setColor(Color.black);
					for (int i = 0; i < _fd.getBasisCount(); i++) {
						Basis b = _fd.getBasis(i);
						if (b.getType() == Basis.TYPE_LINE) {
							LineBasis ld = (LineBasis) b;
							org.openawt.geom.Line2D line = ld.getLine();
							double x1 = line.getX1();
							double y1 = line.getY1();
							double x2 = line.getX2();
							double y2 = line.getY2();
							if (_scaled) {
								x1 = (x1 - INSETS) * _scaleFactor + INSETS;
								y1 = (y1 - INSETS) * _scaleFactor + INSETS;
								x2 = (x2 - INSETS) * _scaleFactor + INSETS;
								y2 = (y2 - INSETS) * _scaleFactor + INSETS;
							}
							g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
						} else if (b.getType() == Basis.TYPE_ELLIPSE) {
							EllipseBasis ed = (EllipseBasis) b;
							int n = ed.getNumEllipticalDataPoints();
							double points[][] = ed.getEllipticalPoints();
							for (int k = 1; k < n; k++) {
								double x1 = points[0][k - 1];
								double y1 = points[1][k - 1];
								double x2 = points[0][k];
								double y2 = points[1][k];
								if (_scaled) {
									x1 = (x1 - INSETS) * _scaleFactor + INSETS;
									y1 = (y1 - INSETS) * _scaleFactor + INSETS;
									x2 = (x2 - INSETS) * _scaleFactor + INSETS;
									y2 = (y2 - INSETS) * _scaleFactor + INSETS;
								}
								g.drawLine((int) x1, (int) y1, (int) x2,
										(int) y2);
							}
						}
					}
				}
			}
			Dimension d = getSize();
			g.setColor(Color.black);
			if (_fd != null) {
				g.drawString(getTemplate(), 2, (int) d.getHeight() - 10);
			}
		}

		public void paintStroke(Graphics g, TimedStroke s, boolean showPoints,
				double scaleFactor) {
			for (int j = 0; j < s.getVertexCount() - 1; j++) {
				double x1 = s.getX(j);
				double y1 = s.getY(j);
				double x2 = s.getX(j + 1);
				double y2 = s.getY(j + 1);
				if (scaleFactor != 1) {// tranlate to origin, scale, and
										// tranlate back
					x1 = (x1 - INSETS) * scaleFactor + INSETS;
					y1 = (y1 - INSETS) * scaleFactor + INSETS;
					x2 = (x2 - INSETS) * scaleFactor + INSETS;
					y2 = (y2 - INSETS) * scaleFactor + INSETS;
				}
				if (showPoints) {
					if (j == 0) {// start point, bigger
						g.drawOval((int) x1 - 4, (int) y1 - 4, 8, 8);
					} else {
						g.fillOval((int) x1 - 2, (int) y1 - 2, 3, 3);
					}
				}
				g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				if ((j == s.getVertexCount() - 2) && showPoints) {// last loop
					// g.fillOval((int)x2-1,(int)y2-1,2,2);
					g.drawOval((int) x2 - 4, (int) y2 - 4, 8, 8);// end point,
																	// bigger
				}
			}
		}

		public boolean getShowOrigData() {
			return _showOrigData;
		}

		public boolean getShowOrigDataPoints() {
			return _showOrigDataPoints;
		}

		public boolean getShowFilteredData() {
			return _showFilteredData;
		}

		public boolean getShowFilteredDataPoints() {
			return _showFilteredDataPoints;
		}

		public boolean getShowBreakPoints() {
			return _showBreakPoints;
		}

		public boolean getShowFits() {
			return _showFits;
		}

		public void setShowOrigData(boolean val) {
			_showOrigData = val;
		}

		public void setShowOrigDataPoints(boolean val) {
			_showOrigDataPoints = val;
		}

		public void setShowFilteredData(boolean val) {
			_showFilteredData = val;
		}

		public void setShowFilteredDataPoints(boolean val) {
			_showFilteredDataPoints = val;
		}

		public void setShowBreakPoints(boolean val) {
			_showBreakPoints = val;
		}

		public void setShowFits(boolean val) {
			_showFits = val;
		}

		public void toggleShowOrigData() {
			_showOrigData = !_showOrigData;
		}

		public void toggleShowOrigDataPoints() {
			_showOrigDataPoints = !_showOrigDataPoints;
		}

		public void toggleShowFilteredData() {
			_showFilteredData = !_showFilteredData;
		}

		public void toggleShowFilteredDataPoints() {
			_showFilteredDataPoints = !_showFilteredDataPoints;
		}

		public void toggleShowBreakPoints() {
			_showBreakPoints = !_showBreakPoints;
		}

		public void toggleShowFits() {
			_showFits = !_showFits;
		}
	}
}
