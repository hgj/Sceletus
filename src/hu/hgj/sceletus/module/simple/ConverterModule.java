package hu.hgj.sceletus.module.simple;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.module.AbstractModuleAdapter;
import hu.hgj.sceletus.module.ModuleManager;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.TopicQueueListener;
import hu.hgj.sceletus.queue.WithTopic;
import hu.hgj.sceletus.queue.simple.SimpleTopicQueue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base class for creating converter like modules.
 * <p>
 * The module has an input and an output queue configured with the
 * {@code input}, {@code inputFilters} and {@code output} configuration values
 * where {@code input} and {@code output} are the names of the input and output
 * queues respectively and {@code inputFilters} is a list of filters for the
 * input queue.
 * <p>
 * The module executes {@link #convertElement(WithTopic)} for each received
 * element from the input queue. The method should return the new, converted
 * element(s) or null on failure.
 *
 * @param <I> The type of the input element.
 * @param <O> The type of the output element.
 */
public abstract class ConverterModule<I, O> extends AbstractModuleAdapter implements TopicQueueListener<I> {

	protected TopicQueue<I> inputQueue = null;
	protected TopicQueue<O> outputQueue = null;

	public static final Set<String> DEFAULT_INPUT_QUEUE_FILTERS = SimpleTopicQueue.catchAllFilter;

	public ConverterModule(String name) {
		super(name);
	}

	public ConverterModule(String name, TopicQueue<I> inputQueue, TopicQueue<O> outputQueue) {
		super(name);
		this.inputQueue = inputQueue;
		this.inputQueue.subscribe(this, getInputQueueFilters());
		this.outputQueue = outputQueue;
	}

	public ConverterModule(String name, TopicQueue<I> inputQueue, Set<String> inputQueueFilters, TopicQueue<O> outputQueue) {
		super(name);
		this.inputQueue = inputQueue;
		this.inputQueue.subscribe(this, inputQueueFilters);
		this.outputQueue = outputQueue;
	}

	protected Set<String> getInputQueueFilters() {
		return DEFAULT_INPUT_QUEUE_FILTERS;
	}

	@Override
	public boolean updateConfiguration(Object configuration) {
		if (!super.updateConfiguration(configuration)) {
			return false;
		}
		try {
			inputQueue = ModuleManager.getConfiguredQueue(configuration, "$.input");
			try {
				List<String> filters = JsonPath.read(configuration, "$.inputFilters");
				inputQueue.subscribe(this, filters.stream().distinct().collect(Collectors.toSet()));
			} catch (PathNotFoundException ignored) {
				inputQueue.subscribe(this, getInputQueueFilters());
			}
		} catch (Exception exception) {
			logger.error("Failed to configure input queue.", exception);
		}
		try {
			outputQueue = ModuleManager.getConfiguredQueue(configuration, "$.output");
		} catch (Exception exception) {
			logger.error("Failed to configure output queue.", exception);
		}
		return true;
	}

	@Override
	public boolean handleElement(WithTopic<I> elementWithTopic) {
		List<WithTopic<O>> outputElements = null;
		try {
			outputElements = convertElement(elementWithTopic);
		} catch (Throwable throwable) {
			logger.warn("Exception caught while trying to convert elements.", throwable);
		}
		if (outputElements == null) {
			logger.warn("Failed to convert elements.");
			return false;
		} else {
			// We can not do much about an unsuccessful add here... :(
			outputElements.forEach(outputQueue::add);
			if (outputElements.size() > 0) {
				logger.info("Converted {} elements from queue '{}' to queue '{}'.", outputElements.size(), inputQueue.getName(), outputQueue.getName());
			}
			return true;
		}
	}

	/**
	 * Override to work with (convert) the incoming elements.
	 *
	 * @param inputElement The received element.
	 *
	 * @return The list of new, converted elements (can be empty) or null on
	 * failure.
	 */
	protected abstract List<WithTopic<O>> convertElement(WithTopic<I> inputElement);

}
