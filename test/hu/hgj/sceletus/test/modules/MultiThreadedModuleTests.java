package hu.hgj.sceletus.test.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.hgj.sceletus.module.Module;
import hu.hgj.sceletus.module.MultiThreadedModule;
import hu.hgj.sceletus.test.modules.helpers.ModuleHelper;
import hu.hgj.sceletus.test.modules.implementations.LongRunningMultiThreadedModule;
import hu.hgj.sceletus.test.modules.implementations.SimpleMultiThreadedModule;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.junit.Assert.assertEquals;

public class MultiThreadedModuleTests {

	@Test
	public void testConfiguration() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		MultiThreadedModule module = new SimpleMultiThreadedModule("testModule", null, 0);
		String fieldName =  "threadJoinTimeout";
		int timeout = 5000;
		module.updateConfiguration(objectMapper.readValue("{\"" + fieldName + "\":" + timeout + "}", Map.class));
		assertEquals("MultiThreadedModule's threadJoinTimeout field should be updated to " + timeout, timeout, module.threadJoinTimeout);
	}

	@Test
	public void testZeroThreadedModule() {
		Module module = new SimpleMultiThreadedModule("testModule", null, 0);
		ModuleHelper.lifeCycleModule(module);
	}

	public void testSimpleModuleWithNThreads(int threads) {
		Set<Integer> outputSet = new CopyOnWriteArraySet<>();
		MultiThreadedModule module = new SimpleMultiThreadedModule("testModule", outputSet, threads);
		ModuleHelper.testNThreadedModule(module, outputSet, threads);
	}

	public void testLongRunningModuleWithNThreads(int threads) {
		Set<Integer> outputSet = new CopyOnWriteArraySet<>();
		MultiThreadedModule module = new LongRunningMultiThreadedModule("testModule", outputSet, threads);
		ModuleHelper.testNThreadedModule(module, outputSet, threads);
	}

	@Test
	public void simpleModuleOneThreadTest() {
		testSimpleModuleWithNThreads(1);
	}

	@Test
	public void simpleModuleTwoThreadsTest() {
		testSimpleModuleWithNThreads(2);
	}

	@Test
	public void simpleModuleFourThreadsTest() {
		testSimpleModuleWithNThreads(4);
	}

	@Test
	public void simpleModuleSixteenThreadsTest() {
		testSimpleModuleWithNThreads(16);
	}

	@Test
	public void longRunningModuleOneThreadTest() {
		testLongRunningModuleWithNThreads(1);
	}

	@Test
	public void longRunningModuleTwoThreadsTest() {
		testLongRunningModuleWithNThreads(2);
	}

	@Test
	public void longRunningModuleFourThreadsTest() {
		testLongRunningModuleWithNThreads(4);
	}

	@Test
	public void longRunningModuleSixteenThreadsTest() {
		testLongRunningModuleWithNThreads(16);
	}

}
