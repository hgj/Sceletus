package hu.hgj.sceletus.test.queues;

import hu.hgj.sceletus.queue.SimpleQueue;
import hu.hgj.sceletus.queue.QueueManager;
import hu.hgj.sceletus.queue.TopicQueue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QueueManagerTests {

	@Test
	public void registerQueue() {
		SimpleQueue<Integer> integerSimpleQueue = new SimpleQueue<>("testIntegerQueue", 1, false);
		assertTrue("QueueManager should return true", QueueManager.simpleQueueRegistry.register(integerSimpleQueue));
		SimpleQueue<Integer> retrievedSimpleQueue = (SimpleQueue<Integer>) QueueManager.simpleQueueRegistry.get(integerSimpleQueue.getName());
		assertEquals("Retrieved SimpleQueue should equal to registered SimpleQueue", integerSimpleQueue, retrievedSimpleQueue);
	}

	@Test
	public void registerTopicQueue() {
		TopicQueue<Integer> integerTopicQueue = new TopicQueue<>("testIntegerTopicQueue", 1, false);
		assertTrue("QueueManager should return true", QueueManager.topicQueueRegistry.register(integerTopicQueue));
		TopicQueue<Integer> retrievedQueue = (TopicQueue<Integer>) QueueManager.topicQueueRegistry.get(integerTopicQueue.getName());
		assertEquals("Retrieved SimpleQueue should equal to registered SimpleQueue", integerTopicQueue, retrievedQueue);
	}

}
