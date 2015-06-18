package hu.hgj.sceletus.module;

import java.util.Map;

public abstract class AbstractModuleAdapter extends AbstractModule {

	public AbstractModuleAdapter(String name) {
		super(name);
	}

	@Override
	public boolean updateConfiguration(Map<String, Object> configurationMap) {
		return true;
	}

	@Override
	protected boolean doReset() {
		return true;
	}

	@Override
	protected boolean doStart() {
		return true;
	}

	@Override
	protected boolean doStop() {
		return true;
	}

}
