package hu.hgj.sceletus.queue;

public interface QueueListener<E> {

	void handleElement(E element);

}
