package hu.hgj.sceletus.module.simple;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.module.ConverterModule;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.WithTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class SerialiserModule extends ConverterModule<Object, Object, Object, String> {

	private static final Logger logger = LoggerFactory.getLogger(SerialiserModule.class);

	protected boolean serialiseTopic = false;

	public SerialiserModule(String name) {
		super(name);
	}

	public SerialiserModule(String name, TopicQueue<Object, Object> inputQueue, TopicQueue<Object, String> outputQueue) {
		super(name, inputQueue, outputQueue);
	}

	public SerialiserModule(String name, TopicQueue<Object, Object> inputQueue, Predicate<Object> inputQueueFilters, TopicQueue<Object, String> outputQueue) {
		super(name, inputQueue, inputQueueFilters, outputQueue);
	}

	@Override
	public boolean updateConfiguration(Object configuration) {
		if (!super.updateConfiguration(configuration)) {
			return false;
		}
		try {
			serialiseTopic = JsonPath.read(configuration, "$.serialiseTopic");
		} catch (PathNotFoundException ignored) {
			// Ignored, using default value
		}
		logger.info("Updated configuration: serialiseTopic={}", serialiseTopic);
		return true;
	}

	@Override
	protected List<WithTopic<Object, String>> convertElement(WithTopic<Object, Object> inputElementWithTopic) {
		return Collections.singletonList(new WithTopic<>(
				serialiseTopic ? inputElementWithTopic.topic.toString() : inputElementWithTopic.topic,
				inputElementWithTopic.element.toString()
		));
	}

}
