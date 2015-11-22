package hu.hgj.sceletus.modules;

import hu.hgj.sceletus.module.Module;
import hu.hgj.sceletus.modules.helpers.ModuleHelper;
import hu.hgj.sceletus.modules.implementations.LongRunningSingleThreadedModule;
import hu.hgj.sceletus.modules.implementations.SimpleSingleThreadedModule;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class SingleThreadedModuleTests {

	@Test
	public void simpleModuleTest() {
		Set<Integer> outputSet = new CopyOnWriteArraySet<>();
		Module module = new SimpleSingleThreadedModule("testModule", outputSet);
		ModuleHelper.testNThreadedModule(module, outputSet, 1);
	}

	@Test
	public void longRunningModuleTest() {
		Set<Integer> outputSet = new CopyOnWriteArraySet<>();
		Module module = new LongRunningSingleThreadedModule("testModule", outputSet);
		ModuleHelper.testNThreadedModule(module, outputSet, 1);
	}

}
