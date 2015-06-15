package hu.hgj.sceletus.test.modules;

import hu.hgj.sceletus.Module;
import hu.hgj.sceletus.test.modules.implementations.ForeverRunningSingleThreadedModule;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.junit.Assert.assertEquals;

public class ForeverRunningModuleTest {

	@Test
	public void foreverRunningModuleTest() {
		Set<Integer> outputSet = new CopyOnWriteArraySet<>();
		Module module = new ForeverRunningSingleThreadedModule(outputSet);
		assertEquals("Module state should be UNKNOWN", Module.State.UNKNOWN, module.getState());
		assertEquals("Module should successfully reset()", true, module.reset());
		assertEquals("Module state should be RESET", Module.State.RESET, module.getState());
		assertEquals("Module should successfully start()", true, module.start());
		assertEquals("Module state should be STARTED", Module.State.STARTED, module.getState());
		assertEquals("Module should not stop() successfully", false, module.stop());
		assertEquals("Module state should be UNKNOWN", Module.State.UNKNOWN, module.getState());
		assertEquals("Module should successfully reset()", true, module.reset());
		assertEquals("Module state should be RESET", Module.State.RESET, module.getState());
	}

}
