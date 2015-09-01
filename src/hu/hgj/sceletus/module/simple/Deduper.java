package hu.hgj.sceletus.module.simple;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Deduper<T> {

	public static final long DEFAULT_DEDUPE_WINDOW_NANO = TimeUnit.SECONDS.toNanos(60);

	protected long dedupeWindowNano = DEFAULT_DEDUPE_WINDOW_NANO;

	public long getDedupeWindow() {
		return dedupeWindowNano;
	}

	public void setDedupeWindow(long dedupeWindowNano) {
		this.dedupeWindowNano = dedupeWindowNano;
	}

	public void setDedupeWindow(long duration, TimeUnit timeUnit) {
		this.dedupeWindowNano = timeUnit.toNanos(duration);
	}

	protected ConcurrentHashMap<T, Long> cache = new ConcurrentHashMap<>();

	public Deduper() {
	}

	public Deduper(long dedupeWindowNano) {
		setDedupeWindow(dedupeWindowNano);
	}

	public Deduper(long duration, TimeUnit timeUnit) {
		setDedupeWindow(duration, timeUnit);
	}

	public boolean isDuplicate(T element) {
		if (cache.containsKey(element)) {
			if (dedupeWindowNano == 0 || (System.nanoTime() - cache.get(element)) < dedupeWindowNano) {
				return true;
			}
		}
		return false;
	}

	public boolean dedupe(T element) {
		if (isDuplicate(element)) {
			if (dedupeWindowNano != 0) {
				// NOTE: We do not update timestamps if the window is "infinite"
				cache.put(element, System.nanoTime());
			}
			return true;
		} else {
			cache.put(element, System.nanoTime());
			return false;
		}
	}

}