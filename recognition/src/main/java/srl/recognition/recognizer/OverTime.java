package srl.recognition.recognizer;

import org.slf4j.Logger;


public class OverTime {

	/**
	 * Checks whether the current recognition time has exceeded the maximum
	 * allowed time.
	 * 
	 * @param startTime
	 *            time the recognizer started.
	 * @param maxTime
	 *            maximum time allotted to the recognizer.
	 * @param log
	 *            recognizer's log4j logger.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time, {@code maxTime}.
	 */
	public static void overTimeCheck(long startTime, long maxTime, Logger log)
			throws OverTimeException {

		long currTime = System.currentTimeMillis();
		long currDuration = currTime - startTime;
		if (currDuration >= maxTime) {
			log.info("Ran out of time during recognition at " + currDuration
					+ " ms. " + "Exceeded the allotted " + maxTime + " ms.");

			throw new OverTimeException(
					"The recognizer has run out of time in " + log.getName());
		}
	}

	/**
	 * Return the remaining amount of time left for recognition.
	 * 
	 * @param startTime
	 *            time the recognizer started.
	 * @param maxTime
	 *            maximum time allotted to the recognizer.
	 * @return remaining time for recognition.
	 */
	public static long timeRemaining(long startTime, long maxTime) {
		return maxTime - (System.currentTimeMillis() - startTime);
	}
}
