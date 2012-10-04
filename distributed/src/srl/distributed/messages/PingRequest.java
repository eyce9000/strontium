package srl.distributed.messages;

import javax.servlet.http.HttpSession;

public class PingRequest extends Request {
	private long startTime;
	
	public PingRequest(long startTime){
		this.startTime = startTime;
	}
	

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	@Override
	public Response performService(HttpSession clientSessionScope) {
		// TODO Auto-generated method stub
		return null;
	}
}
