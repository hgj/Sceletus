package hu.hgj.sceletus.modules.implementations;

import org.slf4j.LoggerFactory;

import java.util.Set;

public class LongRunningMultiThreadedModule extends SimpleMultiThreadedModule {

	public LongRunningMultiThreadedModule(String name, Set<Integer> outputSet, int threads) {
		super(name, outputSet, threads);
	}

	@Override
	protected void main(int threadID) {
		while (running) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException exception) {
				LoggerFactory.getLogger(this.getClass()).info("Interrupted while sleeping.");
			}
		}
		outputSet.add(threadID);
	}

}
