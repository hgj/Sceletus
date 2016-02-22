package hu.hgj.sceletus.module;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.WithTopic;
import hu.hgj.sceletus.queue.simple.PatternFilter;
import hu.hgj.sceletus.queue.simple.PatternMapFilter;
import hu.hgj.sceletus.queue.simple.SimpleTopicQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class MultiConverterModule<IT, IE, OT, OE> extends AbstractModuleAdapter {

	private static final Logger logger = LoggerFactory.getLogger(MultiConverterModule.class);

	protected List<TopicQueue<IT, IE>> inputQueues = new ArrayList<>();
	protected List<TopicQueue<OT, OE>> outputQueues = new ArrayList<>();

	public MultiConverterModule(String name) {
		super(name);
	}

	public MultiConverterModule(String name, TopicQueue<IT, IE> inputQueue, TopicQueue<OT, OE> outputQueue) {
		super(name);
		this.inputQueues = new ArrayList<TopicQueue<IT, IE>>(1) {{
			add(inputQueue);
		}};
		subscribeToInputQueues();
		this.outputQueues = new ArrayList<TopicQueue<OT, OE>>(1) {{
			add(outputQueue);
		}};
	}

	public MultiConverterModule(String name, TopicQueue<IT, IE> inputQueue, Predicate<IT> inputQueueFilter, TopicQueue<OT, OE> outputQueue) {
		super(name);
		this.inputQueues = new ArrayList<TopicQueue<IT, IE>>(1) {{
			add(inputQueue);
		}};
		subscribeToInputQueues(inputQueueFilter);
		this.outputQueues = new ArrayList<TopicQueue<OT, OE>>(1) {{
			add(outputQueue);
		}};
	}

	public MultiConverterModule(String name, List<TopicQueue<IT, IE>> inputQueues, List<TopicQueue<OT, OE>> outputQueues) {
		super(name);
		this.inputQueues.addAll(inputQueues);
		subscribeToInputQueues();
		this.outputQueues.addAll(outputQueues);
	}

	public MultiConverterModule(String name, List<TopicQueue<IT, IE>> inputQueues, Predicate<IT> inputQueueFilter, List<TopicQueue<OT, OE>> outputQueues) {
		super(name);
		this.inputQueues.addAll(inputQueues);
		subscribeToInputQueues(inputQueueFilter);
		this.outputQueues.addAll(outputQueues);
	}

	public MultiConverterModule(String name, List<TopicQueue<IT, IE>> inputQueues, List<Predicate<IT>> inputQueueFilters, List<TopicQueue<OT, OE>> outputQueues) {
		super(name);
		this.inputQueues.addAll(inputQueues);
		subscribeToInputQueues(inputQueueFilters);
		this.outputQueues.addAll(outputQueues);
	}

	protected Predicate<IT> getDefaultInputQueueFilter() {
		return SimpleTopicQueue::catchAllFilter;
	}

	protected void subscribeToInputQueues() {
		subscribeToInputQueues(getDefaultInputQueueFilter());
	}

	protected void subscribeToInputQueues(Predicate<IT> inputQueueFilter) {
		subscribeToInputQueues(Collections.singletonList(inputQueueFilter));
	}

	protected void subscribeToInputQueues(List<Predicate<IT>> inputQueueFilters) {
		assert (inputQueueFilters.size() == this.inputQueues.size()
				|| inputQueueFilters.size() == 1);
		Predicate<IT> inputQueueFilter = inputQueueFilters.get(0);
		for (int i = 0; i < this.inputQueues.size(); i++) {
			if (inputQueueFilters.size() > 1) {
				inputQueueFilter = inputQueueFilters.get(i);
			}
			final int inputQueueID = i;
			this.inputQueues.get(i).subscribe(
					inputElementWithTopic -> handleElement(inputQueueID, inputElementWithTopic),
					inputQueueFilter
			);
		}
	}

	@Override
	public boolean updateConfiguration(Object configuration) {
		if (!super.updateConfiguration(configuration)) {
			return false;
		}
		// Configure conversions queue(s)
		try {
			List<Map<String, Object>> conversions = JsonPath.read(configuration, "$.conversions");
			for (int i = 0; i < conversions.size(); i++) {
				final int queueID = i;
				Map<String, Object> conversion = conversions.get(i);
				String inputQueueName = (String) conversion.get("inputQueue");
				String outputQueueName = (String) conversion.get("outputQueue");
				if (inputQueueName == null || outputQueueName == null) {
					logger.error("Input or output queue name missing from a conversion.");
					return false;
				}
				TopicQueue<IT, IE> inputQueue = ModuleManager.getQueue(inputQueueName);
				if (inputQueue == null) {
					logger.error("Input queue '{}' does not exist. Skipping conversion.", inputQueueName);
					continue;
				}
				TopicQueue<OT, OE> outputQueue = ModuleManager.getQueue(outputQueueName);
				if (outputQueue == null) {
					logger.error("Output queue '{}' does not exist. Skipping conversion.", outputQueueName);
					continue;
				}
				inputQueues.add(inputQueue);
				outputQueues.add(outputQueue);
				String inputFilter = (String) conversion.get("inputFilter");
				List<String> inputFilters = (List<String>) conversion.get("inputFilters");
				Map<String, String> inputFilterMap = (Map<String, String>) conversion.get("inputFilterMap");
				Predicate<IT> filter = null;
				if (inputFilter != null) {
					filter = PatternFilter.fromRegexSet(Collections.singleton(inputFilter));
				}
				if (inputFilters != null) {
					filter = PatternFilter.fromRegexSet(new LinkedHashSet<>(inputFilters));
				}
				if (inputFilterMap != null) {
					filter = (Predicate<IT>) PatternMapFilter.fromRegexMap(inputFilterMap);
				}
				if (filter == null) {
					logger.warn("No filter found for input '{}', using default filter (accept all).", inputQueueName);
					filter = SimpleTopicQueue::catchAllFilter;
				}
				inputQueue.subscribe(
						inputElementWithTopic -> handleElement(queueID, inputElementWithTopic),
						filter
				);
			}
		} catch (PathNotFoundException | ClassCastException exception) {
			logger.error("Failed to configure input-output.", exception);
			// We might have some conversions configured, not returning false
		}
		if (inputQueues.isEmpty()) {
			logger.error("Failed to configure any conversion.");
			return false;
		}
		logger.info("Updated configuration: inputQueues={}, outputQueues={}", inputQueues.toString(), outputQueues.toString());
		return true;
	}

	public boolean handleElement(int inputQueueID, WithTopic<IT, IE> elementWithTopic) {
		TopicQueue<IT, IE> inputQueue = inputQueues.get(inputQueueID);
		TopicQueue<OT, OE> outputQueue = outputQueues.get(inputQueueID);
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
	 * @param inputElementWithTopic The received element.
	 *
	 * @return The list of new, converted elements (can be empty) or null on
	 * failure.
	 */
	protected abstract List<WithTopic<OT, OE>> convertElement(WithTopic<IT, IE> inputElementWithTopic);

}
