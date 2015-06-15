package hu.hgj.sceletus.test.modules.implementations;

import org.slf4j.LoggerFactory;

import java.util.Set;

public class LongRunningSingleThreadedModule extends SimpleSingleThreadedModule {

	public LongRunningSingleThreadedModule(Set<Integer> outputSet) {
		super(outputSet);
	}

	@Override
	protected void main() {
		while (running) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException exception) {
				LoggerFactory.getLogger(this.getClass()).info("Interrupted while sleeping.");
			}
		}
		outputSet.add(0);
	}

}
