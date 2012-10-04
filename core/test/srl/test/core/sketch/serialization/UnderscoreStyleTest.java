package srl.test.core.sketch.serialization;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import srl.core.serialization.UnderscoreStyle;

public class UnderscoreStyleTest {
	UnderscoreStyle style = new UnderscoreStyle();
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		assertTrue(style.getAttribute("Sketch"),style.getAttribute("Sketch").equals("sketch"));
		assertTrue(style.getAttribute("SketchRec"),style.getAttribute("SketchRec").equals("sketch_rec"));
		assertTrue(style.getAttribute("SComponent"),style.getAttribute("SComponent").equals("scomponent"));
	}

}
