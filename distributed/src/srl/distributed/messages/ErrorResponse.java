package srl.distributed.messages;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorResponse extends Response{
	String classname;
	String message;
	String stacktrace;

	public ErrorResponse() {
	}
	public ErrorResponse(Throwable t){
		classname = t.getClass().getName();
		message = t.getMessage();

		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));

		stacktrace = sw.toString();
		
	}
	
	public ErrorResponse(String s) {
		message = s;
	}

	/**
	 * @return the classname
	 */
	public String getClassname() {
		return classname;
	}
	/**
	 * @param classname the classname to set
	 */
	public void setClassname(String classname) {
		this.classname = classname;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the stacktrace
	 */
	public String getStacktrace() {
		return stacktrace;
	}
	/**
	 * @param stacktrace the stacktrace to set
	 */
	public void setStacktrace(String stacktrace) {
		this.stacktrace = stacktrace;
	}
	public String toString() {
		return message;
	}
	
}
