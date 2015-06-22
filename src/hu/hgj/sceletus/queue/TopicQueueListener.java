package hu.hgj.sceletus.queue;

public abstract class TopicQueueListener<E> implements QueueListener<WithTopic<E>> {

	public boolean handleElement(WithTopic<E> elementWithTopic) {
		return handleElement(elementWithTopic.topic, elementWithTopic.value);
	}

	abstract boolean handleElement(String topic, E element);

}
