package hu.hgj.sceletus.test.queues;

import hu.hgj.sceletus.module.ModuleManager;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.WithTopic;
import hu.hgj.sceletus.queue.simple.SimpleTopicQueue;
import hu.hgj.sceletus.test.queues.helpers.Gatherer;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class QueueTests {

	static final int numberOfElements = 1000;
	static final int sleepTimeMillis = 100;

	@Test
	public void integerSimpleTopicQueueTest() {
		TopicQueue<Object, Integer> integerTopicQueue = new SimpleTopicQueue<>("integerTopicQueue", 1, false, false);
		ArrayList<WithTopic<Object, Integer>> integerStorage = new ArrayList<>();
		Gatherer<Object, Integer> integerGatherer = new Gatherer<>(integerStorage);
		integerTopicQueue.subscribe(integerGatherer, SimpleTopicQueue::catchAllFilter);
		integerTopicQueue.start();
		for (int i = 0; i < numberOfElements; i++) {
			integerTopicQueue.add(null, i);
		}
		// Wait for queue to become empty
		try {
			ModuleManager.waitForEmptyQueue(integerTopicQueue, sleepTimeMillis);
		} catch (InterruptedException exception) {
			exception.printStackTrace();
		}
		for (int i = 0; i < numberOfElements; i++) {
			assertTrue(String.format("integerStorage should contain number %d", i), integerStorage.contains(new WithTopic<>(null, i)));
		}
	}

}
