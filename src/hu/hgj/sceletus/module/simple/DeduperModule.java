package hu.hgj.sceletus.module.simple;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.module.ConverterModule;
import hu.hgj.sceletus.queue.WithTopic;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An implementation to {@link ConverterModule} that deduplicates incoming
 * elements.
 * <p>
 * In addition to the configuration of the {@link ConverterModule}, the module
 * is configured with the {@code dedupeWindow} value that specifies the size of
 * the deduplication window's duration in ISO-8601 format.
 * See {@link Deduper#DEFAULT_DEDUPE_WINDOW_NANO} for default value.
 * <p>
 * The module drops (does not output) any element, that has been seen within
 * the {@code dedupeWindow} long deduplication window. A window size of zero
 * means "infinite".
 *
 * @param <T> The type of input (and thus, the output) element.
 * @see ConverterModule
 */
public class DeduperModule<T> extends ConverterModule<T, T> {

	protected Deduper<WithTopic<T>> deduper = new Deduper<>();

	public DeduperModule(String name) {
		super(name);
	}

	public DeduperModule(String name, long dedupeWindowNano) {
		super(name);
		deduper.setDedupeWindow(dedupeWindowNano);
	}

	public DeduperModule(String name, long duration, TimeUnit timeUnit) {
		super(name);
		deduper.setDedupeWindow(duration, timeUnit);
	}

	@Override
	public boolean updateConfiguration(Object configuration) {
		if (!super.updateConfiguration(configuration)) {
			return false;
		}
		String dedupeWindowField = "dedupeWindow";
		try {
			String windowDuration = JsonPath.read(configuration, "$." + dedupeWindowField);
			try {
				deduper.setDedupeWindow(Duration.parse(windowDuration).toNanos());
			} catch (DateTimeParseException exception) {
				logger.error("Failed to parse '{}' as an ISO-8601 duration.", windowDuration, exception);
				return false;
			} catch (ArithmeticException exception) {
				logger.error("Configured duration is too big to fit in a long.", exception);
				return false;
			}
		} catch (PathNotFoundException ignored) {
			// Use default
		}
		return true;
	}

	@Override
	protected List<WithTopic<T>> convertElement(WithTopic<T> inputElement) {
		if (deduper.dedupe(inputElement)) {
			return Collections.emptyList();
		} else {
			return Collections.singletonList(inputElement);
		}
	}

}
