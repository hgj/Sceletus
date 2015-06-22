package hu.hgj.sceletus.queue;

public class WithTopic<T> {

	public final String topic;
	public final T value;

	public WithTopic(String topic, T value) {
		this.topic = topic;
		this.value = value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || getClass() != other.getClass()) return false;
		WithTopic<?> otherWithTopic = (WithTopic<?>) other;
		return topic.equals(otherWithTopic.topic) && value.equals(otherWithTopic.value);
	}

	@Override
	public String toString() {
		return "WithTopic{topic='" + topic + "', value=" + value + "}";
	}

}
