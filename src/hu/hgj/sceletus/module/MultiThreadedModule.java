package hu.hgj.sceletus.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public abstract class MultiThreadedModule extends AbstractModuleAdapter {

	public int threadJoinTimeout = 2000;

	protected ArrayList<Thread> threads;

	protected boolean running = false;

	protected abstract int getNumberOfThreads();

	public MultiThreadedModule(String name) {
		super(name);
	}

	public MultiThreadedModule(String name, int timeout) {
		super(name);
		threadJoinTimeout = timeout;
	}

	@Override
	public boolean updateConfiguration(Map<String, Object> configurationMap) {
		String fieldName = "threadJoinTimeout";
		if (configurationMap.containsKey(fieldName)) {
			try {
				threadJoinTimeout = (int) configurationMap.get(fieldName);
			} catch (ClassCastException exception) {
				logger.warn("Configuration for '{}' should be an integer.", fieldName, exception);
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean doReset() {
		// We do not care about the current existing threads.
		threads = new ArrayList<>();
		running = false;
		return true;
	}

	protected String nameThread(int threadID) {
		return this.getClass().getSimpleName() + "#" + this.hashCode() + "(" + getName() + ")" + "-" + threadID;
	}

	@Override
	protected boolean doStart() {
		logger.debug("Starting {} threads...", getNumberOfThreads());
		running = true;
		for (int threadID = 0; threadID < getNumberOfThreads(); threadID++) {
			final CountDownLatch threadStartedLatch = new CountDownLatch(1);
			final int finalThreadID = threadID;
			String threadName = nameThread(threadID);
			Thread thread = new Thread(() -> {
				threadStartedLatch.countDown();
				LoggerFactory.getLogger(this.getClass()).debug("Starting thread '{}'", threadName);
				MultiThreadedModule.this.main(finalThreadID);
			});
			threads.add(threadID, thread);
			thread.setName(threadName);
			thread.start();
			try {
				threadStartedLatch.await();
			} catch (InterruptedException exception) {
				logger.warn("Interrupted while waiting for thread '{}' to start.", thread.getName(), exception);
			}
		}
		return true;
	}

	protected abstract void main(int threadID);

	@Override
	protected boolean doStop() {
		Logger logger = LoggerFactory.getLogger(this.getClass());
		logger.debug("Stopping {} threads.", threads.size());
		running = false;
		// Join threads with interruption
		threads.stream().filter(Thread::isAlive).forEach(thread -> {
			thread.interrupt();
			try {
				thread.join(threadJoinTimeout);
			} catch (InterruptedException exception) {
				logger.warn("Interrupted while waiting for thread '{}' to stop.", thread.getName(), exception);
			}
		});
		boolean allThreadsStopped = true;
		for (Thread thread : threads) {
			if (thread.isAlive()) {
				logger.warn("Module's thread '{}' is still alive.", thread.getName());
				allThreadsStopped = false;
			}
		}
		if (allThreadsStopped) {
			logger.debug("All threads stopped.");
		}
		return allThreadsStopped;
	}

}
