package srl.distributed.server;

import javax.servlet.http.HttpServletRequest;

public class ServiceContext {
	private HttpServletRequest request;
	public ServiceContext(HttpServletRequest request){
		this.request = request;
	}
	public HttpServletRequest getRequest(){
		return request;
	}
}
