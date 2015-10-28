package hu.hgj.sceletus.queue;

public class WithStringTopic<E> extends WithTopic<String, E> {

	public WithStringTopic(String topic, E value) {
		super(topic, value);
	}

}
