package hu.hgj.sceletus.modules.implementations;

import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Set;

public class ForeverRunningSingleThreadedModule extends SimpleSingleThreadedModule {

	public ForeverRunningSingleThreadedModule(String name, Set<Integer> outputSet) {
		super(name, outputSet);
		this.threadJoinTimeout = Duration.ofSeconds(1);
	}

	@Override
	protected void main() {
		while (true) {
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				LoggerFactory.getLogger(this.getClass()).info("Interrupted while sleeping, but we will not wake up!");
			}
		}
	}

}
