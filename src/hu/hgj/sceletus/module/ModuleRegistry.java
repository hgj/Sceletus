package hu.hgj.sceletus.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleRegistry<M extends Module> {

	private static final Logger logger = LoggerFactory.getLogger(ModuleRegistry.class);

	private final String name;

	private final Map<String, M> modules = new ConcurrentHashMap<>();

	public ModuleRegistry(String name) {
		this.name = name;
	}

	public boolean register(Module module) {
		if (modules.putIfAbsent(module.getName(), (M) module) != null) {
			logger.warn("Not registering Module named '{}' in ModuleRegistry '{}' as it is already registered.", module.getName(), name);
			return false;
		}
		return true;
	}

	public boolean registerAndStart(M module) {
		if (register(module)) {
			try {
				if (module.start()) {
					return true;
				} else {
					remove(module);
					logger.warn("Not registering Module named '{}' in ModuleRegistry '{}' as it failed to be started.", module.getName(), name);
					return false;
				}
			} catch (Exception exception) {
				logger.error("Exception caught while starting module '{}'.", module.getName(), exception);
				return false;
			}
		} else {
			return false;
		}
	}

	public M remove(String name) {
		return modules.remove(name);
	}

	public boolean remove(M module) {
		return modules.remove(module.getName(), module);
	}

	public M get(String name) {
		return modules.get(name);
	}

	public Collection<M> getAll() {
		return modules.values();
	}

}
