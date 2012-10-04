package srl.recognition.handwriting;

public class ResultConfidencePairing  implements Comparable<ResultConfidencePairing>{
	
	private String m_result;
	
	private double m_confidence;
	
	public ResultConfidencePairing(String stringVal, double confidence){
		m_result = stringVal;
		m_confidence = confidence;
	}
	
	public String getResult() {
		return m_result;
	}
	
	public double getConfidence() {
		return m_confidence;
	}

	@Override
	public int compareTo(ResultConfidencePairing o) {
		return Double.compare(o.getConfidence(),m_confidence);
	}

}
