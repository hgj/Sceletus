package hu.hgj.sceletus.module;

import java.util.Map;

public interface Module {

	String getName();

	enum State {
		UNKNOWN,
		RESET,
		STARTED,
		STOPPED
	}

	State getState();

	boolean updateConfiguration(Map<String, Object> configurationMap);

	boolean reset();

	boolean start();

	boolean stop();

}
