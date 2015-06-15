package hu.hgj.sceletus;

public abstract class SingleThreadedModule extends MultiThreadedModule {

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
