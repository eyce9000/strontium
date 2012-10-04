package srl.distributed.messages;

import javax.servlet.http.HttpSession;

import org.codehaus.jackson.annotate.JsonTypeInfo;


public abstract class Request extends Message{
	abstract public Response performService(HttpSession session);
}
