package hu.hgj.sceletus.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * This implementation of the {@link Module} interface allows the following
 * transitions of the module's state:
 * <li>{@link State#UNKNOWN} -> {@link State#RESET}
 * <li>{@link State#RESET} -> {@link State#STARTED}
 * <li>{@link State#STARTED} -> {@link State#STOPPED}
 * <li>{@link State#STOPPED} -> {@link State#RESET}
 * <li>any state -> {@link State#UNKNOWN}
 */
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
				// Not logging here about the stop() call, as stop() logs.
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
				// Not logging here about the reset() call, as reset() logs.
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
		// Just stop the module because it is requested.
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
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || getClass() != other.getClass()) return false;
		AbstractModule otherAbstractModule = (AbstractModule) other;
		return Objects.equals(name, otherAbstractModule.name) &&
				Objects.equals(state, otherAbstractModule.state);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, state);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{name='" + name
				+ "', state='" + state
				+ "'}";
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (state == State.STARTED) {
			logger.info("Module '{}' destroyed but still in STARTED state. Stopping in finalize().", getName());
			stop();
		}
	}

}
