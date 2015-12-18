package hu.hgj.sceletus.module;

import java.time.Duration;

public abstract class SingleThreadedModule extends MultiThreadedModule {

	public SingleThreadedModule(String name) {
		super(name);
	}

	public SingleThreadedModule(String name, Duration threadJoinTimeoutMilli, Duration threadRestartSleepMilli) {
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
