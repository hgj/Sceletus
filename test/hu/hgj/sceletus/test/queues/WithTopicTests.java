package hu.hgj.sceletus.test.queues;

import hu.hgj.sceletus.queue.WithStringMapTopic;
import hu.hgj.sceletus.queue.WithStringTopic;
import hu.hgj.sceletus.queue.WithTopic;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class WithTopicTests {

	@Test
	public void testEquals() {
		// WithTopic class
		// WithStringTopic class
		// WithStringMapTopic class
	}

	@Test
	public void testHashCode() {
		// WithTopic class
		// WithStringTopic class
		// WithStringMapTopic class
	}

	@Test
	public void testToString() {
		// WithTopic class
		WithTopic<String, String> withTopic = new WithTopic<>(
				"topic", "element"
		);
		assertEquals(
				"Output of toString() should be as expected.",
				"WithTopic{topic='topic', element='element'}",
				withTopic.toString()
		);
		// WithStringTopic class
		WithStringTopic<String> withStringTopic = new WithStringTopic<>(
				"topic", "element"
		);
		assertEquals(
				"Output of toString() should be as expected.",
				"WithStringTopic{topic='topic', element='element'}",
				withStringTopic.toString()
		);
		// WithStringMapTopic class
		Map<String, String> map = new LinkedHashMap<String, String>() {{
			put("key", "value");
			put("anotherKey", "anotherValue");
		}};
		WithStringMapTopic<String> withStringMapTopic = new WithStringMapTopic<>(
				map, "element"
		);
		assertEquals(
				"Output of toString() should be as expected.",
				"WithStringMapTopic{topic='{key=value, anotherKey=anotherValue}', element='element'}",
				withStringMapTopic.toString()
		);
	}

}
