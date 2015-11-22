package hu.hgj.sceletus.modules.implementations;

import hu.hgj.sceletus.module.MultiThreadedModule;

import java.util.Set;

public class SimpleMultiThreadedModule extends MultiThreadedModule {

	protected final Set<Integer> outputSet;
	private final int threads;

	public SimpleMultiThreadedModule(String name, Set<Integer> outputSet, int threads) {
		super(name);
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
