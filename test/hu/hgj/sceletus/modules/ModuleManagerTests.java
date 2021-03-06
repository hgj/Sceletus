package hu.hgj.sceletus.modules;

import hu.hgj.sceletus.module.Module;
import hu.hgj.sceletus.module.ModuleManager;
import hu.hgj.sceletus.queue.simple.SimpleTopicQueue;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.modules.implementations.SimpleSingleThreadedModule;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModuleManagerTests {

	@Test
	public void registerModule() {
		Module module = new SimpleSingleThreadedModule("testModule", new HashSet<>());
		assertTrue("ModuleManager should return true", ModuleManager.moduleRegistry.register(module));
		Module retrievedModule = ModuleManager.moduleRegistry.get(module.getName());
		assertEquals("Retrieved SimpleQueue should equal to registered SimpleQueue", module, retrievedModule);
	}

	@Test
	public void registerTopicQueue() {
		TopicQueue<Object, Integer> integerTopicQueue = new SimpleTopicQueue<>("testIntegerTopicQueue", 1, false, false);
		assertTrue("ModuleManager should return true", ModuleManager.queueRegistry.register(integerTopicQueue));
		TopicQueue<Object, Integer> retrievedQueue = (TopicQueue<Object, Integer>) ModuleManager.queueRegistry.get(integerTopicQueue.getName());
		assertEquals("Retrieved SimpleQueue should equal to registered SimpleQueue", integerTopicQueue, retrievedQueue);
	}

}
