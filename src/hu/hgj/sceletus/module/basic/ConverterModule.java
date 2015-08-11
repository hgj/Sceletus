package hu.hgj.sceletus.module.basic;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.module.AbstractModuleAdapter;
import hu.hgj.sceletus.module.ModuleManager;
import hu.hgj.sceletus.queue.SimpleTopicQueue;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.TopicQueueListener;
import hu.hgj.sceletus.queue.WithTopic;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ConverterModule<I, O> extends AbstractModuleAdapter implements TopicQueueListener<I> {

	protected TopicQueue<I> inputQueue = null;
	protected TopicQueue<O> outputQueue = null;

	public ConverterModule(String name) {
		super(name);
	}

	protected Set<String> getInputQueueFilters() {
		return SimpleTopicQueue.catchAllFilter;
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

	protected abstract List<WithTopic<O>> convertElement(WithTopic<I> inputElement);

}
