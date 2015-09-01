package hu.hgj.sceletus.module;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public abstract class MultiThreadedModule extends AbstractModuleAdapter {

	public static final long DEFAULT_THREAD_JOIN_TIMEOUT = 5000;
	public static final long DEFAULT_THREAD_RESTART_SLEEP = 5000;

	protected long threadJoinTimeoutMilli = DEFAULT_THREAD_JOIN_TIMEOUT;
	protected long threadRestartSleepMilli = DEFAULT_THREAD_RESTART_SLEEP;

	public long getThreadJoinTimeoutMilli() {
		return threadJoinTimeoutMilli;
	}

	public long getThreadRestartSleepMilli() {
		return threadRestartSleepMilli;
	}

	protected ArrayList<Thread> threads;

	protected boolean running = false;

	protected abstract int getNumberOfThreads();

	public MultiThreadedModule(String name) {
		super(name);
	}

	public MultiThreadedModule(String name, long threadJoinTimeoutMilli, long threadRestartSleepMilli) {
		super(name);
		this.threadJoinTimeoutMilli = threadJoinTimeoutMilli;
		this.threadRestartSleepMilli = threadRestartSleepMilli;
	}

	@Override
	public boolean updateConfiguration(Object configuration) {
		try {
			threadJoinTimeoutMilli = ((Number) JsonPath.read(configuration, "$.sceletus.threadJoinTimeout")).longValue();
		} catch (PathNotFoundException ignored) {
			// Ignore, stick to the default
		}
		try {
			threadRestartSleepMilli = ((Number) JsonPath.read(configuration, "$.sceletus.threadRestartSleep")).longValue();
		} catch (PathNotFoundException ignored) {
			// Ignore, stick to the default
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
				while (running) {
					logger.info("Starting thread '{}'", threadName);
					try {
						threadStartedLatch.countDown();
						MultiThreadedModule.this.main(finalThreadID);
					} catch (Throwable throwable) {
						logger.error("Exception caught while running thread '{}' of module.", threadName, throwable);
					}
					// Sleep before restarting thread
					if (running) {
						try {
							Thread.sleep(threadRestartSleepMilli);
						} catch (InterruptedException ignored) {
							// Ignore
						}
					}
				}
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
		boolean allThreadsStopped = true;
		// NOTE: Need to test if threads exists when called from a finalizer thread.
		if (threads != null) {
			logger.debug("Stopping {} threads.", threads.size());
			running = false;
			// Join threads with interruption
			threads.stream().filter(Thread::isAlive).forEach(thread -> {
				thread.interrupt();
				try {
					thread.join(threadJoinTimeoutMilli);
				} catch (InterruptedException exception) {
					logger.warn("Interrupted while waiting for thread '{}' to stop.", thread.getName(), exception);
				}
			});
			for (Thread thread : threads) {
				if (thread.isAlive()) {
					logger.warn("Module's thread '{}' is still alive.", thread.getName());
					allThreadsStopped = false;
				}
			}
			if (allThreadsStopped) {
				logger.debug("All threads stopped.");
			}
		}
		return allThreadsStopped;
	}

}
