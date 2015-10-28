package hu.hgj.sceletus.module;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.TopicQueueListener;
import hu.hgj.sceletus.queue.WithTopic;
import hu.hgj.sceletus.queue.simple.PatternFilter;
import hu.hgj.sceletus.queue.simple.SimpleTopicQueue;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
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
 * @param <IT> The type of the input topic.
 * @param <IE> The type of the input element.
 * @param <OT> The type of the output topic.
 * @param <OE> The type of the output element.
 */
public abstract class ConverterModule<IT, IE, OT, OE> extends AbstractModuleAdapter implements TopicQueueListener<IT, IE> {

	protected TopicQueue<IT, IE> inputQueue = null;
	protected TopicQueue<OT, OE> outputQueue = null;

	public ConverterModule(String name) {
		super(name);
	}

	public ConverterModule(String name, TopicQueue<IT, IE> inputQueue, TopicQueue<OT, OE> outputQueue) {
		super(name);
		this.inputQueue = inputQueue;
		this.inputQueue.subscribe(this, getInputQueueFilters());
		this.outputQueue = outputQueue;
	}

	public ConverterModule(String name, TopicQueue<IT, IE> inputQueue, Predicate<IT> inputQueueFilters, TopicQueue<OT, OE> outputQueue) {
		super(name);
		this.inputQueue = inputQueue;
		this.inputQueue.subscribe(this, inputQueueFilters);
		this.outputQueue = outputQueue;
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
			inputQueue = ModuleManager.getConfiguredQueue(configuration, "$.input");
			try {
				List<String> filters = JsonPath.read(configuration, "$.inputFilters");
				Set<Pattern> patterns = filters.stream().distinct().map(Pattern::compile).collect(Collectors.toSet());
				PatternFilter<IT> patternFilter = new PatternFilter<>(patterns);
				inputQueue.subscribe(this, patternFilter);
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
	public boolean handleElement(WithTopic<IT, IE> elementWithTopic) {
		List<WithTopic<OT, OE>> outputElements = null;
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
	 * @return The list of new, converted elements (can be empty) or null on
	 * failure.
	 */
	protected abstract List<WithTopic<OT, OE>> convertElement(WithTopic<IT, IE> inputElement);

}
