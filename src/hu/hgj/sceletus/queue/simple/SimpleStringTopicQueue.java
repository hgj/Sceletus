package hu.hgj.sceletus.queue.simple;

public class SimpleStringTopicQueue<E> extends SimpleTopicQueue<String, E> {

	public SimpleStringTopicQueue(String name) {
		super(name);
	}

	public SimpleStringTopicQueue(String name, int workers, boolean allowDuplicates, boolean keepElements) {
		super(name, workers, allowDuplicates, keepElements);
	}

}
