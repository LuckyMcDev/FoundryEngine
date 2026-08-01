package de.luckymcdev.foundryengine.common.script;

import org.jspecify.annotations.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs script code on a daemon worker thread with a configurable timeout so
 * that a hung entrypoint cannot block the mod loader forever.
 */
public final class ScriptTimeout {

	private static final AtomicInteger THREAD_ID = new AtomicInteger();

	private ScriptTimeout() {
	}

	public static <T> @Nullable T call(Callable<T> callable, long timeoutSeconds, String description) throws Exception {
		ExecutorService executor = Executors.newSingleThreadExecutor(newDaemonThreadFactory(description));
		FutureTask<T> future = new FutureTask<>(callable);
		executor.execute(future);
		try {
			return await(future, timeoutSeconds, description);
		} finally {
			executor.shutdownNow();
		}
	}

	public static void run(Runnable runnable, long timeoutSeconds, String description) throws Exception {
		ExecutorService executor = Executors.newSingleThreadExecutor(newDaemonThreadFactory(description));
		CompletableFuture<Void> future = CompletableFuture.runAsync(runnable, executor);
		try {
			await(future, timeoutSeconds, description);
		} finally {
			executor.shutdownNow();
		}
	}

	private static <T> T await(Future<T> future, long timeoutSeconds, String description) throws Exception {
		try {
			return future.get(timeoutSeconds, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			future.cancel(true);
			throw new ScriptTimeoutException(description, timeoutSeconds);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause() != null ? e.getCause() : e;
			if (cause instanceof Exception ex) {
				throw ex;
			}
			throw new RuntimeException(cause);
		}
	}

	private static ThreadFactory newDaemonThreadFactory(String description) {
		return runnable -> {
			Thread thread = new Thread(runnable,
				"FoundryEngine-Script-" + description + "-" + THREAD_ID.incrementAndGet());
			thread.setDaemon(true);
			return thread;
		};
	}
}
