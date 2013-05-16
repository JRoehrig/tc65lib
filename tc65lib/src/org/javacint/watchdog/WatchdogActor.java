package org.javacint.watchdog;

/**
 * Watchdog acting interface
 * @author Florent Clairambault
 */
public interface WatchdogActor {

	/**
	 * Tell the watchdog to send its keep-alive signal.
	 * @return true if not error was encountered
	 */
	boolean kick();
}
