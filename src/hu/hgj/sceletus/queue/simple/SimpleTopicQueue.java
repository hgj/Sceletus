package hu.hgj.sceletus.queue.simple;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.module.MultiThreadedModule;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.TopicQueueListener;
import hu.hgj.sceletus.queue.WithTopic;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class SimpleTopicQueue<T, E> extends MultiThreadedModule implements TopicQueue<T, E> {

	public static boolean catchAllFilter(Object topic) {
		return true;
	}

	protected final BlockingQueue<WithTopic<T, E>> queue = new LinkedBlockingQueue<>();
	protected int workers = 1;
	protected boolean allowDuplicates = false;
	protected boolean keepElements = true;
	protected final Map<TopicQueueListener<T, E>, Predicate<T>> listeners = new ConcurrentHashMap<>();
	protected final Map<T, Set<TopicQueueListener<T, E>>> filtersCache = new ConcurrentHashMap<>();

	public SimpleTopicQueue(String name) {
		super(name);
	}

	public SimpleTopicQueue(String name, int workers, boolean allowDuplicates, boolean keepElements) {
		super(name);
		this.workers = workers;
		this.allowDuplicates = allowDuplicates;
		this.keepElements = keepElements;
	}

	@Override
	public boolean updateConfiguration(Object configuration) {
		if (!super.updateConfiguration(configuration)) {
			return false;
		}
		try {
			workers = JsonPath.read(configuration, "$.workers");
		} catch (PathNotFoundException ignored) {
			// Ignore, stick to the default
		}
		try {
			allowDuplicates = JsonPath.read(configuration, "$.allowDuplicates");
		} catch (PathNotFoundException ignored) {
			// Ignore, stick to the default
		}
		try {
			keepElements = JsonPath.read(configuration, "$.keepElements");
		} catch (PathNotFoundException ignored) {
			// Ignore, stick to the default
		}
		logger.info("Updated configuration: workers={}, allowDuplicates={}, keepElements={}", workers, allowDuplicates, keepElements);
		return true;
	}

	public BlockingQueue<WithTopic<T, E>> getQueue() {
		return queue;
	}

	public int size() {
		return queue.size();
	}

	public boolean add(T topic, E element) {
		return add(new WithTopic<>(topic, element));
	}

	public boolean add(WithTopic<T, E> elementWithTopic) {
		if (!allowDuplicates && queue.contains(elementWithTopic)) {
			logger.trace("Not adding element with topic {} to queue '{}' as it is a duplicate.", elementWithTopic.toString(), getName());
			return false;
		}
		if (queue.offer(elementWithTopic)) {
			return true;
		} else {
			logger.warn("Failed to add element with topic {} to queue '{}'.", elementWithTopic.toString(), getName());
			return false;
		}
	}

	public Map<TopicQueueListener<T, E>, Predicate<T>> getListeners() {
		return listeners;
	}

	public void unSubscribe(TopicQueueListener<T, E> listener) {
		subscribe(listener, null);
	}

	public void subscribe(TopicQueueListener<T, E> listener) {
		subscribe(listener, SimpleTopicQueue::catchAllFilter);
	}

	public void subscribe(TopicQueueListener<T, E> listener, Predicate<T> filter) {
		if (filter == null) {
			listeners.remove(listener);
		} else {
			listeners.put(listener, filter);
		}
		filtersCache.clear();
	}

	@Override
	protected int getNumberOfThreads() {
		return workers;
	}

	@Override
	protected void main(int threadID) {
		while (running) {
			try {
				if (waitingForListeners()) {
					continue;
				}
				WithTopic<T, E> elementWithTopic = queue.poll(1, TimeUnit.SECONDS);
				if (elementWithTopic != null) {
					if (!workWithElement(elementWithTopic) && keepElements) {
						logger.warn("Failed to work with element in queue, putting it back. Element is: {}", elementWithTopic.toString());
						this.add(elementWithTopic);
					}
				}
			} catch (InterruptedException ignored) {
				// Ignore interrupt
			}
		}
	}

	protected boolean waitingForListeners() throws InterruptedException {
		if (listeners.size() == 0) {
			Thread.sleep(1000);
			return true;
		} else {
			return false;
		}
	}

	protected boolean workWithElement(WithTopic<T, E> elementWithTopic) {
		Set<TopicQueueListener<T, E>> affectedListeners = null;
		if (elementWithTopic.topic != null) {
			// TODO: Maybe allow null topics in cache?
			affectedListeners = filtersCache.get(elementWithTopic.topic);
		}
		if (affectedListeners == null) {
			affectedListeners = new LinkedHashSet<>();
			for (Map.Entry<TopicQueueListener<T, E>, Predicate<T>> entry : listeners.entrySet()) {
				if (entry.getValue().test(elementWithTopic.topic)) {
					affectedListeners.add(entry.getKey());
				}
			}
			if (elementWithTopic.topic != null) {
				filtersCache.put(elementWithTopic.topic, affectedListeners);
			}
		}
		boolean success = true;
		for (TopicQueueListener<T, E> listener : affectedListeners) {
			try {
				success &= listener.handleElement(elementWithTopic);
			} catch (Throwable throwable) {
				logger.warn("Exception caught while handling element.", throwable);
				success &= false;
			}
		}
		if (affectedListeners.size() == 1) return success;
		return affectedListeners.size() > 0;
	}

}
