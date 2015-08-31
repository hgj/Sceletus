package hu.hgj.sceletus.module.simple;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.queue.WithTopic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation to {@link ConverterModule} that deduplicates incoming
 * elements.
 * <p>
 * In addition to the configuration of the {@link ConverterModule}, the module
 * is configured with the {@code dedupeWindow} value that specifies the size of
 * the deduplication window in seconds. See {@link #DEFAULT_DEDUPE_WINDOW} for
 * default value.
 * <p>
 * The module drops (does not output) any element, that has been seen within
 * the {@code dedupeWindow} long deduplication window. A window size of zero
 * means "infinite".
 *
 * @param <T> The type of input (and thus, the output) element.
 *
 * @see ConverterModule
 */
public class DeduperModule<T> extends ConverterModule<T, T> {

	public static long DEFAULT_DEDUPE_WINDOW = 60;

	protected long dedupeWindowSeconds = DEFAULT_DEDUPE_WINDOW;
	protected long dedupeWindowNano = dedupeWindowSeconds * 1_000_000_000;

	protected ConcurrentHashMap<WithTopic<T>, Long> cache = new ConcurrentHashMap<>();

	public DeduperModule(String name) {
		super(name);
	}

	public DeduperModule(String name, long dedupeWindowSeconds) {
		super(name);
		this.dedupeWindowSeconds = dedupeWindowSeconds;
	}

	@Override
	public boolean updateConfiguration(Object configuration) {
		if (!super.updateConfiguration(configuration)) {
			return false;
		}
		try {
			dedupeWindowSeconds = JsonPath.read(configuration, "$.dedupeWindow");
		} catch (PathNotFoundException ignored) {
			// Use default
		}
		dedupeWindowNano = dedupeWindowSeconds * 1_000_000_000;
		return true;
	}

	@Override
	protected List<WithTopic<T>> convertElement(WithTopic<T> inputElement) {
		long now = System.nanoTime();
		List<WithTopic<T>> outputElements = new ArrayList<>(1);
		// See if we have to drop this element
		if (cache.containsKey(inputElement)) {
			if (dedupeWindowSeconds == 0) {
				// No need to update timestamp
				return outputElements;
			} else {
				if (now - cache.get(inputElement) < dedupeWindowNano) {
					// We update the timestamp here, as the window is not "infinite"
					cache.put(inputElement, now);
					return outputElements;
				}
			}
		}
		cache.put(inputElement, now);
		outputElements.add(inputElement);
		return outputElements;
	}

}
