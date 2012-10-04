package srl.core.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import srl.core.sketch.Point;
import srl.core.sketch.Stroke;


public class StrokeAnalysis {
	private HashMap<String,StrokeAnalyzer> analyzers = new HashMap<String,StrokeAnalyzer>();
	private HashMap<String,List<DataPoint>> features = new HashMap<String,List<DataPoint>>();
	public StrokeAnalysis(Stroke stroke,StrokeAnalyzer... newAnalyzers){
		registerAnalyzer(newAnalyzers);
		recalculate(stroke);
	}
	public Map<String,List<Double>> getAllFeatures(){
		return new HashMap(features);
	}
	public List<DataPoint> getFeature(String key){
		return features.get(key);
	}
	public StrokeAnalysis registerAnalyzer(StrokeAnalyzer... newAnalyzers){
		for(StrokeAnalyzer analyzer:newAnalyzers){
			analyzers.put(analyzer.getName(), analyzer);
		}
		return this;
	}

	public void exportToCSV(File file) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		boolean done = false;

		//Write headers

		String rowData = "\"Timestamp\"";
		for(StrokeAnalyzer analyzer:analyzers.values()){
			rowData += ",\""+analyzer.getName()+"\"";
		}
		writer.write(rowData+"\n");

		int row = 0;
		//Write rows
		while(!done){
			long timestamp = 0;
			double[] data = new double[analyzers.size()];
			int i = 0;
			for(StrokeAnalyzer analyzer:analyzers.values()){
				List<DataPoint> dataList = analyzer.getData();
				if(row<dataList.size()){
					data[i] = dataList.get(row).getValue();
					timestamp = dataList.get(row).getTimestamp();
					i++;
				}
				else{
					done = true;
					break;
				}
			}
			if(!done){
				rowData = timestamp+"";
				for(i=0; i<data.length; i++){
					rowData += ","+data[i];
				}
				writer.write(rowData+"\n");
			}
			row ++;
		}

		writer.close();
	}

	public StrokeAnalysis recalculate(Stroke stroke){
		features = new HashMap<String,List<DataPoint>>();
		for(Point point:stroke.getPoints()){
			for(StrokeAnalyzer analyzer:analyzers.values()){
				analyzer.nextPoint(point);
			}
		}
		for(StrokeAnalyzer analyzer:analyzers.values()){
			features.put(analyzer.getName(),analyzer.getData());
		}
		return this;
	}
}
