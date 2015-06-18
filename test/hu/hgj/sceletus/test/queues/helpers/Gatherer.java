package hu.hgj.sceletus.test.queues.helpers;

import hu.hgj.sceletus.queue.QueueListener;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Gatherer<E> implements QueueListener<E> {

	private final List<E> storage;
	private AtomicInteger counter = new AtomicInteger(0);

	public Gatherer(List<E> storage) {
		this.storage = storage;
	}

	@Override
	public void handleElement(E element) {
		storage.add(element);
		counter.incrementAndGet();
	}

	public int getCounter() {
		return counter.get();
	}

}
