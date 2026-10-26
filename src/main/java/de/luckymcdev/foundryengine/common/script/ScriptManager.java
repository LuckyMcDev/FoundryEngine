package de.luckymcdev.foundryengine.common.script;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import de.luckymcdev.foundryengine.common.script.definition.ScriptDefinition;
import de.luckymcdev.foundryengine.common.script.definition.ScriptEnv;
import de.luckymcdev.foundryengine.common.script.runtime.ScriptCompiler;
import de.luckymcdev.foundryengine.common.script.runtime.ScriptRuntime;
import de.luckymcdev.foundryengine.common.script.sandbox.SandboxPolicy;
import de.luckymcdev.foundryengine.config.StartupConfig;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MANAGER: creates and owns everything else (definitions, runtimes, editor state, compiler, thread pools).
 * <p>
 * Compilation is parallel across bundles on a bounded daemon pool. To get that parallelism call
 * {@link #prepareAll} once with all bundles before the first {@link #load}; a lone {@link #load}
 * compiles just its bundle. Entry code (constructors, onLoad, onUnload) runs on the calling
 * thread's behalf under the sandbox timeout, in load order.
 */
public final class ScriptManager {

	private static final Logger LOGGER = LogUtils.getLogger();
	private final Set<ScriptEnv> activeEnvs;
	private final ScriptCompiler compiler;
	private final ScriptErrorReporter reporter = new ScriptErrorReporter();
	private final ExecutorService compilePool;
	private final ExecutorService lifecycleExecutor;
	private final Map<String, Entry> entries = new ConcurrentHashMap<>();

	/**
	 * @param activeEnvs environments this JVM runs: all three on a client, COMMON and SERVER on a
	 *                   dedicated server. Scripts of other environments are never compiled.
	 */
	public ScriptManager(Set<ScriptEnv> activeEnvs) {
		this.activeEnvs = Set.copyOf(activeEnvs);
		this.compiler = new ScriptCompiler();
		int threads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
		this.compilePool = Executors.newFixedThreadPool(threads, daemonFactory("FoundryEngine-ScriptCompile-"));
		this.lifecycleExecutor = Executors.newSingleThreadExecutor(daemonFactory("FoundryEngine-ScriptLifecycle-"));
	}

	private static ThreadFactory daemonFactory(String prefix) {
		AtomicInteger id = new AtomicInteger();
		return runnable -> {
			Thread thread = new Thread(runnable, prefix + id.incrementAndGet());
			thread.setDaemon(true);
			return thread;
		};
	}

	/**
	 * Starts compiling a bundle if it is not known yet. The future completes when it is compiled.
	 */
	public CompletableFuture<Void> prepare(String bundleId, BundleFiles files) {
		return entries.computeIfAbsent(bundleId, id -> schedule(id, files)).ready();
	}

	/**
	 * Starts compiling all given bundles in parallel.
	 */
	public CompletableFuture<Void> prepareAll(Map<String, BundleFiles> bundles) {
		CompletableFuture<?>[] futures = bundles.entrySet().stream()
			.map(e -> prepare(e.getKey(), e.getValue()))
			.toArray(CompletableFuture[]::new);
		return CompletableFuture.allOf(futures);
	}

	private Entry schedule(String bundleId, BundleFiles files) {
		ScriptDefinition definition = ScriptDefinition.of(bundleId, files, activeEnvs);
		SandboxPolicy policy = SandboxPolicy.current();
		ScriptRuntime runtime = new ScriptRuntime(definition, policy, reporter);
		runtime.markCompiling();
		CompletableFuture<Void> ready = CompletableFuture.runAsync(() -> {
			try {
				runtime.attach(compiler.compile(definition, policy));
			} catch (Throwable t) {
				LOGGER.error("Compiling bundle '{}' crashed", bundleId, t);
				runtime.recordCompileCrash(t);
			}
		}, compilePool);
		return new Entry(files, runtime, ready);
	}

	/**
	 * Waits for the bundle's compilation, then loads the entrypoints of one environment.
	 */
	public List<BundleEntrypoint> load(String bundleId, BundleFiles files, ScriptEnv env) {
		if (!StartupConfig.SCRIPTING_ENABLED.get() || !activeEnvs.contains(env)) {
			return List.of();
		}
		Entry entry = entries.computeIfAbsent(bundleId, id -> schedule(id, files));
		entry.ready().join();
		return entry.runtime().load(env, source -> true);
	}

	/**
	 * Unloads, recompiles and reloads the environments that were loaded before.
	 */
	public CompletableFuture<Void> reload(String bundleId) {
		Entry old = entries.get(bundleId);
		if (old == null) {
			return CompletableFuture.completedFuture(null);
		}
		return CompletableFuture.runAsync(() -> {
			Set<ScriptEnv> envs = old.runtime().loadedEnvs();
			old.runtime().unload();
			entries.remove(bundleId, old);
			Entry fresh = entries.computeIfAbsent(bundleId, id -> schedule(id, old.files()));
			fresh.ready().join();
			for (ScriptEnv env : envs) {
				fresh.runtime().load(env, s -> true);
			}
		}, lifecycleExecutor);
	}

	public CompletableFuture<Void> setBundleEnabled(String bundleId, boolean enabled) {
		Entry entry = entries.get(bundleId);
		if (entry == null) {
			return CompletableFuture.completedFuture(null);
		}
		return enabled
			? reload(bundleId)
			: CompletableFuture.runAsync(() -> entry.runtime().unload(), lifecycleExecutor);
	}

	public CompletableFuture<Void> setScriptEnabled(String bundleId, String script, boolean enabled) {
		return reload(bundleId);
	}

	/**
	 * Unloads a single bundle: fires {@code onUnload} on its entrypoints and closes its class
	 * loader. The bundle is then forgotten so the next {@link #load} recompiles.
	 */
	public void unloadBundle(String bundleId) {
		Entry entry = entries.remove(bundleId);
		if (entry != null) {
			entry.ready().join();
			entry.runtime().unload();
		}
	}

	/**
	 * Unloads every bundle (calling onUnload) and forgets it. Next load recompiles.
	 */
	public void unloadAll() {
		for (String bundleId : List.copyOf(entries.keySet())) {
			Entry entry = entries.remove(bundleId);
			if (entry != null) {
				entry.ready().join();
				entry.runtime().unload();
			}
		}
	}

	public void shutdown() {
		unloadAll();
		compilePool.shutdownNow();
		lifecycleExecutor.shutdownNow();
	}

	public Collection<ScriptRuntime> runtimes() {
		return entries.values().stream().map(Entry::runtime).toList();
	}

	public Optional<ScriptRuntime> runtime(String bundleId) {
		return Optional.ofNullable(entries.get(bundleId)).map(Entry::runtime);
	}

	public Map<String, List<Throwable>> drainErrors() {
		return reporter.drain();
	}

	private record Entry(BundleFiles files, ScriptRuntime runtime, CompletableFuture<Void> ready) {
	}
}