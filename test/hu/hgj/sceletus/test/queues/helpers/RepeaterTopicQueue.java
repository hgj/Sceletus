package hu.hgj.sceletus.test.queues.helpers;

import hu.hgj.sceletus.TopicQueue;
import hu.hgj.sceletus.WithTopic;

public class RepeaterTopicQueue<E> extends TopicQueue<E> {

	private final WithTopic<E> elementWithTopic;

	public RepeaterTopicQueue(int workers, WithTopic<E> elementWithTopic) {
		super(workers);
		this.elementWithTopic = elementWithTopic;
	}

	@Override
	protected void main(int threadID) {
		while (running) {
			workWithElement(elementWithTopic);
		}
	}

}
