package hu.hgj.sceletus.module;

public abstract class AbstractModuleAdapter extends AbstractModule {

	public AbstractModuleAdapter(String name) {
		super(name);
	}

	@Override
	public boolean updateConfiguration(Object configuration) {
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
