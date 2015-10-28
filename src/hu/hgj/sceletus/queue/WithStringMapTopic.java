package hu.hgj.sceletus.queue;

import java.util.Map;

public class WithStringMapTopic<E> extends WithTopic<Map<String, ?>, E> {

	public WithStringMapTopic(Map<String, ?> topic, E value) {
		super(topic, value);
	}

}
