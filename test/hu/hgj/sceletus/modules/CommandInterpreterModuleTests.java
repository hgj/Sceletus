package hu.hgj.sceletus.modules;

import hu.hgj.sceletus.module.CommandInterpreterModule;
import hu.hgj.sceletus.modules.implementations.FooBarCommandInterpreterModule;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.WithStringTopic;
import hu.hgj.sceletus.queue.WithTopic;
import hu.hgj.sceletus.queue.simple.SimpleStringTopicQueue;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class CommandInterpreterModuleTests {

	@Test
	public void testDefaultCommandNameFilter() {
		String commandName = "_-foo-Bar_BAZ-_";
		String expectedFilteredCommandName = "foobarbaz";
		String filteredCommandName = CommandInterpreterModule.defaultFilterCommandName(commandName);
		assertEquals(
				"Filtered command name should equal expected string.",
				expectedFilteredCommandName,
				filteredCommandName
		);
	}

	@Test
	public void testFooBarCommandInterpreter() throws InterruptedException {
		// TODO: Rewrite this and other tests as actual configurations
		TopicQueue<String, List<String>> inputQueue = new SimpleStringTopicQueue<>("inputQueue");
		TopicQueue<String, String> outputQueue = new SimpleStringTopicQueue<>("outputQueue");
		FooBarCommandInterpreterModule fooBarCommandInterpreterModule = new FooBarCommandInterpreterModule("FooBar");
		inputQueue.subscribe(elementWithTopic -> outputQueue.add(fooBarCommandInterpreterModule.interpretCommandWithTopic(elementWithTopic)));
		inputQueue.start();
		String topic = "myTopic";
		inputQueue.add(new WithStringTopic<>(topic, Collections.singletonList("notFooBar")));
		WithTopic<String, String> result = outputQueue.getQueue().poll(1, TimeUnit.SECONDS);
		WithTopic<String, String> expected = new WithTopic<>(topic, "foobar");
		assertEquals("Expecting 'foobar' in output queue.", expected, result);
	}

}
