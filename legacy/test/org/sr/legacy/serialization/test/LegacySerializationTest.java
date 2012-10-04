package org.sr.legacy.serialization.test;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openawt.svg.SVGCanvas;
import org.sr.legacy.serialization.SketchSimplConverter;
import org.sr.legacy.serialization.SousaReader;

import srl.core.sketch.Sketch;

public class LegacySerializationTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void readSimplXML() throws Exception{
		Sketch sketch = SketchSimplConverter.readSimplSketch(LegacySerializationTest.class.getResourceAsStream("files/simpl.sketch.1.xml"));
		SVGCanvas canvas = new SVGCanvas();
		canvas.addShape(sketch.toSVGShape());
		canvas.serialize(new File("sketch.svg"));
		assertNotNull("Simpl encoded sketch failed to deserialize.",sketch);
	}
	
	@Test
	public void readSousaXML() throws Exception{
		SousaReader input = new SousaReader();
		Sketch sketch = input.parseDocument(LegacySerializationTest.class.getResourceAsStream("files/sousa.sample.xml"));
		assertNotNull("Sousa encoded sketch failed to deserialize.",sketch);
	}
}
