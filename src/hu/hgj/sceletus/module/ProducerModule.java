package hu.hgj.sceletus.module;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.WithTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Base class for creating producer like modules.
 * <p>
 * The module has an output queue configured with the {@code output} and {@code
 * sleepTime} configuration values, where {@code output} is the name of the
 * output queue and {@code sleepDuration} is the amount to sleep (in ISO-8601
 * format) between each call to {@link #produceOutput()}. See {@link
 * #DEFAULT_SLEEP_DURATION} for default value.
 * <p>
 * The module executes {@link #produceOutput()} after {@code sleepTime} element
 * from the input queue. This method should return the new, converted element(s)
 * or null on failure.
 *
 * @param <O> The type of the output element.
 */
public abstract class ProducerModule<T, O> extends MultiThreadedModule {

	private static final Logger logger = LoggerFactory.getLogger(ProducerModule.class);

	public static final Duration DEFAULT_SLEEP_DURATION = Duration.ofSeconds(60);

	protected Duration sleepDuration = DEFAULT_SLEEP_DURATION;

	protected TopicQueue<T, O> outputQueue;

	public ProducerModule(String name) {
		super(name);
	}

	@Override
	public boolean updateConfiguration(Object configuration) {
		if (!super.updateConfiguration(configuration)) {
			return false;
		}
		try {
			outputQueue = ModuleManager.getConfiguredQueue(configuration, "$.outputQueue");
		} catch (Exception exception) {
			logger.error("Failed to configure output queue.", exception);
			return false;
		}
		if (outputQueue == null) {
			logger.error("Output queue does not exist.");
			return false;
		}
		try {
			String sleepDurationString = JsonPath.read(configuration, "$.sleepDuration");
			try {
				sleepDuration = Duration.parse(sleepDurationString);
			} catch (DateTimeParseException exception) {
				logger.error("Failed to parse '{}' as an ISO-8601 duration.", sleepDurationString, exception);
				return false;
			} catch (ArithmeticException exception) {
				logger.error("Configured duration is too big to fit in a long.", exception);
				return false;
			}
		} catch (PathNotFoundException ignored) {
			// Use default
		}
		logger.info("Updated configuration: outputQueue={}, sleepDuration={}", outputQueue.getName(), sleepDuration);
		return true;
	}

	@Override
	protected void main(int threadID) {
		// Fake last run ended a portion of the time window ago, based on thread ID
		// This way, all threads should start working in a different part of the sleep window
		long lastRunEnded = System.nanoTime() - ((sleepDuration.toNanos() / getNumberOfThreads()) * (threadID + 1));
		while (running) {
			// Produce output if we did sleep enough
			if (System.nanoTime() - lastRunEnded > sleepDuration.toNanos()) {
				List<WithTopic<T, O>> outputs = null;
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
				long sleepNeededMilliseconds = TimeUnit.MILLISECONDS.convert(
						sleepDuration.toNanos() - (System.nanoTime() - lastRunEnded),
						TimeUnit.NANOSECONDS
				) + 1;
				logger.debug("Sleeping for a total of {} ({} milliseconds right now).", sleepDuration.toString(), sleepNeededMilliseconds);
				Thread.sleep(sleepNeededMilliseconds);
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
	protected abstract List<WithTopic<T, O>> produceOutput();

}
