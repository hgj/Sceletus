package hu.hgj.sceletus;

import org.slf4j.LoggerFactory;

public abstract class AbstractModule implements Module {

	private State state = State.UNKNOWN;

	@Override
	public State getState() {
		return state;
	}

	@Override
	public boolean reset() {
		if (state == State.RESET) {
			LoggerFactory.getLogger(this.getClass()).warn("Module already RESET. Not doing anything.");
			return true;
		}
		if (state == State.STARTED) {
			LoggerFactory.getLogger(this.getClass()).warn("Module is STARTED. Stopping before resetting.");
			if (!stop()) {
				// Not logging here about the stop() call.
				return false;
			}
		}
		if (doReset()) {
			state = State.RESET;
			LoggerFactory.getLogger(this.getClass()).info("Module is RESET.");
			return true;
		} else {
			state = State.UNKNOWN;
			LoggerFactory.getLogger(this.getClass()).error("Failed to RESET module. Module is in UNKNOWN state.");
			return false;
		}
	}

	protected abstract boolean doReset();

	@Override
	public boolean start() {
		if (state == State.STARTED) {
			LoggerFactory.getLogger(this.getClass()).warn("Module already STARTED. Not doing anything.");
			return true;
		}
		if (state != State.RESET) {
			LoggerFactory.getLogger(this.getClass()).warn("Module is not RESET. Resetting before starting.");
			if (!reset()) {
				// Not logging here about the reset() call.
				return false;
			}
		}
		if (doStart()) {
			state = State.STARTED;
			LoggerFactory.getLogger(this.getClass()).info("Module is STARTED.");
			return true;
		} else {
			state = State.UNKNOWN;
			LoggerFactory.getLogger(this.getClass()).error("Failed to START module. Module is in UNKNOWN state.");
			return false;
		}
	}

	protected abstract boolean doStart();

	@Override
	public boolean stop() {
		if (state == State.STOPPED) {
			LoggerFactory.getLogger(this.getClass()).warn("Module already STOPPED. Not doing anything.");
			return true;
		}
		// We do not care about any other state, just stop the module because it is requested.
		if (doStop()) {
			state = State.STOPPED;
			LoggerFactory.getLogger(this.getClass()).info("Module is STOPPED.");
			return true;
		} else {
			state = State.UNKNOWN;
			LoggerFactory.getLogger(this.getClass()).error("Failed to STOP module. Module is in UNKNOWN state.");
			return false;
		}
	}

	protected abstract boolean doStop();

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (state == State.STARTED) {
			LoggerFactory.getLogger(this.getClass()).info("Module destroyed but still running. Stopping in finalize().");
			stop();
		}
	}

}
