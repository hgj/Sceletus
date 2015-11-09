package hu.hgj.sceletus.module;

import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.WithTopic;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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

	/**
	 * Convert commands to "standard" format: Lower case and remove {@code -} or
	 * {@code _} characters.
	 *
	 * @param commandName The original command name.
	 *
	 * @return The converted command name.
	 */
	public static String defaultFilterCommandName(String commandName) {
		return commandName.toLowerCase().replaceAll("[-_]", "");
	}

	protected String filterCommandName(String commandName) {
		return defaultFilterCommandName(commandName);
	}

	protected List<String> getSupportedCommands() {
		return Collections.emptyList();
	}

	protected boolean filterCommand(String command) {
		String filteredCommandName = filterCommandName(command);
		for (String supportedCommand : getSupportedCommands()) {
			if (filteredCommandName.equals(supportedCommand)) {
				return true;
			}
		}
		logger.debug("Command '{}' does not seem to be supported.", command);
		return false;
	}

	@Override
	protected Predicate<T> getDefaultInputQueueFilter() {
		List<String> supportedCommands = getSupportedCommands();
		if (supportedCommands == null || supportedCommands.isEmpty()) {
			return super.getDefaultInputQueueFilter();
		} else {
			return topic -> {
				String commandName = null;
				try {
					Map<String, Object> topicMap = (Map<String, Object>) topic;
					commandName = (String) topicMap.get("command");
				} catch (ClassCastException ignored) {
					commandName = topic.toString();
				}
				return filterCommand(commandName);
			};
		}
	}

	public abstract String interpretCommand(List<String> arguments);

	public WithTopic<T, String> interpretCommandWithTopic(WithTopic<T, List<String>> argumentsWithTopic) {
		String result = interpretCommand(argumentsWithTopic.element);
		if (result != null) {
			return new WithTopic<>(
					argumentsWithTopic.topic,
					result
			);
		} else {
			return null;
		}
	}

	@Override
	protected List<WithTopic<T, String>> convertElement(WithTopic<T, List<String>> inputElementWithTopic) {
		WithTopic<T, String> withTopicResult = interpretCommandWithTopic(inputElementWithTopic);
		if (withTopicResult != null) {
			return Collections.singletonList(withTopicResult);
		} else {
			return Collections.emptyList();
		}
	}

}
