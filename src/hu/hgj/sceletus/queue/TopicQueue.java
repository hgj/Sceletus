package hu.hgj.sceletus.queue;

import hu.hgj.sceletus.module.MultiThreadedModule;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TopicQueue<E> extends MultiThreadedModule implements Queue {

	private final BlockingQueue<WithTopic<E>> queue = new LinkedBlockingQueue<>();
	private final int workers;
	private final boolean allowDuplicates;
	private final Map<QueueListener<WithTopic<E>>, Set<Pattern>> listeners = new ConcurrentHashMap<>();
	private final Map<String, Set<QueueListener<WithTopic<E>>>> filtersCache = new ConcurrentHashMap<>();

	public TopicQueue(String name, int workers, boolean allowDuplicates) {
		super(name);
		this.workers = workers;
		this.allowDuplicates = allowDuplicates;
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
		if (!allowDuplicates && queue.contains(elementWithTopic)) {
			return false;
		}
		return queue.add(elementWithTopic);
	}

	public Map<QueueListener<WithTopic<E>>, Set<Pattern>> getListeners() {
		return listeners;
	}

	public void unSubscribe(QueueListener<WithTopic<E>> listener) {
		subscribe(listener, null);
	}

	public void subscribe(QueueListener<WithTopic<E>> listener, Set<String> filters) {
		if (filters == null) {
			listeners.remove(listener);
		} else {
			Set<Pattern> patterns = filters.stream().map(Pattern::compile).collect(Collectors.toCollection(LinkedHashSet::new));
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
				if (waitingForListeners()) {
					continue;
				}
				WithTopic<E> elementWithTopic = queue.poll(1, TimeUnit.SECONDS);
				if (elementWithTopic != null) {
					if (!workWithElement(elementWithTopic)) {
						logger.warn("Failed to work with element in queue, putting it back. Element is: {}", elementWithTopic.toString());
						this.add(elementWithTopic);
					}
				}
			} catch (InterruptedException exception) {
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

	protected boolean workWithElement(WithTopic<E> elementWithTopic) {
		Set<QueueListener<WithTopic<E>>> affectedListeners = filtersCache.get(elementWithTopic.topic);
		if (affectedListeners == null) {
			affectedListeners = new LinkedHashSet<>();
			listenersLoop:
			for (Map.Entry<QueueListener<WithTopic<E>>, Set<Pattern>> entry : listeners.entrySet()) {
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
		boolean success = true;
		for (QueueListener<WithTopic<E>> listener : affectedListeners) {
			success &= listener.handleElement(elementWithTopic);
		}
		if (affectedListeners.size() == 1) return success;
		return affectedListeners.size() > 0;
	}

}
