package srl.distributed.server;

import srl.distributed.messages.Request;
import srl.distributed.messages.Response;

public abstract class ServerRequest extends Request {

	abstract public Response performService(ServiceContext context);

}
