package hu.hgj.sceletus.modules.simple;

import hu.hgj.sceletus.module.simple.Deduper;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeduperTests {

	@Test
	public void testInfiniteDeduping() {
		Deduper<String> stringDeduper = new Deduper<>();
		assertEquals("Expecting dedupe window to be infinite.", Deduper.INFINITE_DEDUPE_WINDOW_DURATION, stringDeduper.getDedupeWindow());
		String string = "foobar";
		assertFalse("Expecting first occurrence not to be a duplicate.", stringDeduper.isDuplicate(string));
		assertFalse("Expecting first occurrence not to be a duplicate.", stringDeduper.dedupe(string));
		assertTrue("Expecting second occurrence to be a duplicate.", stringDeduper.dedupe(string));
		try {
			Thread.sleep(2000);
		} catch (InterruptedException ignored) {
			// Ignored
		}
		assertTrue("Expecting third occurrence to be a duplicate (after 2 seconds).", stringDeduper.dedupe(string));
	}

	@Test
	public void testOneSecondDeduping() {
		Deduper<String> stringDeduper = new Deduper<>(Duration.ofSeconds(1));
		assertEquals("Expecting dedupe window to be one second.", Duration.ofSeconds(1), stringDeduper.getDedupeWindow());
		String string = "foobar";
		assertFalse("Expecting first occurrence not to be a duplicate.", stringDeduper.isDuplicate(string));
		assertFalse("Expecting first occurrence not to be a duplicate.", stringDeduper.dedupe(string));
		assertTrue("Expecting second occurrence to be a duplicate.", stringDeduper.dedupe(string));
		try {
			Thread.sleep(2000);
		} catch (InterruptedException ignored) {
			// Ignored
		}
		assertFalse("Expecting third occurrence not to be a duplicate (after 2 seconds).", stringDeduper.isDuplicate(string));
		assertFalse("Expecting third occurrence not to be a duplicate (after 2 seconds).", stringDeduper.dedupe(string));
	}

}
