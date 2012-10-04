package org.sr.legacy.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import ecologylab.serialization.SIMPLTranslationException;

public class SketchSimplConverter {
	public static srl.core.sketch.Sketch readSimplSketch(File file) throws Exception{
		FileInputStream fis = new FileInputStream(file);
		return readSimplSketch(fis);
	}
	public static srl.core.sketch.Sketch readSimplSketch(InputStream inputStream) throws Exception{

		edu.tamu.core.sketch.Sketch sketchSimpl = edu.tamu.core.sketch.Sketch.deserializeSimplXML(inputStream);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		sketchSimpl.serializeSRXML(out);

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		return srl.core.sketch.Sketch.deserializeXML(in);
	}
}
