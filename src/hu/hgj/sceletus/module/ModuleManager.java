package hu.hgj.sceletus.module;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import hu.hgj.sceletus.queue.TopicQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ModuleManager {

	private static final Logger logger = LoggerFactory.getLogger(ModuleManager.class);

	public static final ModuleRegistry<Module> moduleRegistry = new ModuleRegistry<>("Modules");
	public static final ModuleRegistry<Module> queueRegistry = new ModuleRegistry<>("Queues");

	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Shutdown hook stopping all queues.");
				stopAbstractModules(queueRegistry.getAll());
				logger.info("Shutdown hook stopping all modules.");
				stopAbstractModules(moduleRegistry.getAll());
			}
		});
	}

	public static Class<? extends Module> findAbstractModule(String className) {
		try {
			return (Class<? extends Module>) Class.forName(className);
		} catch (ClassNotFoundException exception) {
			logger.error("Can not find class '{}'.", className, exception);
			return null;
		} catch (ClassCastException exception) {
			logger.error("Can not cast class '{}' to Module.", className, exception);
			return null;
		}
	}

	public static Module createAbstractModule(String moduleName, String className) {
		Class<? extends Module> moduleClass = findAbstractModule(className);
		if (moduleClass != null) {
			Constructor<? extends Module> moduleConstructor;
			try {
				moduleConstructor = moduleClass.getConstructor(String.class);
			} catch (NoSuchMethodException exception) {
				logger.error("Can not find constructor for module '{}'.", moduleClass.getCanonicalName(), exception);
				return null;
			}
			try {
				return moduleConstructor.newInstance(moduleName);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
				logger.error("Can not construct module '{}'.", moduleClass.getCanonicalName(), exception);
				return null;
			}
		}
		return null;
	}

	public static List<Object> parseQueuesConfiguration(File configurationFile) throws IOException {
		return JsonPath.read(configurationFile, "$.queues[*]");
	}

	public static List<Object> parseModulesConfiguration(File configurationFile) throws IOException {
		return JsonPath.read(configurationFile, "$.modules[*]");
	}

	public static boolean createQueues(List<Object> modulesConfiguration) {
		return createAbstractModules(modulesConfiguration, queueRegistry);
	}

	public static boolean createModules(List<Object> modulesConfiguration) {
		return createAbstractModules(modulesConfiguration, moduleRegistry);
	}

	public static boolean createAbstractModules(List<Object> modulesConfiguration, ModuleRegistry<Module> registry) {
		if (modulesConfiguration.size() == 0) {
			return false;
		}
		for (Object moduleConfiguration : modulesConfiguration) {
			String moduleName;
			String moduleClass;
			try {
				moduleName = JsonPath.read(moduleConfiguration, "$.name");
				moduleClass = JsonPath.read(moduleConfiguration, "$.class");
			} catch (PathNotFoundException exception) {
				logger.error("Can not load module without 'name' or 'class'.");
				return false;
			}
			boolean moduleEnabled = true;
			try {
				moduleEnabled = JsonPath.read(moduleConfiguration, "$.enabled");
			} catch (PathNotFoundException exception) {
				// Ignore and leave module enabled
			}
			try {
				moduleEnabled = !((boolean) JsonPath.read(moduleConfiguration, "$.disabled"));
			} catch (PathNotFoundException exception) {
				// Ignore and leave module enabled
			}
			if (!moduleEnabled) {
				logger.info("Not loading module '{}' as configuration says it should be disabled.", moduleName);
				continue;
			}
			Module module = createAbstractModule(moduleName, moduleClass);
			if (module != null) {
				try {
					Object customModuleConfiguration = JsonPath.read(moduleConfiguration, "$.configuration");
					if (!module.updateConfiguration(customModuleConfiguration)) {
						logger.error("Failed to configure module '{}' ({}).", moduleName, moduleClass);
						return false;
					}
				} catch (PathNotFoundException exception) {
					logger.info("No custom configuration found for module '{}' ({}). Not configuring.", moduleName, moduleClass);
				}
				if (!registry.register(module)) {
					logger.error("Failed to register module '{}' ({}).", moduleName, moduleClass);
					return false;
				}
			} else {
				logger.error("Failed to create module '{}' ({}).", moduleName, moduleClass);
				return false;
			}
		}
		return true;
	}

	public static boolean createAndStartQueues(List<Object> modulesConfiguration) throws IOException {
		return createAndStartAbstractModules(modulesConfiguration, queueRegistry);
	}

	public static boolean createAndStartModules(List<Object> modulesConfiguration) throws IOException {
		return createAndStartAbstractModules(modulesConfiguration, moduleRegistry);
	}

	public static boolean createAndStartAbstractModules(List<Object> modulesConfiguration, ModuleRegistry<Module> registry) throws IOException {
		if (createAbstractModules(modulesConfiguration, registry)) {
			boolean allStarted = true;
			for (Module module : registry.getAll()) {
				allStarted &= module.start();
			}
			return allStarted;
		} else {
			return false;
		}
	}

	public static <E> TopicQueue<E> getConfiguredQueue(Object configuration, String path) throws Exception {
		try {
			String queueName = JsonPath.read(configuration, path);
			return (TopicQueue<E>) queueRegistry.get(queueName);
		} catch (PathNotFoundException exception) {
			String error = "Can not get queue. Missing configuration '" + path + "'.";
			logger.error(error, exception);
			throw new Exception(error, exception);
		} catch (ClassCastException exception) {
			String error = "Can not get queue. Registered queue is not the right type.";
			logger.error(error, exception);
			throw new Exception(error, exception);
		}
	}

	public static <E> TopicQueue<E> getOrCreateQueue(String queueName, Function<String, ? extends TopicQueue<E>> queueSupplier) throws Exception {
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
					throw new Exception("Failed to register queue.");
				}
			}
		} catch (ClassCastException exception) {
			logger.error("Requested queue '{}' already exists, but is not the right type (got {}).",
					queueName, queueRegistry.get(queueName).getClass().getCanonicalName(), exception
			);
			throw new Exception("Requested queue already exists, but is not the right type.", exception);
		}
	}

	public static <E> TopicQueue<E> getOrCreateConfiguredQueue(Object configuration, String path, Function<String, ? extends TopicQueue<E>> queueProvider) throws Exception {
		try {
			String queueName = JsonPath.read(configuration, path);
			return getOrCreateQueue(queueName, queueProvider);
		} catch (PathNotFoundException exception) {
			logger.error("Can not configure queue. Missing configuration '{}'.", path, exception);
			throw new Exception("Can not configure queue.", exception);
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

	public static boolean stopAbstractModules(Collection<Module> modules) {
		boolean allStopped = true;
		for (Module module : modules) {
			allStopped &= module.stop();
		}
		return allStopped;
	}

}