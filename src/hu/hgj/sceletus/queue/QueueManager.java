package hu.hgj.sceletus.queue;

import hu.hgj.sceletus.module.ModuleRegistry;

public class QueueManager {

	public static final ModuleRegistry<SimpleQueue> simpleQueueRegistry = new ModuleRegistry<>();

	public static final ModuleRegistry<TopicQueue> topicQueueRegistry = new ModuleRegistry<>();

	public static void waitForEmptyQueue(Queue queue) throws InterruptedException {
		waitForEmptyQueue(queue, 100);
	}

	public static void waitForEmptyQueue(Queue queue, int sleepTimeMillis) throws InterruptedException {
		while (queue.size() > 0) {
			Thread.sleep(sleepTimeMillis);
		}
	}

}
