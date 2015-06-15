package hu.hgj.sceletus;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopicQueue<E> extends MultiThreadedModule implements Queue {

	private final BlockingQueue<WithTopic<E>> queue = new LinkedBlockingQueue<>();
	private final int workers;
	private final Map<TopicQueueListener<E>, Set<Pattern>> listeners = new ConcurrentHashMap<>();
	private final Map<String, Set<TopicQueueListener<E>>> filtersCache = new ConcurrentHashMap<>();

	public TopicQueue(int workers) {
		this.workers = workers;
	}

	public BlockingQueue<WithTopic<E>> getQueue() {
		return queue;
	}

	public int size() {
		return queue.size();
	}

	public boolean add(String topic, E element) {
		return this.add(new WithTopic<>(topic, element));
	}

	public boolean add(WithTopic<E> elementWithTopic) {
		return queue.add(elementWithTopic);
	}

	public void unSubscribe(TopicQueueListener<E> listener) {
		subscribe(listener, null);
	}

	public void subscribe(TopicQueueListener<E> listener, Set<String> filters) {
		if (filters == null) {
			listeners.remove(listener);
		} else {
			Set<Pattern> patterns = new LinkedHashSet<>();
			for (String filter : filters) {
				patterns.add(Pattern.compile(filter));
			}
			listeners.put(listener, patterns);
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
				WithTopic<E> elementWithTopic = queue.poll(1, TimeUnit.SECONDS);
				if (elementWithTopic != null) {
					workWithElement(elementWithTopic);
				}
			} catch (InterruptedException exception) {
				// Ignore interrupt
			}
		}
	}

	protected void workWithElement(WithTopic<E> elementWithTopic) {
		Set<TopicQueueListener<E>> affectedListeners = filtersCache.get(elementWithTopic.topic);
		if (affectedListeners == null) {
			affectedListeners = new LinkedHashSet<>();
			listenersLoop:
			for (Map.Entry<TopicQueueListener<E>, Set<Pattern>> entry : listeners.entrySet()) {
				Set<Pattern> patterns = entry.getValue();
				for (Pattern pattern : patterns) {
					Matcher matcher = pattern.matcher(elementWithTopic.topic);
					if (matcher.matches()) {
						affectedListeners.add(entry.getKey());
						continue listenersLoop;
					}
				}
			}
			filtersCache.put(elementWithTopic.topic, affectedListeners);
		}
		for (TopicQueueListener<E> listener : affectedListeners) {
			listener.handleElement(elementWithTopic.topic, elementWithTopic.value);
		}
	}

}
