package hu.hgj.sceletus.queue;

import java.util.Map;
import java.util.function.Predicate;

public abstract class StringMapTopicQueue<E> implements TopicQueue<Map<String, ?>, E> {

	boolean add(WithStringMapTopic<E> elementWithStringMapTopic) {
		return add((WithTopic<Map<String, ?>, E>) elementWithStringMapTopic);
	}

	void unSubscribe(StringMapTopicQueueListener<E> listener) {
		unSubscribe((TopicQueueListener<Map<String, ?>, E>) listener);
	}

	void subscribe(StringMapTopicQueueListener<E> listener) {
		subscribe((TopicQueueListener<Map<String, ?>, E>) listener);
	}

	void subscribe(StringMapTopicQueueListener<E> listener, Predicate<Map<String, ?>> filter) {
		subscribe((TopicQueueListener<Map<String, ?>, E>) listener, filter);
	}

}
