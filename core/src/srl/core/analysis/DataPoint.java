package srl.core.analysis;

public class DataPoint {
	private double value;
	private long timestamp;
	public DataPoint(double value,long time){
		this.value = value;
		this.timestamp = time;
	}
	public double getValue(){
		return value;
	}
	public long getTimestamp(){
		return timestamp;
	}
}
