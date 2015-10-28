package hu.hgj.sceletus.queue;

import hu.hgj.sceletus.module.Module;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Predicate;

public interface TopicQueue<T, E> extends Module {

	BlockingQueue<WithTopic<T, E>> getQueue();

	int size();

	boolean add(T topic, E element);

	boolean add(WithTopic<T, E> elementWithTopic);

	Map<TopicQueueListener<T, E>, Predicate<T>> getListeners();

	void unSubscribe(TopicQueueListener<T, E> listener);

	void subscribe(TopicQueueListener<T, E> listener);

	void subscribe(TopicQueueListener<T, E> listener, Predicate<T> filter);

}
