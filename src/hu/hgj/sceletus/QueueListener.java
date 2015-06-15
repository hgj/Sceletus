package hu.hgj.sceletus;

public interface QueueListener<E> {

	void handleElement(E element);

}
