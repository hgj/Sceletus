package hu.hgj.sceletus.module;

/**
 * Module interface to gather all modules.
 * <p>
 * All modules should have a current state (see {@link State}), should be able
 * to transition to another using the {@link #reset()}, {@link #start()}
 * and {@link #stop()} methods.
 */
public interface Module {

	/**
	 * Should return the name of the module, that is a unique identifier of an
	 * instance.
	 *
	 * @return The name of the module.
	 */
	String getName();

	/**
	 * Possible states for a module.
	 */
	enum State {
		/** Unknown state. */
		UNKNOWN,
		/** Ready to be started state. */
		RESET,
		/** Started state. (Does not mean it is "running".) */
		STARTED,
		/** Stopped state. (Does not mean it is "not running".) */
		STOPPED
	}

	/**
	 * Should return the current state of the module.
	 *
	 * @return The current state of the module.
	 */
	State getState();

	/**
	 * Configure the module based on the passed configuration.
	 *
	 * @param configuration The configuration "object".
	 *
	 * @return True on success, false otherwise.
	 */
	boolean updateConfiguration(Object configuration);

	/**
	 * Transition the module to the {@link State#RESET} state.
	 *
	 * @return True on success, false otherwise.
	 */
	boolean reset();

	/**
	 * Transition the module to the {@link State#STARTED} state.
	 *
	 * @return True on success, false otherwise.
	 */
	boolean start();

	/**
	 * Transition the module to the {@link State#STOPPED} state.
	 *
	 * @return True on success, false otherwise.
	 */
	boolean stop();

}
