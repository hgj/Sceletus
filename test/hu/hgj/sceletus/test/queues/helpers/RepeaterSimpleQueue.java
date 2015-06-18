package hu.hgj.sceletus.test.queues.helpers;

import hu.hgj.sceletus.queue.SimpleQueue;

public class RepeaterSimpleQueue<E> extends SimpleQueue<E> {

	private final E element;

	public RepeaterSimpleQueue(String name, int workers, E element) {
		super(name, workers, true);
		this.element = element;
	}

	@Override
	protected void main(int threadID) {
		while (running) {
			workWithElement(element);
		}
	}

}
