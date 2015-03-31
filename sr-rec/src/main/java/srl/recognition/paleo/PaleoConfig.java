/**
 * PaleoConfig.java
 * 
 * Revision History:<br>
 * Jun 30, 2008 bpaulson - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for PaleoSketch - denotes what tests should be run when
 * recognize() is called
 * 
 * @author bpaulson
 */
public class PaleoConfig implements Cloneable,Serializable {
	public static enum Option{
		Line,
		Arc,
		Ellipse,
		Circle,
		Curve,
		Helix,
		Spiral,
		Arrow,
		Complex,
		Polyline,
		Polygon,
		Rectangle,
		Square,
		Diamond,
		Dot,
		Wave,
		Gull,
		Blob,
		Infinity
	}
	/**
	 * Line test on or off
	 */
	protected boolean m_line = true;

	/**
	 * Arc test on or off
	 */
	protected boolean m_arc = true;

	/**
	 * Ellipse test on or off
	 */
	protected boolean m_ellipse = true;

	/**
	 * Circle test on or off
	 */
	protected boolean m_circle = true;

	/**
	 * Curve test on or off
	 */
	protected boolean m_curve = true;

	/**
	 * Helix test on or off
	 */
	protected boolean m_helix = true;

	/**
	 * Spiral test on or off
	 */
	protected boolean m_spiral = true;

	/**
	 * Arrow test on or off
	 */
	protected boolean m_arrow = true;

	/**
	 * Complex test on or off
	 */
	protected boolean m_complex = true;

	/**
	 * Polyline test on or off
	 */
	protected boolean m_polyline = true;

	/**
	 * Polygon test on or off
	 */
	protected boolean m_polygon = true;

	/**
	 * Rectangle test on or off
	 */
	protected boolean m_rectangle = true;

	/**
	 * Square test on or off
	 */
	protected boolean m_square = true;

	/**
	 * Diamond test on or off
	 */
	protected boolean m_diamond = true;

	/**
	 * Dot test on or off
	 */
	protected boolean m_dot = true;

	/**
	 * Wave test on or off
	 */
	protected boolean m_wave = true;

	/**
	 * Gull test on or off
	 */
	protected boolean m_gull = true;

	/**
	 * Blob test on or off
	 */
	protected boolean m_blob = true;

	/**
	 * Infinity test on or off
	 */
	protected boolean m_infinity = true;

	/**
	 * NBC test on or off
	 */
	protected boolean m_nbc = true;
	
	/**
	 * Neural Network recognition on or off
	 */
	protected boolean useNN = false;

	/**
	 * Heuristics to use in configuration
	 */
	protected PaleoHeuristics m_heuristics;

	/**
	 * Default constructor - creates config where all tests are turned on
	 */
	public PaleoConfig() {
		m_heuristics = new PaleoHeuristics();
	}
	
	/**
	 * Creates a configuration with all of the listed options enabled
	 * @param options a list of shape recognizers to enable
	 */
	public PaleoConfig(List<Option> options){
		this();
		this.setAllOff();
		this.enableOptions(options);
	}

	/**
	 * Creates a configuration with all of the listed options enabled
	 * @param options a list of shape recognizers to enable
	 */
	public PaleoConfig(Option... options){
		this();
		this.setAllOff();
		this.enableOptions(options);
	}

	/**
	 * Determines if line test is turned on
	 * 
	 * @return true if test is on; else false
	 */
	public boolean isLineTestOn() {
		return m_line;
	}

	/**
	 * Determines if arc test is turned on
	 * 
	 * @return true if test is on; else false
	 */
	public boolean isArcTestOn() {
		return m_arc;
	}

	/**
	 * Determines if ellipse test is turned on
	 * 
	 * @return true if test is on; else false
	 */
	public boolean isEllipseTestOn() {
		return m_ellipse;
	}

	/**
	 * Determines if circle test is turned on
	 * 
	 * @return true if test is on; else false
	 */
	public boolean isCircleTestOn() {
		return m_circle;
	}

	/**
	 * Determines if curve test is turned on
	 * 
	 * @return true if test is on; else false
	 */
	public boolean isCurveTestOn() {
		return m_curve;
	}

	/**
	 * Determines if helix test is turned on
	 * 
	 * @return true if test is on; else false
	 */
	public boolean isHelixTestOn() {
		return m_helix;
	}

	/**
	 * Determines if spiral test is turned on
	 * 
	 * @return true if test is on; else false
	 */
	public boolean isSpiralTestOn() {
		return m_spiral;
	}

	/**
	 * Determines if arrow test is turned on
	 * 
	 * @return true if test is on; else false
	 */
	public boolean isArrowTestOn() {
		return m_arrow;
	}

	/**
	 * Determines if complex test is turned on
	 * 
	 * @return true if test is on; else false
	 */
	public boolean isComplexTestOn() {
		return m_complex;
	}

	/**
	 * Determines if polyline test is turned on
	 * 
	 * @return true if test is on; else false
	 */
	public boolean isPolylineTestOn() {
		return m_polyline;
	}

	/**
	 * Determines if polygon test is turned on
	 * 
	 * @return true if test is on; else false
	 */
	public boolean isPolygonTestOn() {
		return m_polygon;
	}

	/**
	 * Determines if rectangle test is turned on
	 * 
	 * @return true if test is on; else false
	 */
	public boolean isRectangleTestOn() {
		return m_rectangle;
	}

	/**
	 * Determines if square test is turned on
	 * 
	 * @return true if test is on; else false
	 */
	public boolean isSquareTestOn() {
		return m_square;
	}

	/**
	 * Determines if diamond test is turned on
	 * 
	 * @return true if test is on; else false
	 */
	public boolean isDiamondTestOn() {
		return m_diamond;
	}

	/**
	 * Determines if dot test is turned on
	 * 
	 * @return true if test is on; else false
	 */
	public boolean isDotTestOn() {
		return m_dot;
	}

	/**
	 * Determines if wave test is turned on
	 * 
	 * @return true if wave is on; else false
	 */
	public boolean isWaveTestOn() {
		return m_wave;
	}

	/**
	 * Determines if gull test is turned on
	 * 
	 * @return true if gull is on; else false
	 */
	public boolean isGullTestOn() {
		return m_gull;
	}

	/**
	 * Determines if blob test is turned on
	 * 
	 * @return true if blob is on; else false
	 */
	public boolean isBlobTestOn() {
		return m_blob;
	}

	/**
	 * Determines if infinity test is turned on
	 * 
	 * @return true if infinity is on; else false
	 */
	public boolean isInfinityTestOn() {
		return m_infinity;
	}

	/**
	 * Determines if NBC test is turned on
	 * 
	 * @return true if NBC is on; else false
	 */
	public boolean isNBCTestOn() {
		return m_nbc;
	}

	/**
	 * Turn line test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setLineTestOn(boolean flag) {
		m_line = flag;
	}

	/**
	 * Turn arc test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setArcTestOn(boolean flag) {
		m_arc = flag;
	}

	/**
	 * Turn ellipse test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setEllipseTestOn(boolean flag) {
		m_ellipse = flag;
	}

	/**
	 * Turn circle test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setCircleTestOn(boolean flag) {
		m_circle = flag;
	}

	/**
	 * Turn curve test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setCurveTestOn(boolean flag) {
		m_curve = flag;
	}

	/**
	 * Turn helix test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setHelixTestOn(boolean flag) {
		m_helix = flag;
	}

	/**
	 * Turn spiral test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setSpiralTestOn(boolean flag) {
		m_spiral = flag;
	}

	/**
	 * Turn arrow test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setArrowTestOn(boolean flag) {
		m_arrow = flag;
	}

	/**
	 * Turn complex test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setComplexTestOn(boolean flag) {
		m_complex = flag;
	}

	/**
	 * Turn polyline test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setPolylineTestOn(boolean flag) {
		m_polyline = flag;
	}

	/**
	 * Turn polygon test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setPolygonTestOn(boolean flag) {
		m_polygon = flag;
	}

	/**
	 * Turn rectangle test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setRectangleTestOn(boolean flag) {
		m_rectangle = flag;
	}

	/**
	 * Turn square test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setSquareTestOn(boolean flag) {
		m_square = flag;
	}

	/**
	 * Turn diamond test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setDiamondTestOn(boolean flag) {
		m_diamond = flag;
	}

	/**
	 * Turn dot test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setDotTestOn(boolean flag) {
		m_dot = flag;
	}

	/**
	 * Turn wave test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setWaveTestOn(boolean flag) {
		m_wave = flag;
	}

	/**
	 * Turn gull test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setGullTestOn(boolean flag) {
		m_gull = flag;
	}

	/**
	 * Turn blob test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setBlobTestOn(boolean flag) {
		m_blob = flag;
	}

	/**
	 * Turn infinity test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setInfinityTestOn(boolean flag) {
		m_infinity = flag;
	}

	/**
	 * Turn NBC test on or off
	 * 
	 * @param flag
	 *            true for on; false for off
	 */
	public void setNBCTestOn(boolean flag) {
		m_nbc = flag;
	}

	/**
	 * Set the heurstics of the paleo configuration
	 * 
	 * @param heuristics
	 *            heuristics of the paleo config
	 */
	public void setHeuristics(PaleoHeuristics heuristics) {
		m_heuristics = heuristics;
	}

	/**
	 * Get the heurstics of the paleo configuration
	 * 
	 * @return heuristics of the paleo config
	 */
	public PaleoHeuristics getHeuristics() {
		return m_heuristics;
	}
	
	public void setNNEnabled(boolean enable){
		this.useNN = enable;
	}
	public boolean getNNEnabled(){
		return useNN;
	}

	/**
	 * Returns a list of strings of the shape tests that are turned on
	 * 
	 * @return list of strings of the shape tests that are turned on
	 */
	public List<String> getShapesTurnedOn() {
		ArrayList<String> strList = new ArrayList<String>();
		if (m_line)
			strList.add(Fit.LINE);
		if (m_arc)
			strList.add(Fit.ARC);
		if (m_ellipse)
			strList.add(Fit.ELLIPSE);
		if (m_circle)
			strList.add(Fit.CIRCLE);
		if (m_curve)
			strList.add(Fit.CURVE);
		if (m_helix)
			strList.add(Fit.HELIX);
		if (m_spiral)
			strList.add(Fit.SPIRAL);
		if (m_arrow)
			strList.add(Fit.ARROW);
		if (m_complex)
			strList.add(Fit.COMPLEX);
		if (m_polyline)
			strList.add(Fit.POLYLINE);
		if (m_polygon)
			strList.add(Fit.POLYGON);
		if (m_rectangle)
			strList.add(Fit.RECTANGLE);
		if (m_square)
			strList.add(Fit.SQUARE);
		if (m_diamond)
			strList.add(Fit.DIAMOND);
		if (m_dot)
			strList.add(Fit.DOT);
		if (m_wave)
			strList.add(Fit.WAVE);
		if (m_gull)
			strList.add(Fit.GULL);
		if (m_blob)
			strList.add(Fit.BLOB);
		if (m_infinity)
			strList.add(Fit.INFINITY);
		if (m_nbc)
			strList.add(Fit.NBC);
		return strList;
	}
	public void enableOptions(List<Option> options){
		for(Option option:options){
			setOption(option,true);
		}
	}
	public void enableOptions(Option... options){
		for(Option option:options){
			setOption(option,true);
		}
	}
	public void disableOptions(List<Option> options){
		for(Option option:options){
			setOption(option,false);
		}
	}
	public void disableOptions(Option... options){
		for(Option option:options){
			setOption(option,false);
		}
	}
	public PaleoConfig setOption(Option option,boolean enabled){
		switch(option){
		case Line:
			m_line = enabled;
			break;
		case Arc:
			m_arc = enabled;
			break;
		case Ellipse:
			m_ellipse = enabled;
			break;
		case Circle:
			m_circle = enabled;
			break;
		case Curve:
			m_curve = enabled;
			break;
		case Helix:
			m_helix = enabled;
			break;
		case Spiral:
			m_spiral = enabled;
			break;
		case Arrow:
			m_arrow = enabled;
			break;
		case Complex:
			m_complex = enabled;
			break;
		case Polyline:
			m_polyline = enabled;
			break;
		case Polygon:
			m_polygon = enabled;
			break;
		case Rectangle:
			m_rectangle =enabled;
			break;
		case Square:
			m_square = enabled;
			break;
		case Diamond:
			m_diamond = enabled;
			break;
		case Dot:
			m_dot = enabled;
			break;
		case Wave:
			m_wave = enabled;
			break;
		case Gull:
			m_gull = enabled;
			break;
		case Blob:
			m_blob = enabled;
			break;
		case Infinity:
			m_infinity = enabled;
			break;
		}
		return this;
	}

	public List<Option> getEnabledOptions(){
		ArrayList<Option> options = new ArrayList<Option>();
		if (m_line)
			options.add(Option.Line);
		if (m_arc)
			options.add(Option.Arc);
		if (m_ellipse)
			options.add(Option.Ellipse);
		if (m_circle)
			options.add(Option.Circle);
		if (m_curve)
			options.add(Option.Curve);
		if (m_helix)
			options.add(Option.Helix);
		if (m_spiral)
			options.add(Option.Spiral);
		if (m_arrow)
			options.add(Option.Arrow);
		if (m_complex)
			options.add(Option.Complex);
		if (m_polyline)
			options.add(Option.Polyline);
		if (m_polygon)
			options.add(Option.Polygon);
		if (m_rectangle)
			options.add(Option.Rectangle);
		if (m_square)
			options.add(Option.Square);
		if (m_diamond)
			options.add(Option.Diamond);
		if (m_dot)
			options.add(Option.Dot);
		if (m_wave)
			options.add(Option.Wave);
		if (m_gull)
			options.add(Option.Gull);
		if (m_blob)
			options.add(Option.Blob);
		if (m_infinity)
			options.add(Option.Infinity);
		return options;
	}
	
	public void setAllOn(){
		setAll(true);
	}
	public void setAllOff(){
		setAll(false);
	}
	private void setAll(boolean enabled){

		m_line = enabled;
		m_arc = enabled;
		m_ellipse = enabled;
		m_circle = enabled;
		m_curve = enabled;
		m_helix = enabled;
		m_spiral = enabled;
		m_arrow = enabled;
		m_polygon = enabled;
		m_polyline = enabled;
		m_complex = enabled;
		m_rectangle = enabled;
		m_square = enabled;
		m_diamond = enabled;
		m_dot = enabled;
		m_wave = enabled;
		m_gull = enabled;
		m_blob = enabled;
		m_infinity = enabled;
		m_nbc = enabled;
	}
	
	/**
	 * Get a configuration where all tests are turned on
	 * 
	 * @return configuration where all tests are turned on
	 */
	public static PaleoConfig allOn() {
		PaleoConfig config = new PaleoConfig();
		config.setAllOn();
		return config;
	}

	/**
	 * Get a configuration where all tests are turned off
	 * 
	 * @return configuration where all tests are turned off
	 */
	public static PaleoConfig allOff() {
		PaleoConfig config = new PaleoConfig();
		config.setAllOff();
		return config;
	}

	/**
	 * Default config for new version of paleo
	 * 
	 * @return default config
	 */
	public static PaleoConfig newDefault() {
		PaleoConfig config = allOn();
		config.m_square = false;
		config.m_circle = false;
		config.m_gull = false;
		config.m_blob = false;
		config.m_nbc = false;
		config.m_heuristics.SMALL_POLYLINE_COMBINE = false;
		return config;
	}

	/**
	 * Get a configuration where all tests are turned on except complex fits
	 * 
	 * @return configuration where all tests are turned on except complex fits
	 */
	public static PaleoConfig noComplex() {
		PaleoConfig config = new PaleoConfig();
		config.m_complex = false;
		return config;
	}

	/**
	 * Get a configuration where only basic primitives are allowed (lines, arcs,
	 * ellipses, circles, helixes, spirals)
	 * 
	 * @return configuration where only basic primitives are allowed
	 */
	public static PaleoConfig basicPrimsOnly() {
		PaleoConfig config = new PaleoConfig();
		config.m_arrow = false;
		config.m_polygon = false;
		config.m_polyline = false;
		config.m_complex = false;
		config.m_rectangle = false;
		config.m_square = false;
		config.m_diamond = false;
		config.m_wave = false;
		config.m_gull = false;
		config.m_blob = false;
		config.m_infinity = false;
		config.m_nbc = false;
		return config;
	}

	/**
	 * Get a configuration where only the original paleosketch shapes (from IUI
	 * paper) are allowed
	 * 
	 * @return configuration where only the original paleosketch shapes are
	 *         allowed
	 */
	public static PaleoConfig origPaleo() {
		PaleoConfig config = new PaleoConfig();
		config.m_arrow = false;
		config.m_polygon = false;
		config.m_rectangle = false;
		config.m_square = false;
		config.m_diamond = false;
		config.m_dot = false;
		config.m_wave = false;
		config.m_gull = false;
		config.m_blob = false;
		config.m_infinity = false;
		config.m_nbc = false;
		return config;
	}

	/**
	 * Get a configuration for Deep Green
	 * 
	 * @return deep green configuration
	 */
	public static PaleoConfig deepGreenConfig() {
		PaleoConfig config = PaleoConfig.allOn();
		config.setCircleTestOn(false);
		config.setComplexTestOn(false);
		config.setCurveTestOn(false);
		config.setHelixTestOn(false);
		config.setPolygonTestOn(true);
		config.setSpiralTestOn(false);
		config.setSquareTestOn(false);
		// config.setArrowTestOn(false);
		config.setBlobTestOn(false);
		config.getHeuristics().ARC_DOWN = true;
		config.getHeuristics().M_VS_GULL_CHECK = true;
		return config;
	}

	/**
	 * Get a configuration for Civil Sketch
	 */
	public static PaleoConfig civilSketchConfig() {
		PaleoConfig config = PaleoConfig.allOff();
		config.setLineTestOn(true);
		config.setCircleTestOn(true);
		config.setDotTestOn(true);
		// config.setArrowTestOn(true);
		// config.setCurveTestOn(true);
		// config.setArcTestOn(true);
		config.setPolylineTestOn(true);
		// config.setPolygonTestOn(true);
		// config.setRectangleTestOn(true);
		return config;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		PaleoConfig clonedConfig = new PaleoConfig();
		clonedConfig.setArcTestOn(m_arc);
		clonedConfig.setArrowTestOn(m_arrow);
		clonedConfig.setBlobTestOn(m_blob);
		clonedConfig.setCircleTestOn(m_circle);
		clonedConfig.setComplexTestOn(m_complex);
		clonedConfig.setCurveTestOn(m_curve);
		clonedConfig.setDiamondTestOn(m_diamond);
		clonedConfig.setDotTestOn(m_dot);
		clonedConfig.setEllipseTestOn(m_ellipse);
		clonedConfig.setGullTestOn(m_gull);
		clonedConfig.setHelixTestOn(m_helix);
		clonedConfig.setInfinityTestOn(m_infinity);
		clonedConfig.setLineTestOn(m_line);
		clonedConfig.setNBCTestOn(m_nbc);
		clonedConfig.setPolygonTestOn(m_polygon);
		clonedConfig.setPolylineTestOn(m_polyline);
		clonedConfig.setRectangleTestOn(m_rectangle);
		clonedConfig.setSpiralTestOn(m_spiral);
		clonedConfig.setSquareTestOn(m_square);
		clonedConfig.setWaveTestOn(m_wave);
		clonedConfig.setHeuristics(m_heuristics);
		return clonedConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof PaleoConfig) {
			PaleoConfig other = (PaleoConfig) o;
			return other.m_arc == m_arc && other.m_arrow == m_arrow
					&& other.m_blob == m_blob && other.m_circle == m_circle
					&& other.m_complex == m_complex && other.m_curve == m_curve
					&& other.m_diamond == m_diamond && other.m_dot == m_dot
					&& other.m_ellipse == m_ellipse && other.m_gull == m_gull
					&& other.m_helix == m_helix
					&& other.m_infinity == m_infinity && other.m_line == m_line
					&& other.m_nbc == m_nbc && other.m_polygon == m_polygon
					&& other.m_polyline == m_polyline
					&& other.m_rectangle == m_rectangle
					&& other.m_spiral == m_spiral && other.m_square == m_square
					&& other.m_wave == m_wave
					&& other.m_heuristics.equals(m_heuristics);
		}
		return false;
	}
}
