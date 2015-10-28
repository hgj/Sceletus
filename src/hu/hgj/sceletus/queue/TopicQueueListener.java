package hu.hgj.sceletus.queue;

public interface TopicQueueListener<T, E> {

	boolean handleElement(WithTopic<T, E> elementWithTopic);

}
