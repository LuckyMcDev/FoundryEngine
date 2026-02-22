package io.github.luckymcdev.foundryengine.common.thread;

import io.github.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A Manager for all {@link EngineThread} Register via {@link RegisterEngineThreadEvent}.
 */
public class ThreadManager {
    private final GenericRegistry<Identifier, EngineThread> THREADS = new GenericRegistry<>();

    public ThreadManager() {
    }

    private static String defaultLabel(Identifier id) {
        if (id == null) {
            return "engine-thread";
        }
        String path = id.getPath();
        return (path == null || path.isBlank()) ? id.toString() : path;
    }

    /**
     * Registers a new Thread to the Manager.
     *
     * @param thread the Thread to register.
     * @return the registered and started Thread.
     */
    public EngineThread register(EngineThread thread) {
        Objects.requireNonNull(thread, "thread");
        THREADS.register(thread.getIdentifier(), thread);
        thread.startThread();
        return thread;
    }

    /**
     * Returns a {@link EngineThread} by its Identifier
     * @param id the Identifier of the Thread.
     * @return the {@link EngineThread}
     */
    public EngineThread get(Identifier id) {
        EngineThread thread = THREADS.get(id);
        if (thread == null) {
            throw new IllegalStateException("Thread not registered: " + id);
        }
        return thread;
    }

    public Optional<EngineThread> tryGet(Identifier id) {
        return THREADS.tryGet(id);
    }

    public boolean isRegistered(Identifier id) {
        return THREADS.contains(id);
    }

    public boolean isOnThread(Identifier id) {
        return get(id).isOnThisThread();
    }

    public void assertOnThread(Identifier id) {
        get(id).assertOnThisThread();
    }

    public boolean isOnThread(Thread thread) {
        Objects.requireNonNull(thread, "thread");
        if (thread instanceof EngineThread engineThread) {
            return engineThread.isOnThisThread();
        }
        return Thread.currentThread() == thread;
    }

    public void assertOnThread(Thread thread) {
        Objects.requireNonNull(thread, "thread");
        if (thread instanceof EngineThread engineThread) {
            engineThread.assertOnThisThread();
            return;
        }
        if (Thread.currentThread() != thread) {
            throw new WrongThreadException("Not on " + thread.getName() + " Thread.");
        }
    }

    /**
     * Executes an Action ({@link Runnable}) on a {@link EngineThread}.
     * @param thread the Thread to execute the Action on.
     * @param action the Action to execute.
     */
    public void execute(EngineThread thread, Runnable action) {
        Objects.requireNonNull(thread, "thread");
        thread.execute(action);
    }

    /**
     * Submits an Async action with return to run on a {@link EngineThread}
     * @param thread the Thread to execute the Action on.
     * @param action the Action to execute.
     * @return the computed Value.
     * @param <T> the Type
     */
    public <T> CompletableFuture<T> submit(EngineThread thread, Supplier<T> action) {
        Objects.requireNonNull(thread, "thread");
        return thread.submit(action);
    }

    /**
     * Shuts down a {@link EngineThread} by Identifier
     * @param id the Identifier of the {@link EngineThread} to shut down
     */
    public void shutdown(Identifier id) {
        EngineThread thread = THREADS.get(id);
        if (thread != null) {
            thread.shutdown();
        }
    }

    /**
     * Shuts down all {@link EngineThread} registered to the Manager.
     */
    public void shutdownAll() {
        THREADS.forEach(EngineThread::shutdown);
    }
}
