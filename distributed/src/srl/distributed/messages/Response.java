package srl.distributed.messages;



public abstract class Response extends Message{
	protected boolean success = false;
	public Response(){};
	public Response(boolean success){
		this.success = success;
	}
	/**
	 * @return the success
	 */
	public boolean getSuccess() {
		return success;
	}
	/**
	 * @param success the success to set
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}
}
