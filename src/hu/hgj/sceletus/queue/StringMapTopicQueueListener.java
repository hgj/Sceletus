package hu.hgj.sceletus.queue;

import java.util.Map;

public abstract class StringMapTopicQueueListener<E> implements TopicQueueListener<Map<String, ?>, E> {

	public boolean handleElement(WithStringMapTopic<E> elementWithStringMapTopic) {
		return handleElement((WithTopic<Map<String, ?>, E>) elementWithStringMapTopic);
	}

}
