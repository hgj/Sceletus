package hu.hgj.sceletus.queue;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.module.Module;
import hu.hgj.sceletus.module.ModuleManager;
import hu.hgj.sceletus.module.ModuleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Function;

public class QueueManager {

	private static final Logger logger = LoggerFactory.getLogger(QueueManager.class);

	public static final ModuleRegistry<Module> queueRegistry = new ModuleRegistry<>("Queues");

	public static Module createQueue(String queueName, String queueClassName, int workers, boolean allowDuplicates, boolean keepElements) {
		Class<? extends Module> queueClass = ModuleManager.findModule(queueClassName);
		if (queueClass != null) {
			Constructor<? extends Module> queueConstructor;
			try {
				queueConstructor = queueClass.getConstructor(String.class, int.class, boolean.class, boolean.class);
			} catch (NoSuchMethodException exception) {
				logger.error("Can not find constructor for queue '{}'.", queueClass.getCanonicalName(), exception);
				return null;
			}
			try {
				return queueConstructor.newInstance(queueName, workers, allowDuplicates, keepElements);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
				logger.error("Can not construct queue '{}'.", queueClass.getCanonicalName(), exception);
				return null;
			}
		}
		return null;
	}

	public static List<Object> parseQueuesConfiguration(File configurationFile) throws IOException {
		return JsonPath.read(configurationFile, "$.queues[*]");
	}

	public static boolean createQueues(List<Object> queuesConfiguration) {
		if (queuesConfiguration.size() == 0) {
			return false;
		}
		for (Object queueConfiguration : queuesConfiguration) {
			String queueName;
			String queueClass;
			try {
				queueName = JsonPath.read(queueConfiguration, "$.name");
				queueClass = JsonPath.read(queueConfiguration, "$.class");
			} catch (PathNotFoundException exception) {
				logger.error("Can not load queue without 'name' or 'class'.");
				return false;
			}
			boolean enabled = true;
			try {
				enabled = JsonPath.read(queueConfiguration, "$.enabled");
			} catch (PathNotFoundException exception) {
				// Ignore and leave module enabled
			}
			try {
				enabled = !((boolean) JsonPath.read(queueConfiguration, "$.disabled"));
			} catch (PathNotFoundException exception) {
				// Ignore and leave module enabled
			}
			if (!enabled) {
				logger.info("Not loading module '{}' as configuration says it should be disabled.", queueName);
				continue;
			}
			int workers = 1;
			try {
				workers = JsonPath.read(queueConfiguration, "$.workers");
			} catch (PathNotFoundException exception) {
				// Ignore and leave module enabled
			}
			boolean allowDuplicates = false;
			try {
				allowDuplicates = JsonPath.read(queueConfiguration, "$.allowDuplicates");
			} catch (PathNotFoundException exception) {
				// Ignore and leave module enabled
			}
			boolean keepElements = false;
			try {
				workers = JsonPath.read(queueConfiguration, "$.keepElements");
			} catch (PathNotFoundException exception) {
				// Ignore and leave module enabled
			}
			Module module = createQueue(queueName, queueClass, workers, allowDuplicates, keepElements);
			if (module != null) {
				if (!queueRegistry.register(module)) {
					logger.error("Failed to register queue '{}' ({}).", queueName, queueClass);
				}
			} else {
				logger.error("Failed to create queue '{}' ({}).", queueName, queueClass);
				return false;
			}
		}
		return true;
	}

	public static boolean createAndStartQueues(List<Object> queuesConfiguration) {
		if (createQueues(queuesConfiguration)) {
			boolean allStarted = true;
			for (Module module : queueRegistry.getAll()) {
				allStarted &= module.start();
			}
			return allStarted;
		} else {
			return false;
		}
	}

	public static <E> TopicQueue<E> getOrCreateQueue(String queueName, Function<String, ? extends TopicQueue<E>> queueSupplier) {
		try {
			TopicQueue<E> existingQueue = (TopicQueue<E>) queueRegistry.get(queueName);
			if (existingQueue != null) {
				return existingQueue;
			} else {
				TopicQueue<E> newQueue = queueSupplier.apply(queueName);
				if (queueRegistry.register(newQueue)) {
					return newQueue;
				} else {
					logger.error("Failed to register queue '{}' ({}).", newQueue.getName(), newQueue.getClass().getCanonicalName());
					return null;
				}
			}
		} catch (ClassCastException exception) {
			logger.error("Requested queue '{}' already exists, but is not the right type (got {}).",
					queueName, queueRegistry.get(queueName).getClass().getCanonicalName(), exception
			);
			return null;
		}
	}

	public static <E> TopicQueue<E> configureQueue(Object configuration, String path, Function<String, ? extends TopicQueue<E>> queueProvider) {
		try {
			String queueName = JsonPath.read(configuration, path);
			return QueueManager.getOrCreateQueue(queueName, queueProvider);
		} catch (PathNotFoundException exception) {
			logger.error("Can not configure queue. Missing configuration '{}'.", path, exception);
			return null;
		}
	}

	public static void waitForEmptyQueue(TopicQueue queue) throws InterruptedException {
		waitForEmptyQueue(queue, 100);
	}

	public static void waitForEmptyQueue(TopicQueue queue, int sleepTimeMillis) throws InterruptedException {
		while (queue.size() > 0) {
			Thread.sleep(sleepTimeMillis);
		}
	}

}
