package hu.hgj.sceletus.module;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

public class ModuleManager {

	private static final Logger logger = LoggerFactory.getLogger(ModuleManager.class);

	public static final ModuleRegistry<Module> moduleRegistry = new ModuleRegistry<>("Modules");

	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Shutdown hook stopping all modules.");
				stopModules(moduleRegistry.getAll());
			}
		});
	}

	public static Class<? extends Module> findModule(String className) {
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

	public static Module createModule(String moduleName, String className) {
		Class<? extends Module> moduleClass = findModule(className);
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

	public static List<Object> parseModulesConfiguration(File configurationFile) throws IOException {
		return JsonPath.read(configurationFile, "$.modules[*]");
	}

	public static boolean createModules(List<Object> modulesConfiguration) {
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
			Module module = createModule(moduleName, moduleClass);
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
				moduleRegistry.register(module);
			} else {
				logger.error("Failed to create module '{}' ({}).", moduleName, moduleClass);
				return false;
			}
		}
		return true;
	}

	public static boolean createAndStartModules(List<Object> modulesConfiguration) throws IOException {
		if (createModules(modulesConfiguration)) {
			boolean allStarted = true;
			for (Module module : moduleRegistry.getAll()) {
				allStarted &= module.start();
			}
			return allStarted;
		} else {
			return false;
		}
	}

	public static boolean stopModules(Collection<Module> modules) {
		boolean allStopped = true;
		for (Module module : modules) {
			allStopped &= module.stop();
		}
		return allStopped;
	}

}