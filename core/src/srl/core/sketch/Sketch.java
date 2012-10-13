
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

package srl.core.sketch;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.openawt.Color;
import org.openawt.svg.SVGCanvas;
import org.openawt.svg.SVGShape;
import org.openawt.svg.Style;
import org.openawt.svg.serialization.ColorTransform;
import org.openawt.svg.serialization.RegisterMatcher;
import org.openawt.svg.serialization.StyleTransform;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.CycleStrategy;
import org.simpleframework.xml.stream.Format;
import org.xml.sax.SAXException;

import srl.core.serialization.TypeAttributeMixin;
import srl.core.serialization.UUIDTransform;
import srl.core.serialization.UnderscoreStyle;
import srl.core.sketch.comparators.TimePeriodComparator;

@Namespace(reference="http://sketchrecognition.com")
@Root(strict=false)
public class Sketch extends SContainer implements TimePeriod,Comparable<TimePeriod>{
	
	
	public Sketch() {

	}

	public Sketch(Sketch copyFrom) {
		super(copyFrom);
	}

	public Sketch(ISketch sketch) {
		this();
		for (IShape shape : sketch.getShapes())
			add(new Shape(shape));
		for (IStroke stroke : sketch.getStrokes())
			add(new Stroke(stroke));

		id = sketch.getID();
	}
	public Sketch(Stroke...strokes ){
		this();
		for(Stroke stroke:strokes)
			add(stroke);
	}
	
	public void serializeSVG(OutputStream out) throws Exception{
		SVGSketchCanvas canvas = new SVGSketchCanvas(this);
		canvas.serialize(out);
	}
	public void serializeSVG(File out) throws Exception{
		SVGSketchCanvas canvas = new SVGSketchCanvas(this);
		canvas.serialize(out);
	}
	public void serializeXML(OutputStream out) throws Exception{
		Persister persister = buildXMLCycleSerializer();
		persister.write(this, out);
	}
	public void serializeXML(File f) throws Exception{
		Persister persister = buildXMLCycleSerializer();
		persister.write(this, f);
	}
	public void serializeFlatXML(File f) throws Exception{
		Persister persister = buildXMLFlatSerializer();
		persister.write(this, f);
	}
	public void serializeFlatXML(OutputStream f) throws Exception{
		Persister persister = buildXMLFlatSerializer();
		persister.write(this, f);
	}
	public static Sketch deserailizeSVG(InputStream in ) throws Exception{
		return SVGSketchCanvas.deserialize(in).getSketch();
	}
	public static Sketch deserailizeSVG(File in ) throws Exception{
		return SVGSketchCanvas.deserialize(in).getSketch();
	}
	public static Sketch deserializeXML(File f) throws Exception{
		Persister persister = buildXMLCycleSerializer();
		return persister.read(Sketch.class, f);
	}
	public static Sketch deserializeXML(InputStream in) throws Exception{
		Persister persister = buildXMLCycleSerializer();
		return persister.read(Sketch.class, in);
	}
	public void serializeJSON(OutputStream outstream) throws JsonGenerationException, JsonMappingException, IOException{
		ObjectMapper mapper = buildJSONSerializer();
		mapper.writeValue(outstream, this);
	}
	public void serializeJSON(File file) throws JsonGenerationException, JsonMappingException, IOException{
		ObjectMapper mapper = buildJSONSerializer();
		mapper.writeValue(file, this);
	}
	public static Sketch deserializeJSON(File f) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = buildJSONSerializer();
		return mapper.readValue(f, Sketch.class);
	}
	public static Sketch deserializeJSON(InputStream instream) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = buildJSONSerializer();
		return mapper.readValue(instream, Sketch.class);
	}
	
	public static Persister buildXMLCycleSerializer(){
		Persister persister = new Persister(new CycleStrategy("_id","_refid"),buildXMLTypeMatcher(),new Format(new UnderscoreStyle()));
		return persister;
	}
	public static Persister buildXMLFlatSerializer(){
		Persister persister = new Persister(buildXMLTypeMatcher(),new Format(new UnderscoreStyle()));
		return persister;
	}
	public static ObjectMapper buildJSONSerializer(){
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(JsonMethod.ALL, Visibility.NONE);
		mapper.setVisibility(JsonMethod.FIELD, Visibility.ANY);
		mapper.configure(Feature.INDENT_OUTPUT, true);
		mapper.configure(Feature.WRITE_NULL_MAP_VALUES, false);
		mapper.setSerializationInclusion(Inclusion.NON_EMPTY);
		mapper.getSerializationConfig().addMixInAnnotations(Color.class, TypeAttributeMixin.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Color.class, TypeAttributeMixin.class);
		mapper.getSerializationConfig().addMixInAnnotations(Style.class, TypeAttributeMixin.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Style.class, TypeAttributeMixin.class);
		return mapper;
	}
	
	public static RegisterMatcher buildXMLTypeMatcher(){
		RegisterMatcher matcher = new RegisterMatcher();
		matcher.register(UUID.class, new UUIDTransform());
		matcher.register(Color.class, new ColorTransform());
		matcher.register(Style.class, new StyleTransform());
		return matcher;
	}
	
	@Override
	public Sketch clone() {
		return new Sketch(this);
	}

	
	private static TimePeriodComparator comparator = new TimePeriodComparator();
	@Override
	public int compareTo(TimePeriod other) {
		return comparator.compare(this, other);
	}
}




/**
 * SketchCanvas is a container that stores sketch data and its SVG representation together.
 * If serialized to an xml file
 * @author George R. Lucchese
 *
 */
class SVGSketchCanvas extends SVGCanvas{
	@Element 
	Sketch sketch;

	private SVGSketchCanvas(){
		
	}
	public SVGSketchCanvas(Sketch sketch){
		super();
		setSketch(sketch);
	}
	
	/**
	 * @return the sketch
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * @param sketch the sketch to set
	 */
	public void setSketch(Sketch sketch) {
		this.sketch = sketch;
		this.addShape(sketch.toSVGShape());
	}
	

	/*
	 * SERIALIZATION
	 */
	public static SVGSketchCanvas deserialize(InputStream in) throws Exception{
		Persister persister = new Persister(buildXMLTypeMatcher());
		return persister.read(SVGSketchCanvas.class, in);
	}
	
	public static SVGSketchCanvas deserialize(File in) throws Exception{
		Persister persister = new Persister(buildXMLTypeMatcher());
		return persister.read(SVGSketchCanvas.class, in);
	}
	
	public void serialize(OutputStream out) throws Exception{

		Persister persister = new Persister(buildXMLTypeMatcher());
		persister.write(this, out);
	}
	
	public void serialize(File out) throws Exception{
		Persister persister = new Persister(buildXMLTypeMatcher());
		persister.write(this, out);
	}
	
	public static RegisterMatcher buildXMLTypeMatcher(){
		return SVGCanvas.buildXMLTypeMatcher().extend(Sketch.buildXMLTypeMatcher());
	}
	
	
}
