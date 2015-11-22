package hu.hgj.sceletus.queues.helpers;

import hu.hgj.sceletus.queue.TopicQueueListener;
import hu.hgj.sceletus.queue.WithTopic;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Gatherer<T, E> implements TopicQueueListener<T, E> {

	private final List<WithTopic<T, E>> storage;
	private AtomicInteger counter = new AtomicInteger(0);

	public Gatherer(List<WithTopic<T, E>> storage) {
		this.storage = storage;
	}

	@Override
	public boolean handleElement(WithTopic<T, E> elementWithTopic) {
		if (storage.add(elementWithTopic)) {
			counter.incrementAndGet();
			return true;
		} else {
			return false;
		}
	}

	public int getCounter() {
		return counter.get();
	}

}
