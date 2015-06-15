package hu.hgj.sceletus;

public interface Module {

	enum State {
		UNKNOWN,
		RESET,
		STARTED,
		STOPPED
	}

	State getState();

	boolean reset();

	boolean start();

	boolean stop();

}
