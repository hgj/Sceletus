package hu.hgj.sceletus.queue;

public abstract class TopicQueueListener<E> implements QueueListener<WithTopic<E>> {

	public void handleElement(WithTopic<E> elementWithTopic) {
		handleElement(elementWithTopic.topic, elementWithTopic.value);
	}

	abstract void handleElement(String topic, E element);

}
