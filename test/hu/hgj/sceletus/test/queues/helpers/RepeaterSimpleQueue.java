package hu.hgj.sceletus.test.queues.helpers;

import hu.hgj.sceletus.SimpleQueue;

public class RepeaterSimpleQueue<E> extends SimpleQueue<E> {

	private final E element;

	public RepeaterSimpleQueue(int workers, E element) {
		super(workers);
		this.element = element;
	}

	@Override
	protected void main(int threadID) {
		while (running) {
			workWithElement(element);
		}
	}

}
