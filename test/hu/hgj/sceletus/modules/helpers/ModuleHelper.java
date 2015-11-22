package hu.hgj.sceletus.modules.helpers;

import hu.hgj.sceletus.module.Module;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ModuleHelper {

	public static void lifeCycleModule(Module module) {
		assertEquals("Module state should be UNKNOWN", Module.State.UNKNOWN, module.getState());
		assertEquals("Module should successfully reset()", true, module.reset());
		assertEquals("Module state should be RESET", Module.State.RESET, module.getState());
		assertEquals("Module should successfully start()", true, module.start());
		assertEquals("Module state should be STARTED", Module.State.STARTED, module.getState());
		assertEquals("Module should successfully stop()", true, module.stop());
		assertEquals("Module state should be STOPPED", Module.State.STOPPED, module.getState());
		assertEquals("Module should successfully reset()", true, module.reset());
		assertEquals("Module state should be RESET", Module.State.RESET, module.getState());
	}

	public static void testNThreadedModule(Module module, Set<Integer> outputSet, int threads) {
		ModuleHelper.lifeCycleModule(module);
		for (int i = 0; i < threads; i++) {
			assertEquals(String.format("outputSet should contain Integer(%d)", i), true, outputSet.contains(Integer.valueOf(i)));
		}
	}

}
