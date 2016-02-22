package hu.hgj.sceletus.module.simple;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.module.ConsumerModule;
import hu.hgj.sceletus.queue.TopicQueue;
import hu.hgj.sceletus.queue.WithTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.function.Predicate;

public class SimpleWriterModule extends ConsumerModule<Object, Object> {

	private static final Logger logger = LoggerFactory.getLogger(SimpleWriterModule.class);

	protected File outputFile = null;
	protected Writer outputFileWriter = null;

	protected boolean append = false;

	protected String outputFormat = "%TOPIC: %ELEMENT\n";

	public SimpleWriterModule(String name) {
		super(name);
	}

	public SimpleWriterModule(String name, TopicQueue<Object, Object> inputQueue) {
		super(name, inputQueue);
	}

	public SimpleWriterModule(String name, TopicQueue<Object, Object> inputQueue, Predicate<Object> inputQueueFilters) {
		super(name, inputQueue, inputQueueFilters);
	}

	@Override
	public boolean updateConfiguration(Object configuration) {
		if (!super.updateConfiguration(configuration)) {
			return false;
		}
		String outputFilePath;
		try {
			outputFilePath = JsonPath.read(configuration, "$.outputFile");
			if (!outputFilePath.equals("-")) {
				outputFile = new File(outputFilePath);
				if (!outputFile.canWrite()) {
					logger.error("Output file '{}' is not writable.", outputFile.getAbsolutePath());
					return false;
				}
			}
		} catch (PathNotFoundException ignored) {
			// Ignored, will default to standard output
		}
		if (outputFile != null) {
			try {
				append = JsonPath.read(configuration, "$.append");
			} catch (PathNotFoundException ignored) {
				// Ignored, will use default value
			}
		}
		return true;
	}

	@Override
	protected boolean doStart() {
		if (!super.doStart()) {
			return false;
		}
		try {
			outputFileWriter = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(outputFile, append),
							Charset.defaultCharset()
					)
			);
		} catch (IOException exception) {
			logger.error("Exception while opening file for writing.", exception);
			return false;
		}
		return true;
	}

	@Override
	protected boolean consumeElement(WithTopic<Object, Object> elementWithTopic) {
		String output = outputFormat
				.replace("%TOPIC", elementWithTopic.topic.toString())
				.replace("%ELEMENT", elementWithTopic.element.toString());
		if (outputFileWriter == null) {
			System.out.print(output);
		} else {
			try {
				outputFileWriter.write(output);
			} catch (IOException exception) {
				logger.warn("Exception while writing to file.", exception);
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean doStop() {
		if (!super.doStop()) {
			return false;
		}
		if (outputFileWriter != null) {
			try {
				outputFileWriter.close();
			} catch (IOException exception) {
				logger.error("Exception while closing the file writer.", exception);
				return false;
			}
			logger.info("Closed file writer.");
		}
		return true;
	}

}
