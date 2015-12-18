package hu.hgj.sceletus.module;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public abstract class MultiThreadedModule extends AbstractModuleAdapter {

	public static final Duration DEFAULT_THREAD_JOIN_TIMEOUT = Duration.ofSeconds(5);
	public static final Duration DEFAULT_THREAD_RESTART_SLEEP_DURATION = Duration.ofSeconds(5);

	protected Duration threadJoinTimeout = DEFAULT_THREAD_JOIN_TIMEOUT;
	protected Duration threadRestartSleepDuration = DEFAULT_THREAD_RESTART_SLEEP_DURATION;

	protected int numberOfThreads = 0;

	public Duration getThreadJoinTimeout() {
		return threadJoinTimeout;
	}

	public Duration getThreadRestartSleepDuration() {
		return threadRestartSleepDuration;
	}

	protected ArrayList<Thread> threads;

	protected boolean running = false;

	protected int getNumberOfThreads() {
		return numberOfThreads > 0 ? numberOfThreads : 1;
	}

	public MultiThreadedModule(String name) {
		super(name);
	}

	public MultiThreadedModule(String name, Duration threadJoinTimeout, Duration threadRestartSleepDuration) {
		super(name);
		this.threadJoinTimeout = threadJoinTimeout;
		this.threadRestartSleepDuration = threadRestartSleepDuration;
	}

	@Override
	public boolean updateConfiguration(Object configuration) {
		try {
			numberOfThreads = JsonPath.read(configuration, "$.sceletus.numberOfThreads");
		} catch (PathNotFoundException ignored) {
			// Ignore, stick to the default
		}
		try {
			String threadJoinTimeoutString = JsonPath.read(configuration, "$.sceletus.threadJoinTimeout");
			try {
				threadJoinTimeout = Duration.parse(threadJoinTimeoutString);
			} catch (DateTimeParseException exception) {
				logger.error("Failed to parse '{}' as an ISO-8601 duration.", threadJoinTimeoutString, exception);
				return false;
			} catch (ArithmeticException exception) {
				logger.error("Configured duration is too big to fit in a long.", exception);
				return false;
			}
		} catch (PathNotFoundException ignored) {
			// Ignore, stick to the default
		}
		try {
			String threadRestartSleepDurationString = JsonPath.read(configuration, "$.sceletus.threadRestartSleepDuration");
			try {
				threadRestartSleepDuration = Duration.parse(threadRestartSleepDurationString);
			} catch (DateTimeParseException exception) {
				logger.error("Failed to parse '{}' as an ISO-8601 duration.", threadRestartSleepDurationString, exception);
				return false;
			} catch (ArithmeticException exception) {
				logger.error("Configured duration is too big to fit in a long.", exception);
				return false;
			}
		} catch (PathNotFoundException ignored) {
			// Ignore, stick to the default
		}
		logger.info("Updated configuration: numberOfThreads={}, threadJoinTimeout={}, threadRestartSleepDuration={}",
				getNumberOfThreads(),
				getThreadJoinTimeout().toString(),
				getThreadRestartSleepDuration().toString()
		);
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
							Thread.sleep(threadRestartSleepDuration.toMillis());
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
					thread.join(threadJoinTimeout.toMillis());
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
