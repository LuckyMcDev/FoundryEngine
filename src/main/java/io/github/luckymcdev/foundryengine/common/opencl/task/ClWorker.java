package io.github.luckymcdev.foundryengine.common.opencl.task;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class ClWorker {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ExecutorService CL_THREAD = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "OpenCL-Worker");
        thread.setDaemon(true);
        return thread;
    });

    public static boolean isOnClThread() {
        return Thread.currentThread().getName().equals("OpenCL-Worker");
    }

    public static void assertOnClThread() {
        if (!isOnClThread()) {
            throw constructThreadException();
        }
    }

    private static IllegalStateException constructThreadException() {
        return new IllegalStateException("OpenCl called from wrong thread");
    }

    /**
     * Runs a compute task asynchronously.
     * @param action A lambda containing your OpenCL logic
     * @return A CompletableFuture that will contain the result
     */
    public static <T> CompletableFuture<T> submit(Supplier<T> action) {
        return CompletableFuture.supplyAsync(action, CL_THREAD)
                .exceptionally(ex -> {
                    LOGGER.error("OpenCL Worker error: ", ex);
                    return null;
                });
    }

    /**
     * Shuts down the worker thread.
     */
    public static void shutdown() {
        CL_THREAD.shutdown();
    }
}