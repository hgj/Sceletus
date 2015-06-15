package hu.hgj.sceletus;

public interface TopicQueueListener<E> {

	void handleElement(String topic, E element);

}
