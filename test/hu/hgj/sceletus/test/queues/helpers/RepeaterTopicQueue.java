package hu.hgj.sceletus.test.queues.helpers;

import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.WithTopic;

public class RepeaterTopicQueue<E> extends TopicQueue<E> {

	private final WithTopic<E> elementWithTopic;

	public RepeaterTopicQueue(String name, int workers, WithTopic<E> elementWithTopic) {
		super(name, workers, true);
		this.elementWithTopic = elementWithTopic;
	}

	@Override
	protected void main(int threadID) {
		while (running) {
			workWithElement(elementWithTopic);
		}
	}

}
