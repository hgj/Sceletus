package hu.hgj.sceletus.test.modules;

import hu.hgj.sceletus.MultiThreadedModule;
import hu.hgj.sceletus.test.modules.helpers.ModuleHelper;
import hu.hgj.sceletus.test.modules.implementations.LongRunningMultiThreadedModule;
import hu.hgj.sceletus.test.modules.implementations.SimpleMultiThreadedModule;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class MultiThreadedModuleTests {

	public void testSimpleModuleWithNThreads(int threads) {
		Set<Integer> outputSet = new CopyOnWriteArraySet<>();
		MultiThreadedModule module = new SimpleMultiThreadedModule(outputSet, threads);
		ModuleHelper.testNThreadedModule(module, outputSet, threads);
	}

	public void testLongRunningModuleWithNThreads(int threads) {
		Set<Integer> outputSet = new CopyOnWriteArraySet<>();
		MultiThreadedModule module = new LongRunningMultiThreadedModule(outputSet, threads);
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
