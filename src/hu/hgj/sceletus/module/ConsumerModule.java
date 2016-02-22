package hu.hgj.sceletus.module;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.TopicQueueListener;
import hu.hgj.sceletus.queue.WithTopic;
import hu.hgj.sceletus.queue.simple.PatternFilter;
import hu.hgj.sceletus.queue.simple.PatternMapFilter;
import hu.hgj.sceletus.queue.simple.SimpleTopicQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Base class for creating consumer like modules.
 * <p>
 * The module has an input queue configured with the {@code inputQueue}. Filters
 * for the input queue can be configured with {@code inputFilter} for a single
 * regex or {@code inputFilters} for a list of regexes. If the topics are maps,
 * you can also use the {@code inputFilterMap} to configure a
 * {@link PatternMapFilter}.
 * <p>
 * The module executes {@link #handleElement(WithTopic)} for each received
 * element from the input queue.
 *
 * @param <IT> The type of the consumed topic.
 * @param <IE> The type of the consumed element.
 */
public abstract class ConsumerModule<IT, IE> extends AbstractModuleAdapter implements TopicQueueListener<IT, IE> {

	private static final Logger logger = LoggerFactory.getLogger(ConsumerModule.class);

	protected TopicQueue<IT, IE> inputQueue = null;

	public ConsumerModule(String name) {
		super(name);
	}

	public ConsumerModule(String name, TopicQueue<IT, IE> inputQueue) {
		super(name);
		this.inputQueue = inputQueue;
		this.inputQueue.subscribe(this, getInputQueueFilters());
	}

	public ConsumerModule(String name, TopicQueue<IT, IE> inputQueue, Predicate<IT> inputQueueFilters) {
		super(name);
		this.inputQueue = inputQueue;
		this.inputQueue.subscribe(this, inputQueueFilters);
	}

	protected Predicate<IT> getInputQueueFilters() {
		return SimpleTopicQueue::catchAllFilter;
	}

	@Override
	public boolean updateConfiguration(Object configuration) {
		if (!super.updateConfiguration(configuration)) {
			return false;
		}
		try {
			inputQueue = ModuleManager.getConfiguredQueue(configuration, "$.inputQueue");
		} catch (Exception exception) {
			logger.error("Failed to configure input queue.", exception);
			return false;
		}
		if (inputQueue == null) {
			logger.error("Input queue does not exist.");
			return false;
		}
		try {
			Map<String, String> filterMap = JsonPath.read(configuration, "$.inputFilterMap");
			PatternMapFilter<Map<String, Object>> patternMapFilter = PatternMapFilter.fromRegexMap(filterMap);
			try {
				inputQueue.subscribe(this, (Predicate<IT>) patternMapFilter);
			} catch (ClassCastException exception) {
				logger.error("Failed to subscribe with inputFilterMap as topic is not a Map.", exception);
				return false;
			}
		} catch (PathNotFoundException filterMapException) {
			try {
				List<String> filters = JsonPath.read(configuration, "$.inputFilters");
				PatternFilter<IT> patternFilter = PatternFilter.fromRegexSet(new HashSet<>(filters));
				inputQueue.subscribe(this, patternFilter);
			} catch (PathNotFoundException filtersException) {
				try {
					String filter = JsonPath.read(configuration, "$.inputFilter");
					PatternFilter<IT> patternFilter = PatternFilter.fromRegexSet(Collections.singleton(filter));
					inputQueue.subscribe(this, patternFilter);
				} catch (PathNotFoundException filterException) {
					logger.warn("No filter found for input '{}', using default filter (accept all).", inputQueue.getName());
					inputQueue.subscribe(this, getInputQueueFilters());
				}
			}
		}
		logger.info("Updated configuration: inputQueue={}", inputQueue.getName());
		return true;
	}

	@Override
	public boolean handleElement(WithTopic<IT, IE> elementWithTopic) {
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
	protected abstract boolean consumeElement(WithTopic<IT, IE> inputElement);

}
