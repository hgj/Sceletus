package hu.hgj.sceletus;

public class WithTopic<T> {

	public final String topic;
	public final T value;

	public WithTopic(String topic, T value) {
		this.topic = topic;
		this.value = value;
	}

}
