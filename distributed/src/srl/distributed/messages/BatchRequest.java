package srl.distributed.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpSession;

public class BatchRequest extends Request {
	private List<Request> requests;
	
	public BatchRequest(){};
	public BatchRequest(Request...messages){
		this.requests = Arrays.asList(messages);
	}
	
	public BatchRequest(List<Request> messages){
		this.requests = messages;
	}
	

	/**
	 * @return the messages
	 */
	public List<Request> getRequests() {
		return requests;
	}
	/**
	 * @param messages the messages to set
	 */
	public void setRequests(List<Request> messages) {
		this.requests = messages;
	}

	
	
	@Override
	public Response performService(HttpSession session) {
		List<Response> responses = new ArrayList<Response>();
		for(Request message:requests){
			responses.add(message.performService(session));
		}
		return new BatchResponse(responses);
	}
}
