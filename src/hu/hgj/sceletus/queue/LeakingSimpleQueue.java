package hu.hgj.sceletus.queue;

public class LeakingSimpleQueue<E> extends SimpleQueue<E> {

	public LeakingSimpleQueue(String name, int workers, boolean allowDuplicates) {
		super(name, workers, allowDuplicates);
	}

	@Override
	protected boolean waitingForListeners() throws InterruptedException {
		return false;
	}

	@Override
	protected boolean workWithElement(E element) {
		super.workWithElement(element);
		return true;
	}

}
