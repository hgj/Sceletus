package hu.hgj.sceletus;

public class AbstractModuleAdapter extends AbstractModule {

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
