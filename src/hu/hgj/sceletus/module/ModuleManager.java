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
import java.util.ArrayList;
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

	public static boolean createQueues(List<Object> queuesConfiguration) {
		return createAbstractModules(queuesConfiguration, queueRegistry);
	}

	public static boolean createModules(List<Object> modulesConfiguration) {
		return createAbstractModules(modulesConfiguration, moduleRegistry);
	}

	public static boolean createAbstractModules(List<Object> modulesConfiguration, ModuleRegistry<Module> registry) {
		if (modulesConfiguration.size() == 0) {
			return true;
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
					try {
						if (!module.updateConfiguration(customModuleConfiguration)) {
							logger.error("Failed to configure module '{}' ({}).", moduleName, moduleClass);
							return false;
						}
					} catch (Exception exception) {
						logger.error("Failed to configure module (exception caught) '{}' ({}).", moduleName, moduleClass, exception);
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
				try {
					allStarted &= module.start();
				} catch (Exception exception) {
					logger.error("Exception caught while starting module '{}'.", module.getName(), exception);
					allStarted = false;
				}
			}
			return allStarted;
		} else {
			return false;
		}
	}

	public static <M extends Module> M getModule(String moduleName) {
		try {
			return (M) moduleRegistry.get(moduleName);
		} catch (ClassCastException exception) {
			String error = "Can not get module. Registered module is not the right type.";
			logger.error(error, exception);
			throw new RuntimeException(error, exception);
		}
	}

	public static <M extends Module> M getConfiguredModule(Object configuration, String path) {
		try {
			String moduleName = JsonPath.read(configuration, path);
			return getModule(moduleName);
		} catch (PathNotFoundException exception) {
			String error = "Can not get module. Missing configuration '" + path + "'.";
			logger.error(error, exception);
			throw new RuntimeException(error, exception);
		}
	}

	public static <Q extends TopicQueue<?, ?>> Q getQueue(String queueName) {
		try {
			return (Q) queueRegistry.get(queueName);
		} catch (ClassCastException exception) {
			String error = "Can not get queue. Registered queue is not the right type.";
			logger.error(error, exception);
			throw new RuntimeException(error, exception);
		}
	}

	public static <Q extends TopicQueue<?, ?>> List<Q> getQueues(List<String> queueNames) {
		List<Q> queues = new ArrayList<>();
		for (String queueName : queueNames) {
			queues.add(getQueue(queueName));
		}
		return queues;
	}

	public static <Q extends TopicQueue<?, ?>> Q getConfiguredQueue(Object configuration, String path) {
		try {
			String queueName = JsonPath.read(configuration, path);
			return getQueue(queueName);
		} catch (PathNotFoundException exception) {
			String error = "Can not get queue. Missing configuration '" + path + "'.";
			logger.error(error, exception);
			throw new RuntimeException(error, exception);
		}
	}

	public static <Q extends TopicQueue<?, ?>> List<Q> getConfiguredQueues(Object configuration, String path) {
		try {
			List<String> queueNames = JsonPath.read(configuration, path);
			return getQueues(queueNames);
		} catch (PathNotFoundException exception) {
			String error = "Can not get queues. Missing configuration '" + path + "'.";
			logger.error(error, exception);
			throw new RuntimeException(error, exception);
		}
	}

	public static <Q extends TopicQueue<?, ?>> Q getOrCreateQueue(String queueName, Function<String, Q> queueSupplier) {
		try {
			Q existingQueue = getQueue(queueName);
			if (existingQueue != null) {
				return existingQueue;
			} else {
				Q newQueue = queueSupplier.apply(queueName);
				if (queueRegistry.register(newQueue)) {
					return newQueue;
				} else {
					logger.error("Failed to register queue '{}' ({}).", newQueue.getName(), newQueue.getClass().getCanonicalName());
					throw new RuntimeException("Failed to register queue.");
				}
			}
		} catch (ClassCastException exception) {
			logger.error("Requested queue '{}' already exists, but is not the right type (got {}).",
					queueName, queueRegistry.get(queueName).getClass().getCanonicalName(), exception
			);
			throw new RuntimeException("Requested queue already exists, but is not the right type.", exception);
		}
	}

	public static <Q extends TopicQueue<?, ?>> Q getOrCreateConfiguredQueue(Object configuration, String path, Function<String, Q> queueSupplier) {
		try {
			String queueName = JsonPath.read(configuration, path);
			return getOrCreateQueue(queueName, queueSupplier);
		} catch (PathNotFoundException exception) {
			logger.error("Can not configure queue. Missing configuration '{}'.", path, exception);
			throw new RuntimeException("Can not configure queue.", exception);
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
			try {
				allStopped &= module.stop();
			} catch (Exception exception) {
				logger.error("Exception caught while stopping module '{}'.", module.getName(), exception);
				allStopped = false;
			}
		}
		return allStopped;
	}

}