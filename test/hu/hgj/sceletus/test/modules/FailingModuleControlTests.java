package hu.hgj.sceletus.test.modules;

import hu.hgj.sceletus.AbstractModuleAdapter;
import hu.hgj.sceletus.Module;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FailingModuleControlTests {

	@Test
	public void failingResetTest() {
		Module module = new AbstractModuleAdapter() {
			@Override
			protected boolean doReset() {
				return false;
			}
		};
		assertEquals("Module state should be UNKNOWN", Module.State.UNKNOWN, module.getState());
		assertEquals("Module should fail to reset()", false, module.reset());
		assertEquals("Module state should be UNKNOWN", Module.State.UNKNOWN, module.getState());
	}

	@Test
	public void failingStartTest() {
		Module module = new AbstractModuleAdapter() {
			@Override
			protected boolean doStart() {
				return false;
			}
		};
		assertEquals("Module state should be UNKNOWN", Module.State.UNKNOWN, module.getState());
		assertEquals("Module should successfully reset()", true, module.reset());
		assertEquals("Module state should be RESET", Module.State.RESET, module.getState());
		assertEquals("Module should fail to start()", false, module.start());
		assertEquals("Module state should be UNKNOWN", Module.State.UNKNOWN, module.getState());
		assertEquals("Module should successfully reset()", true, module.reset());
		assertEquals("Module state should be RESET", Module.State.RESET, module.getState());
	}

	@Test
	public void failingStopTest() {
		Module module = new AbstractModuleAdapter() {
			@Override
			protected boolean doStop() {
				return false;
			}
		};
		assertEquals("Module state should be UNKNOWN", Module.State.UNKNOWN, module.getState());
		assertEquals("Module should successfully reset()", true, module.reset());
		assertEquals("Module state should be RESET", Module.State.RESET, module.getState());
		assertEquals("Module should successfully start()", true, module.start());
		assertEquals("Module state should be STARTED", Module.State.STARTED, module.getState());
		assertEquals("Module should fail to stop()", false, module.stop());
		assertEquals("Module state should be UNKNOWN", Module.State.UNKNOWN, module.getState());
		assertEquals("Module should successfully reset()", true, module.reset());
		assertEquals("Module state should be RESET", Module.State.RESET, module.getState());
	}

}
