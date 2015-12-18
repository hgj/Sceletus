package hu.hgj.sceletus.queue;

public abstract class StringTopicQueueListener<E> implements TopicQueueListener<String, E> {

	public boolean handleElement(WithStringTopic<E> elementWithStringTopic) {
		return handleElement((WithTopic<String, E>) elementWithStringTopic);
	}

}
