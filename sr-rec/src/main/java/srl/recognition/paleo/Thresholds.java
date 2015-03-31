/**
 * Thresholds.java
 *
 * Revision History:<br>
 * Jun 23, 2008 bpaulson - File created
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

/**
 * Interface containing various thresholds used throughout the PaleoSketch
 * recognizer
 * 
 * @author bpaulson
 */
public class Thresholds {

	private static final double BASE_PPI = 94;
	public static final Thresholds DEFAULT_PC = new Thresholds(94);
	public static final Thresholds DEFAULT_ANDROID = new Thresholds(254);
	
	public static Thresholds active = DEFAULT_PC;

	public final double PPI;
	public final double M_HOOK_MAXHOOKLENGTH; // Absolute Length
	
	public final double M_HOOK_MAXHOOKPERCENT = .2;
	public final double M_HOOK_MINSTROKELENGTH; // C
	
	public final int M_HOOK_MINPOINTS = 5; // B
	public final double M_HOOK_MINHOOKCURVATURE = .5; // A
	public final double M_CORNER_MINANGLEDIFF = Math.PI / 10;
	public final double M_CORNER_MAXDISTANCETOSPEED = 15;
	
	public final double M_CORNER_MAXDISTANCETOADJACENT;
	
	public final double M_CORNER_BRANDON_DIS_PERCENT = 0.04;
	public final double M_CORNER_BRANDON_SIM_SLOPE = 1.0;
	public final double M_REVS_TO_BE_OVERTRACED = 1.31; // D
	public final double M_PERCENT_DISTANCE_TO_BE_COMPLETE = 0.17; // E
	public final double M_NUM_REVS_TO_BE_COMPLETE = 0.75;// 0.76;//0.75;
	public final double M_LINE_LS_ERROR_FROM_ENDPTS = 1.5;// 1.65;//1.7;//1.8;
	public final double M_LINE_FEATURE_AREA = 10.25; // H
	public final double M_ELLIPSE_FEATURE_AREA = 0.35; // 0.33; // M
	
	public final double M_ELLIPSE_SMALL;// 30.0; // L
	
	public final double M_CIRCLE_FEATURE_AREA = 0.35; // P
	
	public final double M_CIRCLE_SMALL; // N
	
	public final double M_ARC_FEATURE_AREA = 0.42; // Q
	public final double M_AXIS_RATIO_TO_BE_CIRCLE = 0.5;// 0.6;//0.425;
	public final double M_NDDE_HIGH = 0.79; // K
	public final double M_SPIRAL_CENTER_CLOSENESS = 0.25; // T
	public final double M_SPIRAL_RADIUS_RATIO = 0.9; // S
	public final double M_SPIRAL_DIAMETER_CLOSENESS = 0.2; // U
	public final double M_POLYLINE_LS_ERROR = 1.3;// 1.0;//0.0036; // I
	public final double M_POLYLINE_SUBSTROKES_LOW = 5; // X
	public final double M_DCR_TO_BE_POLYLINE = 6.0; // J
	public final double M_DCR_TO_BE_POLYLINE_STRICT = 9.0; // W
	public final double M_CURVE_ERROR = 0.37; // R
	public final double M_RATIO_TO_REMOVE_TAIL = 0.1; // V
	public final double M_ELLIPSE_CORNER_LENGTH_RATIO = 0.01;
	public final double M_CIRCLE_CORNER_LENGTH_RATIO = 0.05;
	public final double M_CURVE_CORNER_LENGTH_RATIO = 0.003;
	public final double M_ARC_AREA_RATIO = 0.3;
	public final double M_SLOPE_DIFF = 6.5;
	public final double M_REVS_TO_BE_CIRCULAR = 0.875;
	public final double M_ENDPT_STROKE_LENGTH_RATIO = 0.98;
	public final double M_NEIGHBORHOOD_PCT = .06;
	public final double M_POLYGON_PCT = 0.05;
	public final double M_DIR_WINDOW_SIZE = 5.0;
	public final double M_RECTANGLE_ERROR = 0.35; // 0.275
	public final double M_RECTANGLE_LOW_ERROR = 0.15; // 0.17;
	public final double M_LOW_ROTATION = 0.92;
	public final double M_BB_MAJOR_AXIS_RATIO = 0.15;// 0.143; // 0.2
	
	public final double M_ARC_SMALL;

	public Thresholds(double ppi) {
		PPI = ppi;
		double ppiRatio = PPI / BASE_PPI;

		M_HOOK_MAXHOOKLENGTH = 10 * ppiRatio;

		M_HOOK_MINSTROKELENGTH = 70 * ppiRatio;

		M_CORNER_MAXDISTANCETOADJACENT = 15 * ppiRatio;
		
		M_ELLIPSE_SMALL = 33.0 * ppiRatio;
		
		M_CIRCLE_SMALL = 16.0 * ppiRatio;
		
		M_ARC_SMALL = 25.0 * ppiRatio;
	}

}
