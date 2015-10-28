package hu.hgj.sceletus.queue.simple;

import java.util.Map;

public class SimpleStringMapTopicQueue<E> extends SimpleTopicQueue<Map<String, ?>, E> {

	public SimpleStringMapTopicQueue(String name) {
		super(name);
	}

	public SimpleStringMapTopicQueue(String name, int workers, boolean allowDuplicates, boolean keepElements) {
		super(name, workers, allowDuplicates, keepElements);
	}

}
