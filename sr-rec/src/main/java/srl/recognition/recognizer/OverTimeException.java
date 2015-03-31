package srl.recognition.recognizer;

/**
 * Exception for if a recognizer takes longer than the maximum time allowed.
 * 
 * @see Exception
 * @author awolin
 */
public class OverTimeException extends Exception {

	/**
	 * Auto-generated ID.
	 */
	private static final long serialVersionUID = -6065038974875746286L;

	/**
	 * The default message to use for exceptions of this type.
	 */
	public static final String DEFAULT_MESSAGE = "The recognizer has exceeded its maximum allotted run-time.";

	/**
	 * Construct an exception with the default message.
	 */
	public OverTimeException() {
		this(DEFAULT_MESSAGE);
	}

	/**
	 * Construct an exception with the given message.
	 * 
	 * @param message
	 *            the message for this exception.
	 */
	public OverTimeException(String message) {
		super(message);
	}

	/**
	 * Construct an exception with the given cause.
	 * 
	 * @param cause
	 *            the cause of this exception.
	 */
	public OverTimeException(Throwable cause) {
		this(DEFAULT_MESSAGE, cause);
	}

	/**
	 * Construct an exception with the given message and cause.
	 * 
	 * @param message
	 *            the message for this exception.
	 * @param cause
	 *            the cause of this exception.
	 */
	public OverTimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
