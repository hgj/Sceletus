package hu.hgj.sceletus.module.simple;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class Deduper<E> {

	public static final Duration INFINITE_DEDUPE_WINDOW_DURATION = Duration.ZERO;
	public static final Duration DEFAULT_DEDUPE_WINDOW_DURATION = INFINITE_DEDUPE_WINDOW_DURATION;

	protected Duration dedupeWindowDuration = DEFAULT_DEDUPE_WINDOW_DURATION;

	protected ConcurrentHashMap<E, Instant> cache = new ConcurrentHashMap<>();

	public Deduper() {
	}

	public Deduper(Duration duration) {
		setDedupeWindow(duration);
	}

	public Duration getDedupeWindow() {
		return dedupeWindowDuration;
	}

	public void setDedupeWindow(Duration duration) {
		this.dedupeWindowDuration = duration;
	}

	public boolean isDuplicate(E element) {
		Instant now = Instant.now();
		if (cache.containsKey(element)) {
			if (dedupeWindowDuration.equals(INFINITE_DEDUPE_WINDOW_DURATION)
					|| Duration.between(now, cache.get(element)).abs().compareTo(dedupeWindowDuration) <= 0) {
				return true;
			}
		}
		return false;
	}

	public boolean dedupe(E element) {
		Instant now = Instant.now();
		if (isDuplicate(element)) {
			if (!dedupeWindowDuration.equals(INFINITE_DEDUPE_WINDOW_DURATION)) {
				// NOTE: We do not update timestamps if the window is "infinite"
				cache.put(element, now);
			}
			return true;
		} else {
			cache.put(element, now);
			return false;
		}
	}

}
