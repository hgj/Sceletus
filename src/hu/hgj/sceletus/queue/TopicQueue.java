package hu.hgj.sceletus.queue;

import hu.hgj.sceletus.module.Module;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

public interface TopicQueue<E> extends Module {

	BlockingQueue<WithTopic<E>> getQueue();

	int size();

	boolean add(String topic, E element);

	boolean add(WithTopic<E> elementWithTopic);

	Map<TopicQueueListener<E>, Set<Pattern>> getListeners();

	void unSubscribe(TopicQueueListener<E> listener);

	void subscribe(TopicQueueListener<E> listener, Set<String> filters);

}
