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
 * Base class for creating consumer like modules.
 * <p>
 * The module has an input queue configured with the {@code input} and
 * {@code inputFilters} configuration values, where {@code input} is the name
 * of the input queue and {@code inputFilters} is a list of filters.
 * <p>
 * The module executes {@link #handleElement(WithTopic)} for each received
 * element from the input queue.
 *
 * @param <I> The type of the consumed element.
 */
public abstract class ConsumerModule<I> extends AbstractModuleAdapter implements TopicQueueListener<I> {

	protected TopicQueue<I> inputQueue = null;

	public static Set<String> DEFAULT_INPUT_QUEUE_FILTERS = SimpleTopicQueue.catchAllFilter;

	public ConsumerModule(String name) {
		super(name);
	}

	public ConsumerModule(String name, TopicQueue<I> inputQueue) {
		super(name);
		this.inputQueue = inputQueue;
		this.inputQueue.subscribe(this, getInputQueueFilters());
	}

	public ConsumerModule(String name, TopicQueue<I> inputQueue, Set<String> inputQueueFilters) {
		super(name);
		this.inputQueue = inputQueue;
		this.inputQueue.subscribe(this, inputQueueFilters);
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
		return true;
	}

	@Override
	public boolean handleElement(WithTopic<I> elementWithTopic) {
		try {
			return consumeElement(elementWithTopic);
		} catch (Throwable throwable) {
			logger.warn("Exception caught while trying to consume element.", throwable);
			return false;
		}
	}

	/**
	 * Override to work with (consume) the incoming elements.
	 *
	 * @param inputElement The received element.
	 *
	 * @return See {@link TopicQueueListener#handleElement(WithTopic)}.
	 */
	protected abstract boolean consumeElement(WithTopic<I> inputElement);

}
