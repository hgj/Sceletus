package hu.hgj.sceletus;

public class NamedTopicQueue<E> extends TopicQueue<E> implements NamedQueue {

	private final String name;

	public NamedTopicQueue(String name, int workers) {
		super(workers);
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	protected String nameThread(int threadID) {
		return this.getClass().getSimpleName() + "#" + this.hashCode() + "(" + getName() + ")" + "-" + threadID;
	}

}

