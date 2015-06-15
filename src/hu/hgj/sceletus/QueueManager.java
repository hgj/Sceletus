package hu.hgj.sceletus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueueManager {

	private static final Map<String, SimpleQueue> queues = new ConcurrentHashMap<>();
	private static final Map<String, TopicQueue> topicQueues = new ConcurrentHashMap<>();

	public static void waitForEmptyQueue(Queue queue) throws InterruptedException {
		waitForEmptyQueue(queue, 100);
	}

	public static void waitForEmptyQueue(Queue queue, int sleepTimeMillis) throws InterruptedException {
		while (queue.size() > 0) {
			Thread.sleep(sleepTimeMillis);
		}
	}

	public static boolean registerQueue(String name, SimpleQueue simpleQueue) {
		return queues.putIfAbsent(name, simpleQueue) == null;
	}

	public static boolean registerAndStartQueue(String name, SimpleQueue simpleQueue) {
		if (registerQueue(name, simpleQueue)) {
			if (simpleQueue.start()) {
				return true;
			} else {
				removeQueue(name);
				return false;
			}
		} else {
			return false;
		}
	}

	public static SimpleQueue removeQueue(String name) {
		return queues.remove(name);
	}

	public static boolean removeQueue(String name, SimpleQueue simpleQueue) {
		return queues.remove(name, simpleQueue);
	}

	public static SimpleQueue getQueue(String name) {
		return queues.get(name);
	}

	public static boolean registerTopicQueue(String name, TopicQueue topicQueue) {
		return topicQueues.putIfAbsent(name, topicQueue) == null;
	}

	public static boolean registerAndStartTopicQueue(String name, TopicQueue queue) {
		if (registerTopicQueue(name, queue)) {
			if (queue.start()) {
				return true;
			} else {
				removeTopicQueue(name);
				return false;
			}
		} else {
			return false;
		}
	}

	public static TopicQueue removeTopicQueue(String name) {
		return topicQueues.remove(name);
	}

	public static boolean removeTopicQueue(String name, TopicQueue queue) {
		return topicQueues.remove(name, queue);
	}

	public static TopicQueue getTopicQueue(String name) {
		return topicQueues.get(name);
	}

}
