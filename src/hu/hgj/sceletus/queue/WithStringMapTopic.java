package hu.hgj.sceletus.queue;

import java.util.Map;

public class WithStringMapTopic<E> extends WithTopic<Map<String, Object>, E> {

	public WithStringMapTopic(Map<String, Object> topic, E value) {
		super(topic, value);
	}

}
