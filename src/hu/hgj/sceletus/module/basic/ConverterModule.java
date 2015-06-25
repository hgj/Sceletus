package hu.hgj.sceletus.module.basic;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.module.AbstractModuleAdapter;
import hu.hgj.sceletus.queue.QueueManager;
import hu.hgj.sceletus.queue.SimpleTopicQueue;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.TopicQueueListener;
import hu.hgj.sceletus.queue.WithTopic;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ConverterModule<I, O> extends AbstractModuleAdapter implements TopicQueueListener<I> {

	protected TopicQueue<I> inputQueue = null;
	protected TopicQueue<O> outputQueue = null;

	public ConverterModule(String name) {
		super(name);
	}

	protected boolean configureInputQueue(Object configuration, Function<String, ? extends TopicQueue<I>> queueProvider) {
		return configureInputQueue(configuration, "$.input", queueProvider);
	}

	protected boolean configureInputQueue(Object configuration, String path, Function<String, ? extends TopicQueue<I>> queueProvider) {
		inputQueue = QueueManager.configureQueue(configuration, path, queueProvider);
		return inputQueue != null;
	}

	protected boolean configureOutputQueue(Object configuration, Function<String, ? extends TopicQueue<O>> queueProvider) {
		return configureOutputQueue(configuration, "$.output", queueProvider);
	}

	protected boolean configureOutputQueue(Object configuration, String path, Function<String, ? extends TopicQueue<O>> queueProvider) {
		outputQueue = QueueManager.configureQueue(configuration, path, queueProvider);
		return outputQueue != null;
	}

	protected Set<String> getInputQueueFilters() {
		return SimpleTopicQueue.catchAllFilter;
	}

	@Override
	public boolean updateConfiguration(Object configuration) {
		if (!super.updateConfiguration(configuration)) {
			return false;
		}
		// Creating queues that allow duplicates (why restrict?),
		// but do not keep elements, as we might be unsuccessful with the conversion.
		if (!configureInputQueue(configuration, queueName -> new SimpleTopicQueue<>(queueName, 1, true, false))) {
			return false;
		} else {
			try {
				List<String> filters = JsonPath.read(configuration, "$.inputFilters");
				inputQueue.subscribe(this, filters.stream().distinct().collect(Collectors.toSet()));
			} catch (PathNotFoundException ignored) {
				inputQueue.subscribe(this, getInputQueueFilters());
			}
		}
		if (!configureOutputQueue(configuration, queueName -> new SimpleTopicQueue<>(queueName, 1, true, false))) {
			return false;
		}
		return true;
	}

	@Override
	public boolean handleElement(WithTopic<I> elementWithTopic) {
		List<WithTopic<O>> outputElements = convertElement(elementWithTopic);
		if (outputElements == null) {
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

	protected abstract List<WithTopic<O>> convertElement(WithTopic<I> inputElement);

}
