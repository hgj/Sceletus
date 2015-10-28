package hu.hgj.sceletus.queue;

import java.util.function.Predicate;

public abstract class StringTopicQueue<E> implements TopicQueue<String, E> {

	boolean add(WithStringTopic<E> elementWithStringTopic) {
		return add((WithTopic<String, E>) elementWithStringTopic);
	}

	void unSubscribe(StringTopicQueueListener<E> listener) {
		unSubscribe((TopicQueueListener<String, E>) listener);
	}

	void subscribe(StringTopicQueueListener<E> listener) {
		subscribe((TopicQueueListener<String, E>) listener);
	}

	void subscribe(StringTopicQueueListener<E> listener, Predicate<String> filter) {
		subscribe((TopicQueueListener<String, E>) listener, filter);
	}

}
