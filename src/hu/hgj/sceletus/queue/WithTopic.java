package hu.hgj.sceletus.queue;

import java.util.Objects;

public class WithTopic<T, E> {

	public final T topic;
	public final E element;

	public WithTopic(T topic, E value) {
		this.topic = topic;
		this.element = value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || getClass() != other.getClass()) return false;
		WithTopic<?, ?> otherWithTopic = (WithTopic<?, ?>) other;
		return Objects.equals(topic, otherWithTopic.topic) &&
				Objects.equals(element, otherWithTopic.element);
	}

	@Override
	public int hashCode() {
		return Objects.hash(topic, element);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{topic='" + topic.toString()
				+ "', element='" + element.toString()
				+ "'}";
	}

}
