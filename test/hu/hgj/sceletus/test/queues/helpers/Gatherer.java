package hu.hgj.sceletus.test.queues.helpers;

import hu.hgj.sceletus.queue.TopicQueueListener;
import hu.hgj.sceletus.queue.WithTopic;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Gatherer<E> implements TopicQueueListener<E> {

	private final List<WithTopic<E>> storage;
	private AtomicInteger counter = new AtomicInteger(0);

	public Gatherer(List<WithTopic<E>> storage) {
		this.storage = storage;
	}

	@Override
	public boolean handleElement(WithTopic<E> elementWithTopic) {
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
