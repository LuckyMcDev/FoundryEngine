package io.github.luckymcdev.foundryengine.common.thread;

import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * An Extension to a {@link Thread} which adds some extra Functionality.
 */
public class EngineThread extends Thread {
    private final Identifier id;
    private final String label;
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean accepting = new AtomicBoolean(true);
    private final AtomicBoolean started = new AtomicBoolean(false);

    /**
     * Create a new Engine Thread.
     *
     * @param id     the Identifier of the Thread
     * @param label  the label to be displayed in the log.
     * @param daemon if it is in daemon mode.
     */
    public EngineThread(Identifier id, String label, boolean daemon) {
        super(resolveThreadName(id, label));
        this.id = id;
        this.label = resolveLabel(id, label);
        setDaemon(daemon);
    }

    private static String resolveThreadName(Identifier id, String label) {
        return resolveLabel(id, label);
    }

    private static String resolveLabel(Identifier id, String label) {
        if (label != null && !label.isBlank()) {
            return label;
        }
        if (id == null) {
            return "engine-thread";
        }
        String path = id.getPath();
        return (path == null || path.isBlank()) ? id.toString() : path;
    }

    public Identifier getIdentifier() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void startThread() {
        if (started.compareAndSet(false, true)) {
            start();
        }
    }

    /**
     * Asserts that the current Thread ({@link Thread#currentThread()}) is this Thread.
     */
    public void assertOnThisThread() {
        if (Thread.currentThread() != this) {
            throw new WrongThreadException("Not on " + this.id + " Thread.");
        }
    }

    public boolean isOnThisThread() {
        return Thread.currentThread() == this;
    }

    public void execute(Runnable action) {
        Objects.requireNonNull(action, "action");
        ensureAccepting();
        queue.add(action);
    }

    public <T> CompletableFuture<T> submit(Supplier<T> action) {
        Objects.requireNonNull(action, "action");
        ensureAccepting();
        CompletableFuture<T> future = new CompletableFuture<>();
        queue.add(() -> {
            try {
                future.complete(action.get());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public void shutdown() {
        accepting.set(false);
        running.set(false);
        interrupt();
    }

    public void shutdownNow() {
        accepting.set(false);
        running.set(false);
        queue.clear();
        interrupt();
    }

    @Override
    public void run() {
        while (running.get() || !queue.isEmpty()) {
            try {
                Runnable task = queue.poll(250, TimeUnit.MILLISECONDS);
                if (task != null) {
                    task.run();
                }
            } catch (InterruptedException ignored) {
                // Allow thread to exit when shutdown is requested.
            }
        }
    }

    private void ensureAccepting() {
        if (!accepting.get()) {
            throw new RejectedExecutionException("Thread " + id + " is shutting down");
        }
        startThread();
    }
}
