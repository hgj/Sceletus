package hu.hgj.sceletus.queue;

import java.util.Objects;

public class WithTopic<T> {

	public final String topic;
	public final T element;

	public WithTopic(String topic, T value) {
		this.topic = topic;
		this.element = value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || getClass() != other.getClass()) return false;
		WithTopic<?> otherWithTopic = (WithTopic<?>) other;
		return topic.equals(otherWithTopic.topic) && element.equals(otherWithTopic.element);
	}

	@Override
	public int hashCode() {
		return Objects.hash(topic, element);
	}

	@Override
	public String toString() {
		return "WithTopic{topic='" + topic + "', element='" + element + "'}";
	}

}
