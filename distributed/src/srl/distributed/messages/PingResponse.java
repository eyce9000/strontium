package srl.distributed.messages;

public class PingResponse extends Response {
	private long difference;
	
	private PingResponse(){}
	
	public PingResponse(long difference){
		this.success = true;
		this.difference = difference;
	}
	/**
	 * @return the difference
	 */
	public long getDifference() {
		return difference;
	}
	/**
	 * @param difference the difference to set
	 */
	public void setDifference(long difference) {
		this.difference = difference;
	}
}
