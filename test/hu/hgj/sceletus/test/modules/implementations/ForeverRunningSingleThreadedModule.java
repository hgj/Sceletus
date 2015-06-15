package hu.hgj.sceletus.test.modules.implementations;

import org.slf4j.LoggerFactory;

import java.util.Set;

public class ForeverRunningSingleThreadedModule extends SimpleSingleThreadedModule {

	public ForeverRunningSingleThreadedModule(Set<Integer> outputSet) {
		super(outputSet);
	}

	@Override
	protected void main() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LoggerFactory.getLogger(this.getClass()).info("Interrupted while sleeping, but we will not wake up!");
			}
		}
	}

}
