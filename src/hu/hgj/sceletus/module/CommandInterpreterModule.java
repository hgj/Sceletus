package hu.hgj.sceletus.module;

import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.WithTopic;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public abstract class CommandInterpreterModule<T> extends MultiConverterModule<T, List<String>, T, String> {

	public CommandInterpreterModule(String name) {
		super(name);
	}

	public CommandInterpreterModule(String name, TopicQueue<T, List<String>> inputQueue, TopicQueue<T, String> outputQueue) {
		super(name, inputQueue, outputQueue);
	}

	public CommandInterpreterModule(String name, TopicQueue<T, List<String>> inputQueue, Predicate<T> inputQueueFilter, TopicQueue<T, String> outputQueue) {
		super(name, inputQueue, inputQueueFilter, outputQueue);
	}

	public CommandInterpreterModule(String name, List<TopicQueue<T, List<String>>> inputQueues, List<TopicQueue<T, String>> outputQueues) {
		super(name, inputQueues, outputQueues);
	}

	public CommandInterpreterModule(String name, List<TopicQueue<T, List<String>>> inputQueues, Predicate<T> inputQueueFilter, List<TopicQueue<T, String>> outputQueues) {
		super(name, inputQueues, inputQueueFilter, outputQueues);
	}

	public CommandInterpreterModule(String name, List<TopicQueue<T, List<String>>> inputQueues, List<Predicate<T>> inputQueueFilters, List<TopicQueue<T, String>> outputQueues) {
		super(name, inputQueues, inputQueueFilters, outputQueues);
	}

	public abstract String interpretCommand(List<String> arguments);

	public WithTopic<T, String> interpretCommandWithTopic(WithTopic<T, List<String>> argumentsWithTopic) {
		return new WithTopic<>(
				argumentsWithTopic.topic,
				interpretCommand(argumentsWithTopic.element)
		);
	}

	@Override
	protected List<WithTopic<T, String>> convertElement(WithTopic<T, List<String>> inputElementWithTopic) {
		return Collections.singletonList(interpretCommandWithTopic(inputElementWithTopic));
	}

}
