package hu.hgj.sceletus.queue;

public interface TopicQueueListener<E> {

	boolean handleElement(WithTopic<E> elementWithTopic);

}
