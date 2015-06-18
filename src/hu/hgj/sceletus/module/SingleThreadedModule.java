package hu.hgj.sceletus.module;

public abstract class SingleThreadedModule extends MultiThreadedModule {

	public SingleThreadedModule(String name) {
		super(name);
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
