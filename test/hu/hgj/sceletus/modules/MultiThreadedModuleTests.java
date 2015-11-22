package hu.hgj.sceletus.modules;

import hu.hgj.sceletus.module.Module;
import hu.hgj.sceletus.module.MultiThreadedModule;
import hu.hgj.sceletus.modules.helpers.ModuleHelper;
import hu.hgj.sceletus.modules.implementations.LongRunningMultiThreadedModule;
import hu.hgj.sceletus.modules.implementations.SimpleMultiThreadedModule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.junit.Assert.assertEquals;

public class MultiThreadedModuleTests {

	@Test
	public void testConfiguration() throws IOException {
		MultiThreadedModule module = new SimpleMultiThreadedModule("testModule", null, 0);
		String threadJoinTimeoutFieldName =  "threadJoinTimeout";
		String threadRestartSleepFieldName =  "threadRestartSleep";
		int timeout = 1234;
		Map<String, Object> configuration = new HashMap<String, Object>() {{
			put("sceletus", new HashMap<String, Object>() {{
				put(threadJoinTimeoutFieldName, timeout);
				put(threadRestartSleepFieldName, timeout);
			}});
		}};
		module.updateConfiguration(configuration);
		assertEquals("MultiThreadedModule's threadJoinTimeout field should be updated to " + timeout, timeout, module.getThreadJoinTimeoutMilli());
		assertEquals("MultiThreadedModule's threadRestartSleep field should be updated to " + timeout, timeout, module.getThreadRestartSleepMilli());
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
