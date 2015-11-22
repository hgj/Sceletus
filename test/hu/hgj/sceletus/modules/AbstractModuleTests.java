package hu.hgj.sceletus.modules;

import hu.hgj.sceletus.module.AbstractModule;
import hu.hgj.sceletus.module.simple.DeduperModule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AbstractModuleTests {

	@Test
	public void testToString() {
		AbstractModule module = new DeduperModule<>("MyDeduperModule");
		assertEquals(
				"toString() should return the expected string.",
				"DeduperModule{name='MyDeduperModule', state='UNKNOWN'}",
				module.toString()
		);
	}

	@Test
	public void testEquals() {
		AbstractModule moduleOne = new DeduperModule<>("MyDeduperModule");
		AbstractModule moduleTwo = new DeduperModule<>("MyDeduperModule");
		assertTrue("Expecting two modules to equal.", moduleTwo.equals(moduleOne));
	}

}
