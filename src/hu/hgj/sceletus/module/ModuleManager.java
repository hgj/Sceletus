package hu.hgj.sceletus.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ModuleManager {

	private static final Logger logger = LoggerFactory.getLogger(ModuleManager.class);

	private static final ModuleRegistry<Module> moduleRegistry = new ModuleRegistry<>();

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
		}
	}

	public static Module createModule(String className) {
		Class<? extends Module> moduleClass = findModule(className);
		if (moduleClass != null) {
			Constructor<? extends Module> moduleConstructor;
			try {
				moduleConstructor = moduleClass.getConstructor();
			} catch (NoSuchMethodException exception) {
				logger.error("Can not find constructor for module '{}'.", moduleClass.getCanonicalName(), exception);
				return null;
			}
			try {
				return moduleConstructor.newInstance();
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
				logger.error("Can not construct module '{}'.", moduleClass.getCanonicalName(), exception);
				return null;
			}
		}
		return null;
	}

	public static boolean loadModules(File configurationFile) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> configurationMap;
		try {
			configurationMap = (Map<String, Object>) objectMapper.readValue(configurationFile, Map.class);
		} catch (ClassCastException exception) {
			logger.error("Can not load modules, as configuration is invalid.", exception);
			return false;
		}
		if (!configurationMap.containsKey("modules")) {
			logger.error("Can not load any modules, as configuration does not contain 'modules' part.");
			return false;
		}
		List<Map<String, Object>> modulesConfiguration;
		try {
			modulesConfiguration = (List<Map<String, Object>>) configurationMap.get("modules");
		} catch (ClassCastException exception) {
			logger.error("Can not load modules, as configuration's 'modules' part is invalid.", exception);
			return false;
		}
		for (Map<String, Object> moduleConfiguration : modulesConfiguration) {
			if (!moduleConfiguration.containsKey("name") || !moduleConfiguration.containsKey("class")) {
				logger.error("Can not load module without 'name' or 'class'.");
				return false;
			}
			Module module = ModuleManager.createModule((String) moduleConfiguration.get("class"));
			if (module != null) {
				if (moduleConfiguration.containsKey("configuration")) {
					try {
						module.updateConfiguration((Map<String, Object>) moduleConfiguration.get("configuration"));
					} catch (ClassCastException exception) {
						logger.error("Can not configure module '{}' ({}), as module's configuration is invalid.", moduleConfiguration.get("name"), moduleConfiguration.get("class"), exception);
						return false;
					}
				}
				moduleRegistry.register(module);
			} else {
				logger.error("Failed to create module '{}'.", moduleConfiguration.get("name"));
				return false;
			}
		}
		return true;
	}

	public static boolean loadAndStartModules(File configurationFile) throws IOException {
		if (loadModules(configurationFile)) {
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