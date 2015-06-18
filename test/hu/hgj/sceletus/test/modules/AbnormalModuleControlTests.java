package hu.hgj.sceletus.test.modules;

import hu.hgj.sceletus.module.AbstractModuleAdapter;
import hu.hgj.sceletus.module.Module;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbnormalModuleControlTests {

	private Module createTestModule() {
		return new AbstractModuleAdapter("testModule") {
			// Nothing here;
		};
	}

	@Test
	public void doubleResetTest() {
		Module module = createTestModule();
		assertEquals("Module state should be UNKNOWN", Module.State.UNKNOWN, module.getState());
		assertEquals("Module should successfully reset()", true, module.reset());
		assertEquals("Module state should be RESET", Module.State.RESET, module.getState());
		assertEquals("Module should successfully reset() again", true, module.reset());
		assertEquals("Module state should still be RESET", Module.State.RESET, module.getState());
	}

	@Test
	public void noResetStartTest() {
		Module module = createTestModule();
		assertEquals("Module state should be UNKNOWN", Module.State.UNKNOWN, module.getState());
		assertEquals("Module should successfully start()", true, module.start());
		assertEquals("Module state should be STARTED", Module.State.STARTED, module.getState());
	}

	@Test
	public void doubleStartTest() {
		Module module = createTestModule();
		assertEquals("Module state should be UNKNOWN", Module.State.UNKNOWN, module.getState());
		assertEquals("Module should successfully start()", true, module.start());
		assertEquals("Module state should be STARTED", Module.State.STARTED, module.getState());
		assertEquals("Module should successfully start() again", true, module.start());
		assertEquals("Module state should still be STARTED", Module.State.STARTED, module.getState());
	}

	@Test
	public void doubleStopTest() {
		Module module = createTestModule();
		assertEquals("Module state should be UNKNOWN", Module.State.UNKNOWN, module.getState());
		assertEquals("Module should successfully start()", true, module.start());
		assertEquals("Module state should be STARTED", Module.State.STARTED, module.getState());
		assertEquals("Module should successfully stop()", true, module.stop());
		assertEquals("Module state should be STOPPED", Module.State.STOPPED, module.getState());
		assertEquals("Module should successfully stop() again", true, module.stop());
		assertEquals("Module state should still be STOPPED", Module.State.STOPPED, module.getState());
	}

	@Test
	public void resetStartedTest() {
		Module module = createTestModule();
		assertEquals("Module state should be UNKNOWN", Module.State.UNKNOWN, module.getState());
		assertEquals("Module should successfully reset()", true, module.reset());
		assertEquals("Module state should be RESET", Module.State.RESET, module.getState());
		assertEquals("Module should successfully start()", true, module.start());
		assertEquals("Module state should be STARTED", Module.State.STARTED, module.getState());
		assertEquals("Module should successfully reset()", true, module.reset());
		assertEquals("Module state should be RESET", Module.State.RESET, module.getState());
	}

}
