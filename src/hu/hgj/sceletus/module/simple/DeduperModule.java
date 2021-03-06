package hu.hgj.sceletus.module.simple;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.module.ConverterModule;
import hu.hgj.sceletus.queue.WithTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

/**
 * An implementation to {@link ConverterModule} that deduplicates incoming
 * elements.
 * <p>
 * In addition to the configuration of the {@link ConverterModule}, the module
 * is configured with the {@code dedupeWindow} value that specifies the size of
 * the deduplication window's duration in ISO-8601 format.
 * See {@link Deduper#DEFAULT_DEDUPE_WINDOW_DURATION} for default value.
 * <p>
 * The module drops (does not output) any element, that has been seen within the
 * {@code dedupeWindow} long deduplication window. A window size of zero means
 * "infinite".
 *
 * @param <T> The type of input (and thus, the output) topic.
 * @param <E> The type of input (and thus, the output) element.
 *
 * @see ConverterModule
 */
public class DeduperModule<T, E> extends ConverterModule<T, E, T, E> {

	private static final Logger logger = LoggerFactory.getLogger(DeduperModule.class);

	protected Deduper<WithTopic<T, E>> deduper = new Deduper<>();

	public DeduperModule(String name) {
		super(name);
	}

	public DeduperModule(String name, Duration dedupeWindowDuration) {
		super(name);
		deduper.setDedupeWindow(dedupeWindowDuration);
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
				deduper.setDedupeWindow(Duration.parse(windowDuration));
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
		logger.info("Updated configuration: dedupeWindow={}", deduper.getDedupeWindow().toString());
		return true;
	}

	@Override
	protected List<WithTopic<T, E>> convertElement(WithTopic<T, E> inputElementWithTopic) {
		if (deduper.dedupe(inputElementWithTopic)) {
			return Collections.emptyList();
		} else {
			return Collections.singletonList(inputElementWithTopic);
		}
	}

}
