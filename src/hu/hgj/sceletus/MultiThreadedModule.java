package hu.hgj.sceletus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public abstract class MultiThreadedModule extends AbstractModule {

	private ArrayList<Thread> threads;

	protected boolean running = false;

	protected abstract int getNumberOfThreads();

	@Override
	protected boolean doReset() {
		// We do not care about the current existing threads.
		threads = new ArrayList<>();
		running = false;
		return true;
	}

	protected String nameThread(int threadID) {
		return this.getClass().getSimpleName() + "#" + this.hashCode() + "-" + threadID;
	}

	@Override
	protected boolean doStart() {
		Logger logger = LoggerFactory.getLogger(this.getClass());
		logger.debug("Starting {} threads...", getNumberOfThreads());
		running = true;
		for (int threadID = 0; threadID < getNumberOfThreads(); threadID++) {
			Object threadLock = new Object();
			final boolean[] threadRunning = {false};
			final int finalThreadID = threadID;
			String threadName = nameThread(threadID);
			Thread thread = new Thread(() -> {
				synchronized (threadLock) {
					threadRunning[0] = true;
					threadLock.notify();
				}
				LoggerFactory.getLogger(this.getClass()).debug("Starting thread '{}'", threadName);
				MultiThreadedModule.this.main(finalThreadID);
			});
			threads.add(threadID, thread);
			thread.setName(threadName);
			thread.start();
			synchronized (threadLock) {
				try {
					while (!threadRunning[0]) {
						threadLock.wait();
					}
				} catch (InterruptedException exception) {
					logger.warn("Interrupted while waiting for thread '{}' to start.", thread.getName(), exception);
				}
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
		for (Thread thread : threads) {
			if (thread.isAlive()) {
				thread.interrupt();
				try {
					// TODO: Make timeout configurable
					thread.join(2000);
				} catch (InterruptedException exception) {
					logger.warn("Interrupted while waiting for thread '{}' to stop.", thread.getName(), exception);
				}
			}
		}
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
