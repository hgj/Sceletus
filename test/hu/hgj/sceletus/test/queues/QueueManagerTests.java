package hu.hgj.sceletus.test.queues;

import hu.hgj.sceletus.SimpleQueue;
import hu.hgj.sceletus.QueueManager;
import hu.hgj.sceletus.TopicQueue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QueueManagerTests {

	@Test
	public void registerQueue() {
		SimpleQueue<Integer> integerSimpleQueue = new SimpleQueue<>(1);
		String queueName = "testIntegerQueue";
		assertTrue("QueueManager should return true", QueueManager.registerQueue(queueName, integerSimpleQueue));
		SimpleQueue<Integer> retrievedSimpleQueue = (SimpleQueue<Integer>) QueueManager.getQueue(queueName);
		assertEquals("Retrieved SimpleQueue should equal to registered SimpleQueue", integerSimpleQueue, retrievedSimpleQueue);
	}

	@Test
	public void registerTopicQueue() {
		TopicQueue<Integer> integerTopicQueue = new TopicQueue<>(1);
		String queueName = "testIntegerTopicQueue";
		assertTrue("QueueManager should return true", QueueManager.registerTopicQueue(queueName, integerTopicQueue));
		TopicQueue<Integer> retrievedQueue = (TopicQueue<Integer>) QueueManager.getTopicQueue(queueName);
		assertEquals("Retrieved SimpleQueue should equal to registered SimpleQueue", integerTopicQueue, retrievedQueue);
	}

}
