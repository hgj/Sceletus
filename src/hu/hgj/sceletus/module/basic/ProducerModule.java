package hu.hgj.sceletus.module.basic;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.module.ModuleManager;
import hu.hgj.sceletus.module.MultiThreadedModule;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.WithTopic;

import java.util.List;

public abstract class ProducerModule<O> extends MultiThreadedModule {

	protected int sleepTimeSeconds = 30;

	protected TopicQueue<O> outputQueue;

	public ProducerModule(String name) {
		super(name);
	}

	@Override
	public boolean updateConfiguration(Object configuration) {
		if (!super.updateConfiguration(configuration)) {
			return false;
		}
		try {
			outputQueue = ModuleManager.getConfiguredQueue(configuration, "$.output");
		} catch (Exception exception) {
			logger.error("Failed to configure output queue.", exception);
		}
		try {
			sleepTimeSeconds = JsonPath.read(configuration, "$.sleepTime");
		} catch (PathNotFoundException ignored) {
			// We default to 30 seconds
		}
		return true;
	}

	@Override
	public int getNumberOfThreads() {
		return 1;
	}

	@Override
	protected void main(int threadID) {
		long sleepTimeMilli = 1_000 * sleepTimeSeconds;
		long sleepTimeNano = sleepTimeMilli * 1_000_000;
		long lastRunEnded = System.nanoTime() - sleepTimeNano - 10;
		while (running) {
			// Produce output if we did sleep enough
			if (System.nanoTime() - lastRunEnded > sleepTimeNano) {
				List<WithTopic<O>> outputs = null;
				try {
					outputs = produceOutput();
				} catch (Throwable throwable) {
					logger.warn("Exception caught while trying to produce output.", throwable);
				}
				lastRunEnded = System.nanoTime();
				if (outputs == null) {
					logger.warn("Failed to produce output.");
				} else {
					outputs.forEach(outputQueue::add);
				}
			}
			// Sleep
			try {
				logger.debug("Sleeping for {} seconds.", sleepTimeSeconds);
				Thread.sleep(sleepTimeMilli);
			} catch (InterruptedException exception) {
				logger.warn("Interrupted while sleeping.", exception);
			}
		}
	}

	protected abstract List<WithTopic<O>> produceOutput();

}
