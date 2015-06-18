package hu.hgj.sceletus.queue;

public class LeakingTopicQueue<E> extends TopicQueue<E> {

	public LeakingTopicQueue(String name, int workers, boolean allowDuplicates) {
		super(name, workers, allowDuplicates);
	}

	@Override
	protected boolean waitingForListeners() throws InterruptedException {
		return false;
	}

	@Override
	protected boolean workWithElement(WithTopic<E> element) {
		super.workWithElement(element);
		return true;
	}

}
