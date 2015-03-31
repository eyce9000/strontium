package srl.core.sketch;
/*******************************************************************************
 *  Revision History:<br>
 *  SRL Member - File created
 *
 *  <p>
 *  <pre>
 *  This work is released under the BSD License:
 *  (C) 2011 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openawt.Image;
import org.openawt.svg.SVGShape;
import org.simpleframework.xml.ElementList;

import srl.core.sketch.comparators.TimePeriodComparator;

public class Shape extends SContainer implements IClassifiable, IBeautifiable {

	@ElementList(entry="interpretation")
	protected ArrayList<Interpretation> interpretations;

	@ElementList(entry="alias")
	protected ArrayList<Alias> aliases;

	private transient long timeStart = -1L;
	private transient long timeEnd = -1L;

	public Shape() {
		super();

		interpretations = new ArrayList<Interpretation>();
		aliases = new ArrayList<Alias>();
	}

	public Shape(Shape copyFrom) {
		super(copyFrom);

		interpretations = new ArrayList<Interpretation>();
		for (Interpretation i : copyFrom.interpretations)
			interpretations.add(i.clone());

		aliases = new ArrayList<Alias>();
		for (Alias a : copyFrom.aliases)
			aliases.add(a.clone());
	}

	public Shape(IShape shape) {
		this();

		for (IShape sub : shape.getSubShapes())
			add(new Shape(sub));
		for (IStroke stroke : shape.getStrokes())
			add(new Stroke(stroke));

		id = shape.getID();
		for (Map.Entry<String, String> entry : shape.getAttributes().entrySet()) {
			setAttribute(entry.getKey(), entry.getValue());
		}
		if (shape.getLabel() != null && shape.getLabel() != "")
			interpretations.add(new Interpretation(shape.getLabel(), shape
					.getConfidence()));
	}

	public Shape(Collection<? extends Stroke> subStrokes,
			Collection<? extends Shape> subShapes) {
		this();
		addAll(subStrokes);
		addAll(subShapes);
	}

	@Override
	public Interpretation getInterpretation() {
		return (interpretations.size() > 0) ? interpretations
				.get(interpretations.size() - 1) : null;
	}

	@Override
	public List<Interpretation> getNBestList() {
		return interpretations;
	}

	@Override
	public void addInterpretation(Interpretation i) {
		int position = Collections.binarySearch(interpretations, i);

		if (position >= 0)
			return;

		interpretations.add(-(position + 1), i);
	}

	@Override
	public void setNBestList(List<Interpretation> list) {
		interpretations = new ArrayList<Interpretation>(list);
	}

	/**
	 * Set the label of this shape.
	 * 
	 * Override all current interpretations.
	 * 
	 * @param label
	 */
	public void setLabel(String label) {
		if (interpretations.size() > 1) {
			System.err
					.println("warning: clearning interpretations with setLabel");
		}
		interpretations.clear();
		interpretations.add(new Interpretation(label, 1.0));
	}

	@Override
	public void setInterpretation(Interpretation i) {
		if (interpretations.size() > 1) {
			System.err
					.println("warning: clearning interpretations with setInterpretation");
		}
		interpretations.clear();
		interpretations.add(i);
	}

	@Override
	public Shape clone() {
		return new Shape(this);
	}

	public void addAlias(Alias a) {
		aliases.add(a);
	}

	public List<Alias> getAliases() {
		return aliases;
	}

	public Alias getAlias(String s) {
		for (Alias a : aliases)
			if (a.name.equals(s))
				return a;
		return null;
	}

	@Override
	public void setInterpretation(String label, double confidence) {
		setInterpretation(new Interpretation(label, confidence));
	}

	@Override
	public String toString() {
		return super.toString() + " " + getInterpretation() + " " + id;
	}
	
	@Override
	public SVGShape toSVGShape(){
		if(beautifiedShape!=null)
			return getBeautifiedShape();
		else{
			return super.toSVGShape();
		}
	}
	

	/**************************
	 * 
	 * IBeautifiable
	 * 
	 **************************/

	protected transient Type beautificationType = Type.NONE;
	protected transient SVGShape beautifiedShape;
	protected transient Image beautifiedImage;
	protected transient BoundingBox beautifiedImageBBox;

	@Override
	public Type getBeautificationType() {
		return beautificationType;
	}

	@Override
	public Image getBeautifiedImage() {
		return beautifiedImage;
	}

	@Override
	public BoundingBox getBeautifiedImageBoundingBox() {
		return beautifiedImageBBox;
	}

	@Override
	public void setBeautificationType(Type type) {
		beautificationType = type;
	}

	@Override
	public void setBeautifiedImage(Image image, BoundingBox boundingBox) {
		beautifiedImage = image;
		beautifiedImageBBox = boundingBox;
	}

	@Override
	public void setBeautifiedShape(SVGShape shape) {
		beautifiedShape = shape;
	}

	@Override
	public SVGShape getBeautifiedShape() {
		return beautifiedShape;
	}

	private static TimePeriodComparator comparator = new TimePeriodComparator();
	@Override
	public int compareTo(TimePeriod other) {
		return comparator.compare(this, other);
	}



}
