package hu.hgj.sceletus.queues.helpers;

import hu.hgj.sceletus.queue.simple.SimpleTopicQueue;
import hu.hgj.sceletus.queue.WithTopic;

public class RepeaterTopicQueue<T, E> extends SimpleTopicQueue<T, E> {

	private final WithTopic<T, E> elementWithTopic;

	public RepeaterTopicQueue(String name, int workers, WithTopic<T, E> elementWithTopic) {
		super(name, workers, true, false);
		this.elementWithTopic = elementWithTopic;
	}

	@Override
	protected void main(int threadID) {
		while (running) {
			workWithElement(elementWithTopic);
		}
	}

}
