package hu.hgj.sceletus.module.basic;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.module.MultiThreadedModule;
import hu.hgj.sceletus.queue.QueueManager;
import hu.hgj.sceletus.queue.SimpleTopicQueue;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.WithTopic;

import java.util.List;
import java.util.function.Function;

public abstract class ProducerModule<O> extends MultiThreadedModule {

	protected int sleepTimeSeconds = 30;

	protected TopicQueue<O> outputQueue;

	public ProducerModule(String name) {
		super(name);
	}

	protected boolean configureOutputQueue(Object configuration, Function<String, ? extends TopicQueue<O>> queueProvider) {
		return configureOutputQueue(configuration, "$.output", queueProvider);
	}

	protected boolean configureOutputQueue(Object configuration, String path, Function<String, ? extends TopicQueue<O>> queueProvider) {
		outputQueue = QueueManager.configureQueue(configuration, path, queueProvider);
		return outputQueue != null;
	}

	@Override
	public boolean updateConfiguration(Object configuration) {
		if (!super.updateConfiguration(configuration)) {
			return false;
		}
		// We have absolutely no clue what will happen with the output queue...
		if (!configureOutputQueue(configuration, s -> new SimpleTopicQueue<>(s, 1, true, false))) {
			return false;
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
			// Get all tickets
			if (System.nanoTime() - lastRunEnded > sleepTimeNano) {
				List<WithTopic<O>> outputs = produceOutput();
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
