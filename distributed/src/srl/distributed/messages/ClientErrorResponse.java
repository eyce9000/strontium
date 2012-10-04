package srl.distributed.messages;

public class ClientErrorResponse extends ErrorResponse{
	public ClientErrorResponse(Throwable e){
		super(e);
	}
}
