package hu.hgj.sceletus.queue;

import hu.hgj.sceletus.module.MultiThreadedModule;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SimpleQueue<E> extends MultiThreadedModule implements Queue {

	private final BlockingQueue<E> queue = new LinkedBlockingQueue<>();
	private final int workers;
	private final boolean allowDuplicates;
	protected final Set<QueueListener<E>> listeners = Collections.synchronizedSet(new LinkedHashSet<>());

	public SimpleQueue(String name, int workers, boolean allowDuplicates) {
		super(name);
		this.workers = workers;
		this.allowDuplicates = allowDuplicates;
	}

	public BlockingQueue<E> getQueue() {
		return queue;
	}

	public int size() {
		return queue.size();
	}

	public boolean add(E element) {
		if (!allowDuplicates && queue.contains(element)) {
			return false;
		}
		return queue.add(element);
	}

	public Set<QueueListener<E>> getListeners() {
		return listeners;
	}

	public boolean unSubscribe(QueueListener<E> listener) {
		return listeners.remove(listener);
	}

	public boolean subscribe(QueueListener<E> listener) {
		return listeners.add(listener);
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
				E element = queue.poll(1, TimeUnit.SECONDS);
				if (element != null) {
					if (!workWithElement(element)) {
						logger.warn("Failed to work with element in queue, putting it back. Element is: {}", element.toString());
						this.add(element);
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

	protected boolean workWithElement(E element) {
		boolean success = true;
		for (QueueListener<E> listener : listeners) {
			success &= listener.handleElement(element);
		}
		if (listeners.size() == 1) return success;
		return listeners.size() > 0;
	}

}
