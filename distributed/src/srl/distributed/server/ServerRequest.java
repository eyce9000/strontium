package srl.distributed.server;

import javax.servlet.http.HttpSession;

import srl.distributed.messages.Request;
import srl.distributed.messages.Response;

public abstract class ServerRequest extends Request {

	abstract public Response performService(ServiceContext context);

}
