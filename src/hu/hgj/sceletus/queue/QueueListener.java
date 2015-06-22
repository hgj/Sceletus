package hu.hgj.sceletus.queue;

public interface QueueListener<E> {

	boolean handleElement(E element);

}
