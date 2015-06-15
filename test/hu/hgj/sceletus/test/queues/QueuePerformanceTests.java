package hu.hgj.sceletus.test.queues;

import hu.hgj.sceletus.SimpleQueue;
import hu.hgj.sceletus.TopicQueue;
import hu.hgj.sceletus.WithTopic;
import hu.hgj.sceletus.test.queues.helpers.BlackHoleList;
import hu.hgj.sceletus.test.queues.helpers.Gatherer;
import hu.hgj.sceletus.test.queues.helpers.RepeaterSimpleQueue;
import hu.hgj.sceletus.test.queues.helpers.RepeaterTopicQueue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;

public class QueuePerformanceTests {

	static final Logger logger = LoggerFactory.getLogger(QueuePerformanceTests.class);

	static final int sleepTimeMillis = 1000;

	public void integerQueueTest(int workers) {
		SimpleQueue<Integer> integerSimpleQueue = new RepeaterSimpleQueue<>(workers, 1);
		List<Integer> integerStorage = new BlackHoleList<>();
		Gatherer<Integer> integerGatherer = new Gatherer<>(integerStorage);
		integerSimpleQueue.subscribe(integerGatherer);
		integerSimpleQueue.start();
		// Sleep for some time
		try {
			Thread.sleep(sleepTimeMillis);
		} catch (InterruptedException exception) {
			exception.printStackTrace();
		}
		long startTime = System.nanoTime();
		int startAmount = integerGatherer.getCounter();
		// Sleep for some time
		try {
			Thread.sleep(sleepTimeMillis);
		} catch (InterruptedException exception) {
			exception.printStackTrace();
		}
		int gatheredElements = integerGatherer.getCounter() - startAmount;
		long elapsedTime = System.nanoTime() - startTime;
		long elapsedTimeMillis = elapsedTime / 1_000_000;
		System.err.println(String.format("SimpleQueue collected %,d elements in %,d milliseconds with %d workers.", gatheredElements, elapsedTimeMillis, workers));
		System.err.println(String.format("SimpleQueue collected on average %,.2f elements / second.", gatheredElements * (1_000f / (float) elapsedTimeMillis)));
	}

	@Test
	public void oneWorkerIntegerQueueTest() {
		integerQueueTest(1);
	}

	@Test
	public void twoWorkerIntegerQueueTest() {
		integerQueueTest(2);
	}

	@Test
	public void fourWorkerIntegerQueueTest() {
		integerQueueTest(4);
	}

	public void integerTopicQueueTest(int workers) {
		TopicQueue<Integer> integerTopicQueue = new RepeaterTopicQueue<>(workers, new WithTopic<>("integer", 1));
		List<Integer> integerStorage = new BlackHoleList<>();
		Gatherer<Integer> integerGatherer = new Gatherer<>(integerStorage);
		integerTopicQueue.subscribe(integerGatherer, new LinkedHashSet<String>() {{
			add("integer");
		}});
		integerTopicQueue.start();
		// Sleep for some time
		try {
			Thread.sleep(sleepTimeMillis);
		} catch (InterruptedException exception) {
			exception.printStackTrace();
		}
		long startTime = System.nanoTime();
		int startAmount = integerGatherer.getCounter();
		// Sleep for some time
		try {
			Thread.sleep(sleepTimeMillis);
		} catch (InterruptedException exception) {
			exception.printStackTrace();
		}
		int gatheredElements = integerGatherer.getCounter() - startAmount;
		long elapsedTime = System.nanoTime() - startTime;
		long elapsedTimeMillis = elapsedTime / 1_000_000;
		System.err.println(String.format("TopicQueue collected %,d elements in %,d milliseconds with %d workers.", gatheredElements, elapsedTimeMillis, workers));
		System.err.println(String.format("TopicQueue collected on average %,.2f elements / second.", gatheredElements * (1_000f / (float) elapsedTimeMillis)));
	}

	@Test
	public void oneWorkerIntegerTopicQueueTest() {
		integerTopicQueueTest(1);
	}

	@Test
	public void twoWorkerIntegerTopicQueueTest() {
		integerTopicQueueTest(2);
	}

	@Test
	public void fourWorkerIntegerTopicQueueTest() {
		integerTopicQueueTest(4);
	}

}
