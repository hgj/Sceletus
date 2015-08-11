package hu.hgj.sceletus.test.queues;

import hu.hgj.sceletus.module.ModuleManager;
import hu.hgj.sceletus.queue.SimpleTopicQueue;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.WithTopic;
import hu.hgj.sceletus.test.queues.helpers.Gatherer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertTrue;

public class QueueTests {

	static final int numberOfElements = 1000;
	static final int sleepTimeMillis = 100;

	@Test
	public void integerSimpleTopicQueueTest() {
		TopicQueue<Integer> integerTopicQueue = new SimpleTopicQueue<>("integerTopicQueue", 1, false, false);
		ArrayList<WithTopic<Integer>> integerStorage = new ArrayList<>();
		Gatherer<Integer> integerGatherer = new Gatherer<>(integerStorage);
		String topic = "integer";
		integerTopicQueue.subscribe(integerGatherer, new LinkedHashSet<String>() {{
			add(topic);
		}});
		integerTopicQueue.start();
		for (int i = 0; i < numberOfElements; i++) {
			integerTopicQueue.add(topic, i);
		}
		// Wait for queue to become empty
		try {
			ModuleManager.waitForEmptyQueue(integerTopicQueue, sleepTimeMillis);
		} catch (InterruptedException exception) {
			exception.printStackTrace();
		}
		for (int i = 0; i < numberOfElements; i++) {
			assertTrue(String.format("integerStorage should contain number %d", i), integerStorage.contains(new WithTopic<>(topic, i)));
		}
	}

}
