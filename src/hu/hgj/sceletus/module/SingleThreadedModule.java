package hu.hgj.sceletus.module;

public abstract class SingleThreadedModule extends MultiThreadedModule {

	public SingleThreadedModule(String name) {
		super(name);
	}

	public SingleThreadedModule(String name, long threadJoinTimeoutMilli, long threadRestartSleepMilli) {
		super(name, threadJoinTimeoutMilli, threadRestartSleepMilli);
	}

	@Override
	protected int getNumberOfThreads() {
		return 1;
	}

	@Override
	protected void main(int threadID) {
		main();
	}

	protected abstract void main();

}
