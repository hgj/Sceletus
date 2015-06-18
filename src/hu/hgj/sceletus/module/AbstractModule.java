package hu.hgj.sceletus.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractModule implements Module {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String name;

	private State state = State.UNKNOWN;

	public AbstractModule(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public boolean reset() {
		if (state == State.RESET) {
			logger.warn("Module '{}' is already RESET. Not doing anything.", getName());
			return true;
		}
		if (state == State.STARTED) {
			logger.warn("Module '{}' is STARTED. Stopping before resetting.", getName());
			if (!stop()) {
				// Not logging here about the stop() call.
				return false;
			}
		}
		if (doReset()) {
			state = State.RESET;
			logger.info("Module '{}' is RESET.", getName());
			return true;
		} else {
			state = State.UNKNOWN;
			logger.error("Failed to RESET module '{}'. Module is in UNKNOWN state.", getName());
			return false;
		}
	}

	protected abstract boolean doReset();

	@Override
	public boolean start() {
		if (state == State.STARTED) {
			logger.warn("Module '{}' is already STARTED. Not doing anything.", getName());
			return true;
		}
		if (state != State.RESET) {
			logger.warn("Module '{}' is not RESET. Resetting before starting.", getName());
			if (!reset()) {
				// Not logging here about the reset() call.
				return false;
			}
		}
		if (doStart()) {
			state = State.STARTED;
			logger.info("Module '{}' is STARTED.", getName());
			return true;
		} else {
			state = State.UNKNOWN;
			logger.error("Failed to START module '{}'. Module is in UNKNOWN state.", getName());
			return false;
		}
	}

	protected abstract boolean doStart();

	@Override
	public boolean stop() {
		if (state == State.STOPPED) {
			logger.warn("Module '{}' is already STOPPED. Not doing anything.", getName());
			return true;
		}
		// We do not care about any other state, just stop the module because it is requested.
		if (doStop()) {
			state = State.STOPPED;
			logger.info("Module '{}' is STOPPED.", getName());
			return true;
		} else {
			state = State.UNKNOWN;
			logger.error("Failed to STOP module '{}'. Module is in UNKNOWN state.", getName());
			return false;
		}
	}

	protected abstract boolean doStop();

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (state == State.STARTED) {
			logger.info("Module '{}' destroyed but still running. Stopping in finalize().", getName());
			stop();
		}
	}

}
