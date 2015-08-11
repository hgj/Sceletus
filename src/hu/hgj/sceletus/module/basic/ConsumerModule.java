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

public abstract class ConsumerModule<I> extends AbstractModuleAdapter implements TopicQueueListener<I> {

	protected TopicQueue<I> inputQueue = null;

	public ConsumerModule(String name) {
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

	protected abstract boolean consumeElement(WithTopic<I> inputElement);

}
