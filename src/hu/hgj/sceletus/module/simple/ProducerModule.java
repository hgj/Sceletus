package hu.hgj.sceletus.module.simple;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.module.ModuleManager;
import hu.hgj.sceletus.module.MultiThreadedModule;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.WithTopic;

import java.util.List;

/**
 * Base class for creating producer like modules.
 * <p>
 * The module has an output queue configured with the {@code output} and
 * {@code sleepTime} configuration values, where {@code output} is the name of
 * the output queue and {@code sleepTime} is the amount to sleep in seconds
 * between each call to {@link #produceOutput()}.
 * See {@link #DEFAULT_SLEEP_TIME} for default value.
 * <p>
 * The module executes {@link #produceOutput()} after {@code sleepTime} element
 * from the input queue. This method should return the new, converted
 * element(s) or null on failure.
 *
 * @param <O> The type of the output element.
 */
public abstract class ProducerModule<O> extends MultiThreadedModule {

	public static final long DEFAULT_SLEEP_TIME = 60;

	protected long sleepTimeSeconds = DEFAULT_SLEEP_TIME;
	protected long sleepTimeNano = sleepTimeSeconds * 1_000_000_000;

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
			// Use default
		}
		return true;
	}

	@Override
	public int getNumberOfThreads() {
		return 1;
	}

	@Override
	protected void main(int threadID) {
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
				long sleepNeeded = (sleepTimeNano - (System.nanoTime() - lastRunEnded)) / 1_000_000;
				Thread.sleep(sleepNeeded);
			} catch (InterruptedException exception) {
				logger.warn("Interrupted while sleeping.", exception);
			}
		}
	}

	/**
	 * Override to produce output elements.
	 *
	 * @return The list of new, produced elements (can be empty) or null on
	 * failure.
	 */
	protected abstract List<WithTopic<O>> produceOutput();

}
