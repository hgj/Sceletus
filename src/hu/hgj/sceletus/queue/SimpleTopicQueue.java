package hu.hgj.sceletus.queue;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
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

public class SimpleTopicQueue<E> extends MultiThreadedModule implements TopicQueue<E> {

	public static final Set<String> catchAllFilter;
	protected static final Set<Pattern> catchAllPattern;

	static {
		catchAllFilter = new LinkedHashSet<String>() {{
			add(".*");
		}};
		catchAllPattern = new LinkedHashSet<Pattern>() {{
			add(Pattern.compile(".*"));
		}};
	}

	protected final BlockingQueue<WithTopic<E>> queue = new LinkedBlockingQueue<>();
	protected int workers = 1;
	protected boolean allowDuplicates = false;
	protected boolean keepElements = true;
	protected final Map<TopicQueueListener<E>, Set<Pattern>> listeners = new ConcurrentHashMap<>();
	protected final Map<String, Set<TopicQueueListener<E>>> filtersCache = new ConcurrentHashMap<>();

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
		return true;
	}

	public BlockingQueue<WithTopic<E>> getQueue() {
		return queue;
	}

	public int size() {
		return queue.size();
	}

	public boolean add(String topic, E element) {
		return add(new WithTopic<>(topic, element));
	}

	public boolean add(WithTopic<E> elementWithTopic) {
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

	public Map<TopicQueueListener<E>, Set<Pattern>> getListeners() {
		return listeners;
	}

	public void unSubscribe(TopicQueueListener<E> listener) {
		subscribe(listener, null);
	}

	public void subscribe(TopicQueueListener<E> listener, Set<String> filters) {
		if (filters == null) {
			listeners.remove(listener);
		} else {
			Set<Pattern> patterns;
			if (filters == catchAllFilter) {
				patterns = catchAllPattern;
			} else {
				patterns = filters.stream().map(Pattern::compile).collect(Collectors.toCollection(LinkedHashSet::new));
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
				if (waitingForListeners()) {
					continue;
				}
				WithTopic<E> elementWithTopic = queue.poll(1, TimeUnit.SECONDS);
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

	protected boolean workWithElement(WithTopic<E> elementWithTopic) {
		Set<TopicQueueListener<E>> affectedListeners = filtersCache.get(elementWithTopic.topic);
		if (affectedListeners == null) {
			affectedListeners = new LinkedHashSet<>();
			listenersLoop:
			for (Map.Entry<TopicQueueListener<E>, Set<Pattern>> entry : listeners.entrySet()) {
				if (entry.getValue() == catchAllPattern) {
					affectedListeners.add(entry.getKey());
				} else {
					Set<Pattern> patterns = entry.getValue();
					for (Pattern pattern : patterns) {
						Matcher matcher = pattern.matcher(elementWithTopic.topic);
						if (matcher.matches()) {
							affectedListeners.add(entry.getKey());
							continue listenersLoop;
						}
					}
				}
			}
			filtersCache.put(elementWithTopic.topic, affectedListeners);
		}
		boolean success = true;
		for (TopicQueueListener<E> listener : affectedListeners) {
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
