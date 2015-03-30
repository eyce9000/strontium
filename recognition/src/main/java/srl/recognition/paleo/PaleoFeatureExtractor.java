/**
 * PaleoFeatureExtractor.java
 * 
 * Revision History:<br>
 * Jan 15, 2009 bpaulson - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
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
package srl.recognition.paleo;

import srl.core.exception.InvalidParametersException;
import srl.core.sketch.Point;
import srl.core.sketch.Segmentation;
import srl.core.sketch.Stroke;
import srl.patternrec.classifiers.core.FVector;
import srl.recognition.paleo.cali.CALIFeatures;
import srl.recognition.rubine.RubineStroke;
import srl.segmentation.combination.PolylineCombinationSegmenter;
import srl.segmentation.paleo.PaleoSegmenter;
import srl.segmentation.paleo.WaveSegmenter;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Class used to extract out the geometric "features" used by paleo
 * 
 * @author bpaulson
 */
public class PaleoFeatureExtractor {

	/**
	 * Feature values of the stroke we are recognizing
	 */
	protected StrokeFeatures m_features;

	/**
	 * Line fit of the stroke we are recognizing
	 */
	protected LineFit m_lineFit;

	/**
	 * Curve fit of the stroke we are recognizing
	 */
	protected CurveFit m_curveFit;

	/**
	 * Arc fit of the stroke we are recognizing
	 */
	protected ArcFit m_arcFit;

	/**
	 * Circle fit of the stroke we are recognizing
	 */
	protected CircleFit m_circleFit;

	/**
	 * Ellipse fit of the stroke we are recognizing
	 */
	protected EllipseFit m_ellipseFit;

	/**
	 * Helix fit of the stroke we are recognizing
	 */
	protected HelixFit m_helixFit;

	/**
	 * Spiral fit of the stroke we are recognizing
	 */
	protected SpiralFit m_spiralFit;

	/**
	 * Polyline fit of the stroke we are recognizing
	 */
	protected PolylineFit m_polylineFit;

	/**
	 * Complex fit of the stroke we are recognizing
	 */
	protected ComplexFit m_complexFit;

	/**
	 * Polygon fit of the stroke we are recognizing
	 */
	protected PolygonFit m_polygonFit;

	/**
	 * Arrow fit of the stroke we are recognizing
	 */
	protected ArrowFit m_arrowFit;

	/**
	 * Rectangle fit of the stroke we are recognizing
	 */
	protected RectangleFit m_rectangleFit;

	/**
	 * Square fit of the stroke we are recognizing
	 */
	protected SquareFit m_squareFit;

	/**
	 * Diamond fit of the stroke we are recognizing
	 */
	protected DiamondFit m_diamondFit;

	/**
	 * Dot fit of the stroke we are recognizing
	 */
	protected DotFit m_dotFit;

	/**
	 * Wave fit of the stroke we are recognizing
	 */
	protected WaveFit m_waveFit;

	/**
	 * Gull fit of the stroke we are recognizing
	 */
	protected GullFit m_gullFit;

	/**
	 * Blob fit of the stroke we are recognizing
	 */
	protected BlobFit m_blobFit;

	/**
	 * Infinity fit of the stroke we are recognizing
	 */
	protected InfinityFit m_infinityFit;

	/**
	 * NBC fit of the stroke we are recognizing
	 */
	protected NBCFit m_nbcFit;

	/**
	 * Configuration file for the recognizer
	 */
	protected PaleoConfig m_config;

	/**
	 * Main corner finding interpretation
	 */
	protected Segmentation m_segmentation;

	/**
	 * Wave segmentation of stroke
	 */
	protected Segmentation m_waveSegmentation;

	/**
	 * Feature vector
	 */
	protected FVector m_featureVector;

	/**
	 * List of possible class labels (a dataset must have been previously built
	 * for this to be populated)
	 */
	protected static FastVector m_classLabels;

	/**
	 * Constructor for feature extractor
	 * 
	 * @param features
	 *            stroke features
	 * @param config
	 *            paleo configuration file
	 */
	public PaleoFeatureExtractor(StrokeFeatures features, PaleoConfig config) {
		m_features = features;
		m_config = config;
	}

	/**
	 * Get a feature vector for the stroke representing the paleo features
	 * 
	 * @return feature vector
	 * @throws Exception
	 */
	public FVector getFeatureVector() throws Exception {
		if (m_featureVector == null)
			computeFeatureVector();
		return m_featureVector;
	}

	/**
	 * Get the class labels used by the feature extractor. This will be
	 * populated iff a dataset was created using getNewDataset()
	 * 
	 * @return class labels
	 */
	public FastVector getClassLabels() {
		if (m_classLabels == null) {
			m_classLabels = new FastVector();
			for (int j = 0; j < m_config.getShapesTurnedOn().size(); j++)
				m_classLabels.addElement(m_config.getShapesTurnedOn().get(j));
		}
		return m_classLabels;
	}
	
	public StrokeFeatures getStrokeFeatures(){
		return this.m_features;
	}

	/**
	 * Builds and returns a new (and empty) dataset that contains the attributes
	 * of this extractor to be used by WEKA
	 * 
	 * @return new empty dataset
	 * @throws Exception
	 */
	public Instances getNewDataset() throws Exception {
		Instances dataset;
		FVector fv = getFeatureVector();
		m_classLabels = new FastVector();
		for (int j = 0; j < m_config.getShapesTurnedOn().size(); j++)
			m_classLabels.addElement(m_config.getShapesTurnedOn().get(j));
		FastVector fast = new FastVector(fv.getNumFeatures());
		for (int j = 0; j < fv.getNumFeatures(); j++)
			fast.addElement(new Attribute("a" + j));
		fast.addElement(new Attribute("label", m_classLabels));
		dataset = new Instances("PaleoTrain", fast, 0);
		return dataset;
	}

	/**
	 * Returns a WEKA Instance version of feature vector
	 * 
	 * @param strokeLabel
	 *            the label of the stroke
	 * @return WEKA instance
	 * @throws Exception
	 */
	public Instance getInstance(String strokeLabel) throws Exception {
		FVector fv = getFeatureVector();
		String label = null;

		// convert feature vector to instance
		if (strokeLabel != null) {
			label = strokeLabel.trim();
			if (strokeLabel.contains("(")) {
				label = strokeLabel.substring(0, strokeLabel.indexOf('('));
			}
			label = label.trim();
		}
		double vals[] = new double[fv.getNumFeatures() + 1];
		for (int j = 0; j < fv.getNumFeatures(); j++) {
			vals[j] = fv.getFeature(j);
		}

		// label should be null when creating an instance to test
		if (strokeLabel == null)
			vals[vals.length - 1] = 0.0;
		else {
			int classLabel = m_classLabels.indexOf(label);
			if (classLabel < 0 || classLabel > m_classLabels.size() - 1)
				throw new Exception("bad label: " + label);
			vals[vals.length - 1] = classLabel;
		}
		return new Instance(1.0, vals);
	}

	/**
	 * Computes the features to put into the feature vector
	 * 
	 * @throws Exception
	 */
	public void computeFeatureVector() throws Exception {
		m_featureVector = new FVector();

		// *** add common features (from stroke features) ***
		m_featureVector.add(m_features.getAvgCornerStrokeDistance(),
				"AvgCornerStrokeDist");
		m_featureVector.add(m_features.getAvgCurvature(), "AvgCurv");
		m_featureVector.add(m_features.getBestFitDirGraphError(),
				"BestFitDirGraphErr");
		m_featureVector.add(m_features.getDCR(), "DCR"); // selected
		m_featureVector.add(
				m_features.getDistanceBetweenFarthestCornerAndStroke(),
				"DistBTFarthestCornerAndStroke");
		m_featureVector.add(m_features.getEndptStrokeLengthRatio(),
				"EndptSLRatio"); // selected
		m_featureVector.add(m_features.getMajorAxisAngle(), "MajorAxisAngle");
		m_featureVector.add(m_features.getMajorAxisLength(), "MajorAxisLength");
		m_featureVector.add(m_features.getMaxCornerStrokeDistance(),
				"MaxCornerStrokeDist");
		m_featureVector.add(m_features.getMaxCurv(), "MaxCurv");
		m_featureVector.add(m_features.getMaxCurvToAvgCurvRatio(),
				"MaxCurvToAvgCurvRatio"); // selected
		m_featureVector.add(m_features.getMinCornerStrokeDistance(),
				"MinCornerStrokeDist");
		m_featureVector.add(m_features.getNDDE(), "NDDE"); // selected
		m_featureVector.add(m_features.getNumRevolutions(), "NumRevs"); // selected
		m_featureVector.add(m_features.getPctDirWindowPassed(),
				"PctDirWindowPassed");
		m_featureVector.add(m_features.getSlopeDirGraph(), "SlopeDirGraph");
		m_featureVector.add(m_features.getStdDevCornerStrokeDistance(),
				"StdDevCornerStrokeDist");
		m_featureVector.add(m_features.getStrokeLength(), "StrokeLength"); // selected

		// *** add line features ***
		if (m_config.isLineTestOn()) {
			calcLineFit();
			m_featureVector.add(m_lineFit.getError(), "LineFA");
			m_featureVector.add(
					m_lineFit.getLSQE() / m_features.getStrokeLength(),
					"LineLSQE");
		}

		// *** add arc features ***
		if (m_config.isArcTestOn()) {
			calcArcFit();
			m_featureVector.add(m_arcFit.getError(), "ArcFA");
			m_featureVector.add(m_arcFit.getRadius(), "ArcRadius");
			m_featureVector.add(m_arcFit.getArcArea(), "ArcArea");

			if (m_config.getHeuristics().ARC_DOWN) { // only needed for arc
				// heuristic
				m_featureVector.add(m_arcFit.getAngle(), "ArcAngle");
			}
		}

		// *** add curve features ***
		if (m_config.isCurveTestOn()) {
			calcCurveFit();
			m_featureVector.add(m_curveFit.getError(), "CurveError"); // selected
		}

		// *** add polyline features ***
		if (m_config.isPolylineTestOn()) {
			calcPolylineFit();
			m_featureVector.add(m_polylineFit.getError(), "PolylineFA");
			m_featureVector.add(m_polylineFit.getNumSubStrokes(),
					"PolylineNumSS"); // selected
			m_featureVector.add(m_polylineFit.getLSQE(), "PolylineLSQE");
			m_featureVector.add(m_polylineFit.getPctPassed(),
					"PolylinePctPassed");
		}

		// *** add ellipse features ***
		if (m_config.isEllipseTestOn()) {
			calcEllipseFit();
			m_featureVector.add(m_ellipseFit.getError(), "EllipseFA");
		}

		// *** add circle features ***
		calcCircleFit();
		m_featureVector.add(m_circleFit.getError(), "CircleFA");
		m_featureVector.add(m_circleFit.getAxisRatio(), "CircleAxisRatio");

		// *** add spiral features ***
		if (m_config.isSpiralTestOn()) {
			calcSpiralFit();
			m_featureVector.add(m_spiralFit.getError(), "SpiralErr"); // selected
			m_featureVector.add(m_spiralFit.getPctRadiusTestPassed(),
					"SpiralPctRadiusTestPass");
			m_featureVector.add(m_spiralFit.getAvgRadBBRadRatio(),
					"SpiralAvgRadBBRatio"); // selected
			m_featureVector.add(
					m_spiralFit.getMaxDistanceToCenterRadiusRatio(),
					"SpiralMaxDistToCenterRadRatio");
		}

		// *** add helix features ***
		if (m_config.isHelixTestOn()) {
			calcHelixFit();
		}

		// *** add polygon features ***
		if (m_config.isPolygonTestOn()) {
			calcPolygonFit();
		}

		// *** add arrow features ***
		if (m_config.isArrowTestOn()) {
			calcArrowFit();
			m_featureVector.add(m_arrowFit.getHeadDistance(), "ArrowHeadDist");
			m_featureVector
					.add(m_arrowFit.getLastTwoDiff(), "ArrowLastTwoDiff");
			m_featureVector.add(m_arrowFit.getNumIntersect(),
					"ArrowNumIntersect");
			// TODO: add other types of arrows
		}

		// *** add rectangle features ***
		if (m_config.isRectangleTestOn()) {
			calcRectangleFit();
			m_featureVector.add(m_rectangleFit.getError(), "RectFA");
			m_featureVector.add(m_rectangleFit.getMajorAxisBBDiagRatio(),
					"RectMajorAxisBBDiagRatio");
			m_featureVector.add(m_rectangleFit.getPerimeterStrokeLengthRatio(),
					"RectPerimSLRatio");
			m_featureVector.add(m_segmentation.getSegmentedStrokes().size(),
					"NumCorners_1");
			m_featureVector.add(m_features.getStrokeLengthPerimRatio(),
					"RectSLPerimRatio");
		}

		// *** add square features ***
		if (m_config.isSquareTestOn()) {
			calcSquareFit();
			m_featureVector.add(m_squareFit.getWidthHeightRatio(),
					"SquareWidthHeightRatio");
		}

		// *** add diamond features ***
		if (m_config.isDiamondTestOn()) {
			calcDiamondFit();
			m_featureVector.add(m_diamondFit.getError(), "DiamondFA");
			m_featureVector.add(m_diamondFit.getPerimeterStrokeLengthRatio(),
					"DiamondPerimSLRatio");
			m_featureVector.add(m_diamondFit.getMajorAxisBBDiagRatio(),
					"DiamondMajorAxisBBDiagRatio");
			m_featureVector.add(m_diamondFit.getWidthHeightRatio(),
					"DiamondWidthHeightRatio");
		}

		// *** add dot features ***
		if (m_config.isDotTestOn()) {
			calcDotFit();
			m_featureVector.add(m_dotFit.getDensity(), "DotDensity");
			m_featureVector.add(m_dotFit.getHeightWidthRatio(),
					"DotHeightWidthRatio");
		}

		// *** add wave features ***
		if (m_config.isWaveTestOn()) {
			calcWaveFit();
			m_featureVector.add(
					m_waveSegmentation.getSegmentedStrokes().size(),
					"WaveSegmentSize");
			m_featureVector.add(m_waveFit.getPctSlopePassed(),
					"WavePctSlopePass");
			m_featureVector.add(m_waveFit.getSmallLargeRatio(),
					"WaveSmallLargeRatio");
		}

		// *** add gull features ***
		if (m_config.isGullTestOn()) {
			calcGullFit();
			m_featureVector.add(m_gullFit.getSmallSumRatio(),
					"GullSmallSumRatio");
			m_featureVector.add(m_gullFit.getAngle(), "GullAngle");
			m_featureVector.add(m_gullFit.getPctHorizontalAlignmentPass(),
					"GullPctHorizAlignPass");
			m_featureVector.add(m_gullFit.getSlopeAvg(), "GullSlopeAvg");
			m_featureVector.add(m_gullFit.getPctSlopeTest(), "GullPctSlopTest");
		}

		// *** add blob features ***
		if (m_config.isBlobTestOn()) {
			calcBlobFit();
		}

		// *** add infinity features ***
		if (m_config.isInfinityTestOn()) {
			calcInfinityFit();
		}

		// *** add nbc features ***
		if (m_config.isNBCTestOn()) {
			calcNBCFit();
			m_featureVector.add(m_nbcFit.getSizeRatio(), "NBCSizeRatio");
			m_featureVector.add(m_nbcFit.getDotDensity(), "NBCDotDensity");
			m_featureVector.add(m_nbcFit.getDotRevs(), "NBCDotRevs");
		}

		// *** add complex features ***
		if (m_config.isComplexTestOn()) {
			 calcComplexFit();
			 m_featureVector.add(m_complexFit.getSubFits().size(),"ComplexSubfits");
			 m_featureVector.add(m_complexFit.numPrimitives(),"ComplexPrimitives");
			 m_featureVector.add(m_complexFit.percentLines(),"ComplexPctLines");
			 m_featureVector.add(calcComplexScore(),"ComplexScore");
		}

		/* CALI Features */

		CALIFeatures cali = new CALIFeatures(m_features.getOrigStroke());
		m_featureVector.add(cali.Ach_Abb(), "CALI_ACH_ABB"); // 30
		m_featureVector.add(cali.Ach_Aer(), "CALI_ACH_AER"); // 40
		m_featureVector.add(cali.Alq_Ach(), "CALI_ALQ_ACH"); // 100
		m_featureVector.add(cali.Alq_Aer(), "CALI_ALQ_AER"); // 70
		m_featureVector.add(cali.Alt_Abb(), "CALI_ALT_ABB"); // 30
		m_featureVector.add(cali.Alt_Ach(), "CALI_ALT_ACH"); // 90
		m_featureVector.add(cali.Alt_Aer(), "CALI_ALT_AER"); // 60
		m_featureVector.add(cali.Alt_Alq(), "CALI_ALT_ALQ"); // 50
		m_featureVector.add(cali.Hbb_Wbb(), "CALI_HBB_WBB"); // 0
		m_featureVector.add(cali.Her_Wer(), "CALI_HER_WER"); // 100
		m_featureVector.add(cali.Hm_Wbb(), "CALI_HM_WBB"); // 60
		m_featureVector.add(cali.Hollowness(), "CALI_HOLLOWNESS"); // 20
		m_featureVector.add(cali.Pch2_Ach(), "CALI_PCH2_ACH"); // 80
		m_featureVector.add(cali.Pch_Ns_Tl(), "CALI_PCH_NS_TL"); // 100
		m_featureVector.add(cali.Pch_Pbb(), "CALI_PCH_PBB"); // 10
		m_featureVector.add(cali.Pch_Per(), "CALI_PCH_PER"); // 90
		m_featureVector.add(cali.Plq_Pch(), "CALI_PLQ_PCH"); // 0
		m_featureVector.add(cali.Plq_Per(), "CALI_PLQ_PER"); // 10
		m_featureVector.add(cali.Plt_Pbb(), "CALI_PLT_PBB"); // 30
		m_featureVector.add(cali.Plt_Pch(), "CALI_PLT_PCH"); // 60
		m_featureVector.add(cali.Plt_Per(), "CALI_PLT_PER"); // 20
		m_featureVector.add(cali.Plt_Plq(), "CALI_PLT_PLQ"); // 50
		m_featureVector.add(cali.Tl_Pch(), "CALI_TL_PCH"); // 50
		m_featureVector.add(cali.Vm_Hbb(), "CALI_VM_HBB"); // 60

		

		/* Rubine Features */
		RubineStroke rs = new RubineStroke(m_features.getOrigStroke(),
				RubineStroke.FeatureSet.Long);
		for (int i = 0; i < rs.getFeatures().size(); i++)
			m_featureVector.add(rs.getFeatures().get(i), "Long" + (i + 1));
	}

	

	/**
	 * Calculate line fit
	 */
	protected void calcLineFit() {
		if (m_lineFit == null)
			m_lineFit = new LineFit(m_features, true);
	}

	/**
	 * Calculate arc fit
	 */
	protected void calcArcFit() {
		if (m_arcFit == null)
			m_arcFit = new ArcFit(m_features, m_config);
	}

	/**
	 * Calculate curve fit
	 */
	protected void calcCurveFit() {
		if (m_curveFit == null)
			m_curveFit = new CurveFit(m_features);
	}

	/**
	 * Calculate polyline fit
	 */
	protected void calcPolylineFit() {
		if (m_polylineFit == null) {
			if (m_segmentation == null) {
				if (m_config.getHeuristics().MULTI_CF) {
					try {
						PolylineCombinationSegmenter seg = new PolylineCombinationSegmenter(
								m_config.getHeuristics().FILTER_DIR_GRAPH);
						seg.setStroke(m_features.getOrigStroke());
						m_segmentation = seg.getSegmentations().get(0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					m_segmentation = new PaleoSegmenter(m_features)
							.getSegmentations().get(0);
				}
			}
			m_polylineFit = new PolylineFit(m_features, m_segmentation,
					m_config);
		}
	}

	/**
	 * Calculate ellipse fit
	 */
	protected void calcEllipseFit() {
		if (m_ellipseFit == null)
			m_ellipseFit = new EllipseFit(m_features);
	}

	/**
	 * Calculate circle fit
	 */
	protected void calcCircleFit() {
		if (m_circleFit == null) {
			if (m_ellipseFit == null)
				calcEllipseFit();
			m_circleFit = new CircleFit(m_features, (EllipseFit) m_ellipseFit);
		}
	}

	/**
	 * Calculate spiral fit
	 */
	protected void calcSpiralFit() {
		if (m_spiralFit == null) {
			if (m_circleFit == null)
				calcCircleFit();
			m_spiralFit = new SpiralFit(m_features, (CircleFit) m_circleFit);
		}
	}

	/**
	 * Calculate helix fit
	 */
	protected void calcHelixFit() {
		if (m_helixFit == null) {
			if (m_spiralFit == null)
				calcSpiralFit();
			m_helixFit = new HelixFit(m_features, (SpiralFit) m_spiralFit);
		}
	}

	/**
	 * Calculate polygon fit
	 */
	protected void calcPolygonFit() {
		if (m_polygonFit == null) {
			if (m_polylineFit == null)
				calcPolylineFit();
			m_polygonFit = new PolygonFit(m_features,
					(PolylineFit) m_polylineFit);
		}
	}

	/**
	 * Calculate arrow fit
	 */
	protected void calcArrowFit() {
		if (m_arrowFit == null) {
			if (m_segmentation == null) {
				if (m_config.getHeuristics().MULTI_CF) {
					try {
						PolylineCombinationSegmenter seg = new PolylineCombinationSegmenter(
								m_config.getHeuristics().FILTER_DIR_GRAPH);
						seg.setStroke(m_features.getOrigStroke());
						m_segmentation = seg.getSegmentations().get(0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					m_segmentation = new PaleoSegmenter(m_features)
							.getSegmentations().get(0);
				}
			}
			m_arrowFit = new ArrowFit(m_features, m_segmentation);
			if (m_features.getOrigStroke().getSegmentations().size() > 0) {
				Segmentation seg = m_features.getOrigStroke()
						.getSegmentations().get(0);
				for (Stroke s : seg.getSegmentedStrokes()) {
					m_arrowFit.getShape().getShapes()
							.add(LineFit.getLineFit(s));
				}
			}
		}
	}

	/**
	 * Calculate rectangle fit
	 */
	protected void calcRectangleFit() {
		if (m_rectangleFit == null) {
			if (m_ellipseFit == null)
				calcEllipseFit();
			if (m_segmentation == null) {
				if (m_config.getHeuristics().MULTI_CF) {
					try {
						PolylineCombinationSegmenter seg = new PolylineCombinationSegmenter(
								m_config.getHeuristics().FILTER_DIR_GRAPH);
						seg.setStroke(m_features.getOrigStroke());
						m_segmentation = seg.getSegmentations().get(0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					m_segmentation = new PaleoSegmenter(m_features)
							.getSegmentations().get(0);
				}
			}
			m_rectangleFit = new RectangleFit(m_features,
					(EllipseFit) m_ellipseFit, m_segmentation);
		}
	}

	/**
	 * Calculate square fit
	 */
	protected void calcSquareFit() {
		if (m_squareFit == null) {
			if (m_rectangleFit == null)
				calcRectangleFit();
			m_squareFit = new SquareFit(m_features,
					(RectangleFit) m_rectangleFit);
		}
	}

	/**
	 * Calculate diamond fit
	 */
	protected void calcDiamondFit() {
		if (m_diamondFit == null) {
			if (m_segmentation == null) {
				if (m_config.getHeuristics().MULTI_CF) {
					try {
						PolylineCombinationSegmenter seg = new PolylineCombinationSegmenter(
								m_config.getHeuristics().FILTER_DIR_GRAPH);
						seg.setStroke(m_features.getOrigStroke());
						m_segmentation = seg.getSegmentations().get(0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					m_segmentation = new PaleoSegmenter(m_features)
							.getSegmentations().get(0);
				}
			}
			m_diamondFit = new DiamondFit(m_features, m_config, m_segmentation);
		}
	}

	/**
	 * Calculate dot fit
	 */
	protected void calcDotFit() {
		if (m_dotFit == null)
			m_dotFit = new DotFit(m_features);
	}

	/**
	 * Calculate wave fit
	 */
	protected void calcWaveFit() {
		if (m_waveFit == null) {
			if (m_waveSegmentation == null) {
				WaveSegmenter waveSeg = new WaveSegmenter(m_features);
				try {
					m_waveSegmentation = waveSeg.getSegmentations().get(0);
				} catch (InvalidParametersException e) {
				}
			}
			m_waveFit = new WaveFit(m_features, m_waveSegmentation);
		}
	}

	/**
	 * Calculate gull fit
	 */
	protected void calcGullFit() {
		if (m_gullFit == null) {
			if (m_waveSegmentation == null) {
				WaveSegmenter waveSeg = new WaveSegmenter(m_features);
				try {
					m_waveSegmentation = waveSeg.getSegmentations().get(0);
				} catch (InvalidParametersException e) {
				}
			}
			if (m_polylineFit == null)
				calcPolylineFit();
			m_gullFit = new GullFit(m_features, m_waveSegmentation,
					m_polylineFit, m_config);
		}
	}

	/**
	 * Calculate blob fit
	 */
	protected void calcBlobFit() {
		if (m_blobFit == null)
			m_blobFit = new BlobFit(m_features);
	}

	/**
	 * Calculate infinity fit
	 */
	protected void calcInfinityFit() {
		if (m_infinityFit == null)
			m_infinityFit = new InfinityFit(m_features);
	}

	/**
	 * Calculate NBC fit
	 */
	protected void calcNBCFit() {
		if (m_nbcFit == null) {
			if (m_waveSegmentation == null) {
				WaveSegmenter waveSeg = new WaveSegmenter(m_features);
				try {
					m_waveSegmentation = waveSeg.getSegmentations().get(0);
				} catch (InvalidParametersException e) {
				}
			}
			m_nbcFit = new NBCFit(m_features, m_waveSegmentation);
		}
	}

	/**
	 * Calculate complex fit
	 */
	protected void calcComplexFit() {
		if (m_complexFit == null)
			m_complexFit = new ComplexFit(m_features, m_config);
	}

	/**
	 * Calculates the complex interpretation score
	 */
	protected double calcComplexScore() {
		double m_complexScore = 0;
		for (int i = 0; i < ((ComplexFit) m_complexFit).getSubFits().size(); i++) {
			Fit f = ((ComplexFit) m_complexFit).getSubFits().get(i);
			if (f instanceof LineFit)
				m_complexScore += 1;
			else if (f instanceof ArcFit)
				m_complexScore += 2;
			else if (f instanceof CurveFit)
				m_complexScore += 4;
			else if (f instanceof SpiralFit)
				m_complexScore += 5;
			else if (f instanceof HelixFit)
				m_complexScore += 5;
			else if (f instanceof EllipseFit)
				m_complexScore += 3;
			else if (f instanceof CircleFit)
				m_complexScore += 3;
			else if (f instanceof PolylineFit)
				m_complexScore += ((PolylineFit) f).getSubStrokes().size();
			else if (f instanceof PolygonFit)
				m_complexScore += ((PolygonFit) f).getSubStrokes().size();
			else if (f instanceof RectangleFit || f instanceof SquareFit
					|| f instanceof DiamondFit)
				m_complexScore += 4;
			else if (f instanceof DotFit)
				m_complexScore += 5;
			else
				m_complexScore += 5;
		}
		return m_complexScore;
	}

	/**
	 * Set the fits from an already computed original recognizer (avoids
	 * recomputation)
	 * 
	 * @param paleo
	 *            original paleo recognizer
	 */
	public void setFits(OrigPaleoSketchRecognizer paleo) {
		if (paleo.getArcFitNoCalc() instanceof ArcFit)
			m_arcFit = (ArcFit) paleo.m_arcFit;
		if (paleo.getArrowFitNoCalc() instanceof ArrowFit)
			m_arrowFit = (ArrowFit) paleo.m_arrowFit;
		if (paleo.getBlobFitNoCalc() instanceof BlobFit)
			m_blobFit = (BlobFit) paleo.m_blobFit;
		if (paleo.getCircleFitNoCalc() instanceof CircleFit)
			m_circleFit = (CircleFit) paleo.m_circleFit;
		if (paleo.getComplexFitNoCalc() instanceof ComplexFit)
			m_complexFit = (ComplexFit) paleo.m_complexFit;
		if (paleo.getCurveFitNoCalc() instanceof CurveFit)
			m_curveFit = (CurveFit) paleo.m_curveFit;
		if (paleo.getDiamondFitNoCalc() instanceof DiamondFit)
			m_diamondFit = (DiamondFit) paleo.m_diamondFit;
		if (paleo.getDotFitNoCalc() instanceof DotFit)
			m_dotFit = (DotFit) paleo.m_dotFit;
		if (paleo.getEllipseFitNoCalc() instanceof EllipseFit)
			m_ellipseFit = (EllipseFit) paleo.m_ellipseFit;
		if (paleo.getGullFitNoCalc() instanceof GullFit)
			m_gullFit = (GullFit) paleo.m_gullFit;
		if (paleo.getHelixFitNoCalc() instanceof HelixFit)
			m_helixFit = (HelixFit) paleo.m_helixFit;
		if (paleo.getInfinityFitNoCalc() instanceof InfinityFit)
			m_infinityFit = (InfinityFit) paleo.m_infinityFit;
		if (paleo.getLineFitNoCalc() instanceof LineFit)
			m_lineFit = (LineFit) paleo.m_lineFit;
		if (paleo.getNBCFitNoCalc() instanceof NBCFit)
			m_nbcFit = (NBCFit) paleo.m_nbcFit;
		if (paleo.getPolygonFitNoCalc() instanceof PolygonFit)
			m_polygonFit = (PolygonFit) paleo.m_polygonFit;
		if (paleo.getPolylineFitNoCalc() instanceof PolylineFit)
			m_polylineFit = (PolylineFit) paleo.m_polylineFit;
		if (paleo.getRectangleFitNoCalc() instanceof RectangleFit)
			m_rectangleFit = (RectangleFit) paleo.m_rectangleFit;
		if (paleo.getSpiralFitNoCalc() instanceof SpiralFit)
			m_spiralFit = (SpiralFit) paleo.m_spiralFit;
		if (paleo.getSquareFitNoCalc() instanceof SquareFit)
			m_squareFit = (SquareFit) paleo.m_squareFit;
		if (paleo.getWaveFitNoCalc() instanceof WaveFit)
			m_waveFit = (WaveFit) paleo.m_waveFit;
		m_segmentation = paleo.getSegmentation();
		m_waveSegmentation = paleo.getWaveSegmentation();
	}

	/**
	 * Get a fit by its string name
	 * 
	 * @param fitName
	 *            name of fit to get
	 * @return shape fit
	 */
	public Fit getFit(String fitName) {
		if (fitName.compareToIgnoreCase(Fit.ARC) == 0)
			return m_arcFit;
		else if (fitName.compareToIgnoreCase(Fit.ARROW) == 0)
			return m_arrowFit;
		else if (fitName.compareToIgnoreCase(Fit.BLOB) == 0)
			return m_blobFit;
		else if (fitName.compareToIgnoreCase(Fit.CIRCLE) == 0)
			return m_circleFit;
		else if (fitName.startsWith(Fit.COMPLEX))
			return m_complexFit;
		else if (fitName.compareToIgnoreCase(Fit.CURVE) == 0)
			return m_curveFit;
		else if (fitName.compareToIgnoreCase(Fit.DIAMOND) == 0)
			return m_diamondFit;
		else if (fitName.compareToIgnoreCase(Fit.DOT) == 0)
			return m_dotFit;
		else if (fitName.compareToIgnoreCase(Fit.ELLIPSE) == 0)
			return m_ellipseFit;
		else if (fitName.compareToIgnoreCase(Fit.GULL) == 0)
			return m_gullFit;
		else if (fitName.compareToIgnoreCase(Fit.HELIX) == 0)
			return m_helixFit;
		else if (fitName.compareToIgnoreCase(Fit.INFINITY) == 0)
			return m_infinityFit;
		else if (fitName.compareToIgnoreCase(Fit.LINE) == 0)
			return m_lineFit;
		else if (fitName.compareToIgnoreCase(Fit.NBC) == 0)
			return m_nbcFit;
		else if (fitName.startsWith(Fit.POLYGON))
			return m_polygonFit;
		else if (fitName.startsWith(Fit.POLYLINE))
			return m_polylineFit;
		else if (fitName.compareToIgnoreCase(Fit.RECTANGLE) == 0)
			return m_rectangleFit;
		else if (fitName.compareToIgnoreCase(Fit.SPIRAL) == 0)
			return m_spiralFit;
		else if (fitName.compareToIgnoreCase(Fit.SQUARE) == 0)
			return m_squareFit;
		else if (fitName.compareToIgnoreCase(Fit.WAVE) == 0)
			return m_waveFit;
		else
			return new NullFit();
	}
}
