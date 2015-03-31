package org.sr.legacy.serialization;

import java.io.File;

import org.openawt.Color;
import org.openawt.svg.Style;

import srl.core.sketch.Sketch;

public class SousaConverter {
	public static void main(String[] args){
		convertSousaFiles(new File("data/original"),new File("data/converted"));
	}
	public static void convertSousaFiles(File inputDir,File outputDir){
		File[] input = inputDir.listFiles();
		for(File file:input){
			
			if(file.isDirectory()){
				//Is directory, recurse
				convertSousaFiles(new File(inputDir,file.getName()),new File(outputDir,file.getName()));
			}
			else if(file.getName().endsWith(".xml")){
				try {
					Sketch sketch = new SousaReader().parseDocument(file);
					File newFile = new File(outputDir,file.getName().replace(".xml", ".svg"));
					newFile.getParentFile().mkdirs();
					sketch.setStyle(new Style().setFill(Color.NONE).setStroke(Color.BLACK).setStrokeWidth(2f));
					sketch.serializeSVG(newFile);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}
}
