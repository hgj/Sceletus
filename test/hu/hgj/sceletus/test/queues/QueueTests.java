package hu.hgj.sceletus.test.queues;

import hu.hgj.sceletus.NamedSimpleQueue;
import hu.hgj.sceletus.NamedTopicQueue;
import hu.hgj.sceletus.QueueManager;
import hu.hgj.sceletus.SimpleQueue;
import hu.hgj.sceletus.TopicQueue;
import hu.hgj.sceletus.test.queues.helpers.Gatherer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertTrue;

public class QueueTests {

	static final int numberOfElements = 1000;
	static final int sleepTimeMillis = 100;

	@Test
	public void integerQueueTest() {
		SimpleQueue<Integer> integerSimpleQueue = new NamedSimpleQueue<>("integerSimpleQueue", 1);
		ArrayList<Integer> integerStorage = new ArrayList<>();
		Gatherer<Integer> integerGatherer = new Gatherer<>(integerStorage);
		integerSimpleQueue.subscribe(integerGatherer);
		integerSimpleQueue.start();
		for (int i = 0; i < numberOfElements; i++) {
			integerSimpleQueue.add(i);
		}
		// Wait for queue to become empty
		try {
			QueueManager.waitForEmptyQueue(integerSimpleQueue, sleepTimeMillis);
		} catch (InterruptedException exception) {
			exception.printStackTrace();
		}
		for (int i = 0; i < numberOfElements; i++) {
			assertTrue(String.format("integerStorage should contain number %d", i), integerStorage.contains(i));
		}
	}

	@Test
	public void integerTopicQueueTest() {
		TopicQueue<Integer> integerTopicQueue = new NamedTopicQueue<>("integerTopicQueue", 1);
		ArrayList<Integer> integerStorage = new ArrayList<>();
		Gatherer<Integer> integerGatherer = new Gatherer<>(integerStorage);
		integerTopicQueue.subscribe(integerGatherer, new LinkedHashSet<String>() {{
			add("integer");
		}});
		integerTopicQueue.start();
		for (int i = 0; i < numberOfElements; i++) {
			integerTopicQueue.add("integer", i);
		}
		// Wait for queue to become empty
		try {
			QueueManager.waitForEmptyQueue(integerTopicQueue, sleepTimeMillis);
		} catch (InterruptedException exception) {
			exception.printStackTrace();
		}
		for (int i = 0; i < numberOfElements; i++) {
			assertTrue(String.format("integerStorage should contain number %d", i), integerStorage.contains(i));
		}
	}

}
