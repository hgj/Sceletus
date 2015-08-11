package hu.hgj.sceletus;

import hu.hgj.sceletus.module.ModuleManager;

import java.io.File;
import java.io.IOException;

public class Main {

	public static final int E_USAGE = 1;
	public static final int E_IO = 2;
	public static final int E_CONFIGURATION = 3;

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("No configuration file provided.");
			System.exit(E_USAGE);
		}
		File configurationFile = new File(args[0]);
		try {
			if (!ModuleManager.createAndStartQueues(ModuleManager.parseQueuesConfiguration(configurationFile))) {
				System.err.println("Failed to start queues.");
				System.exit(E_CONFIGURATION);
			}
		} catch (IOException exception) {
			exception.printStackTrace();
			System.exit(E_IO);
		}
		try {
			if (!ModuleManager.createAndStartModules(ModuleManager.parseModulesConfiguration(configurationFile))) {
				System.err.println("Failed to start modules.");
				System.exit(E_CONFIGURATION);
			}
		} catch (IOException exception) {
			exception.printStackTrace();
			System.exit(E_IO);
		}
		System.out.printf("Sceletus started up with %d modules and %d queues registered.",
				ModuleManager.moduleRegistry.getAll().size(),
				ModuleManager.queueRegistry.getAll().size()
		);
	}

}
