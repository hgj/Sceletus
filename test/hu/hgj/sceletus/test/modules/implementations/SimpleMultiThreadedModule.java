package hu.hgj.sceletus.test.modules.implementations;

import hu.hgj.sceletus.MultiThreadedModule;

import java.util.Set;

public class SimpleMultiThreadedModule extends MultiThreadedModule {

	protected final Set<Integer> outputSet;
	private final int threads;

	public SimpleMultiThreadedModule(Set<Integer> outputSet, int threads) {
		this.outputSet = outputSet;
		this.threads = threads;
	}

	@Override
	protected int getNumberOfThreads() {
		return threads;
	}

	@Override
	protected void main(int threadID) {
		outputSet.add(threadID);
	}

}
