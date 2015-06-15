package hu.hgj.sceletus;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SimpleQueue<E> extends MultiThreadedModule implements Queue {

	private final BlockingQueue<E> queue = new LinkedBlockingQueue<>();
	private final int workers;
	protected final Set<QueueListener<E>> listeners = Collections.synchronizedSet(new LinkedHashSet<>());

	public SimpleQueue(int workers) {
		this.workers = workers;
	}

	public BlockingQueue<E> getQueue() {
		return queue;
	}

	public int size() {
		return queue.size();
	}

	public boolean add(E element) {
		return queue.add(element);
	}

	public void unSubscribe(QueueListener<E> listener) {
		listeners.remove(listener);
	}

	public void subscribe(QueueListener<E> listener) {
		listeners.add(listener);
	}

	@Override
	protected int getNumberOfThreads() {
		return workers;
	}

	@Override
	protected void main(int threadID) {
		while (running) {
			try {
				E element = queue.poll(1, TimeUnit.SECONDS);
				if (element != null) {
					workWithElement(element);
				}
			} catch (InterruptedException exception) {
				// Ignore interrupt
			}
		}
	}

	protected void workWithElement(E element) {
		for (QueueListener<E> listener : listeners) {
			listener.handleElement(element);
		}
	}
}
