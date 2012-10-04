package srl.distributed.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BatchResponse extends Response{

	private List<Response> responses = new ArrayList<Response>();
	
	public BatchResponse(){};
	public BatchResponse(Response...messages){
		this(Arrays.asList(messages));
	}
	
	public BatchResponse(List<Response> messages){
		setResponses(messages);
	}
	
	/**
	 * @return the messages
	 */
	public List<Response> getResponses() {
		return responses;
	}
	/**
	 * @param messages the messages to set
	 */
	public void setResponses(List<Response> messages) {
		this.responses = messages;
		success = true;
		for(Response message: messages){
			if(!message.success){
				success = false;
				break;
			}
		}
	}

}
