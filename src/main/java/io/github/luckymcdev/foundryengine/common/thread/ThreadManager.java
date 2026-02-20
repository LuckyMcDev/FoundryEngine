package io.github.luckymcdev.foundryengine.common.thread;

import io.github.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ThreadManager {
    private final GenericRegistry<Identifier, EngineThread> THREADS = new GenericRegistry<>();

    public EngineThread register(EngineThread thread) {
        Objects.requireNonNull(thread, "thread");
        THREADS.register(thread.getIdentifier(), thread);
        thread.startThread();
        return thread;
    }

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

    public EngineThread getOrRegister(Identifier id, String label, boolean daemon) {
        EngineThread existing = THREADS.get(id);
        if (existing != null) {
            return existing;
        }
        EngineThread created = new EngineThread(id, label, daemon);
        THREADS.register(id, created);
        created.startThread();
        return created;
    }

    public EngineThread getOrRegister(Identifier id, boolean daemon) {
        return getOrRegister(id, defaultLabel(id), daemon);
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

    public void execute(Identifier id, Runnable action) {
        getOrRegister(id, defaultLabel(id), true).execute(action);
    }

    public <T> CompletableFuture<T> submit(Identifier id, Supplier<T> action) {
        return getOrRegister(id, defaultLabel(id), true).submit(action);
    }

    public void execute(EngineThread thread, Runnable action) {
        Objects.requireNonNull(thread, "thread");
        thread.execute(action);
    }

    public <T> CompletableFuture<T> submit(EngineThread thread, Supplier<T> action) {
        Objects.requireNonNull(thread, "thread");
        return thread.submit(action);
    }

    public void shutdown(Identifier id) {
        EngineThread thread = THREADS.get(id);
        if (thread != null) {
            thread.shutdown();
        }
    }

    public void shutdownAll() {
        THREADS.forEach(EngineThread::shutdown);
    }

    private static String defaultLabel(Identifier id) {
        if (id == null) {
            return "engine-thread";
        }
        String path = id.getPath();
        return (path == null || path.isBlank()) ? id.toString() : path;
    }
}
