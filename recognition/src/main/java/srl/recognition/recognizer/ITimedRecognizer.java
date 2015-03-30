package srl.recognition.recognizer;

/**
 * Interface for using a timed recognize method that will stop after the maximum
 * time has been reached.
 * 
 * @author awolin
 */
public interface ITimedRecognizer<Rec, Res> extends IRecognizer<Rec, Res> {

	/**
	 * Recognize all the objects that have been submitted for recognition. Block
	 * until recognition is complete and then return the results of recognition.
	 * <p>
	 * If the recognizer runs for longer than the given time, {@code maxTime},
	 * an {@link OverTimeException} is thrown.
	 * 
	 * @param maxTime
	 *            the maximum time the recognizer is allowed to run.
	 * @return a list of results recognized from the input to the recognizer.
	 * 
	 * @throws if
	 *             the recognizer runs for longer than {@code maxTime}.
	 */
	public Res recognizeTimed(long maxTime) throws OverTimeException;
}
