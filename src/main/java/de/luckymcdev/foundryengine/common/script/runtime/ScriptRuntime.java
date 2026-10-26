package de.luckymcdev.foundryengine.common.script.runtime;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.priority.Priority;
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint;
import de.luckymcdev.foundryengine.common.script.ScriptErrorReporter;
import de.luckymcdev.foundryengine.common.script.ScriptTimeout;
import de.luckymcdev.foundryengine.common.script.definition.ScriptDefinition;
import de.luckymcdev.foundryengine.common.script.definition.ScriptEnv;
import de.luckymcdev.foundryengine.common.script.definition.ScriptSource;
import de.luckymcdev.foundryengine.common.script.sandbox.SandboxPolicy;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * What happens to one bundle while the engine runs. Owns the compiled classes,
 * the live entrypoint instances, the status and the failures. Nothing here is persisted.
 * <p>
 * Entrypoint construction, onLoad and onUnload all run under the policy's timeout.
 * Loading an environment twice is a no-op; use {@link #unload()} and recompile to reload.
 */
public class ScriptRuntime {

	private static final Logger LOGGER = LogUtils.getLogger();
	private final ScriptDefinition definition;
	private final SandboxPolicy policy;
	private final ScriptErrorReporter reporter;
	private final List<ScriptFailure> failures = new CopyOnWriteArrayList<>();
	private final Map<ScriptEnv, List<LoadedEntrypoint>> loaded = new EnumMap<>(ScriptEnv.class);
	private volatile ScriptStatus status = ScriptStatus.DEFINED;
	private volatile CompiledBundle compiled;
	private int reported;

	public ScriptRuntime(ScriptDefinition definition, SandboxPolicy policy, ScriptErrorReporter reporter) {
		this.definition = definition;
		this.policy = policy;
		this.reporter = reporter;
	}

	private static List<BundleEntrypoint> entrypoints(List<LoadedEntrypoint> list) {
		return list.stream().map(LoadedEntrypoint::entrypoint).toList();
	}

	/**
	 * Script errors (failed assert, NoClassDefFoundError, ...) must not take down the loader.
	 */
	private static RuntimeException wrap(Error error) {
		if (error instanceof VirtualMachineError vme) {
			throw vme;
		}
		return new RuntimeException(error);
	}

	public ScriptDefinition definition() {
		return definition;
	}

	public ScriptStatus status() {
		return status;
	}

	public List<ScriptFailure> failures() {
		return Collections.unmodifiableList(failures);
	}

	public synchronized Set<ScriptEnv> loadedEnvs() {
		return loaded.isEmpty() ? EnumSet.noneOf(ScriptEnv.class) : EnumSet.copyOf(loaded.keySet());
	}

	public synchronized void clearFailures() {
		failures.clear();
		reported = 0;
	}

	public void markCompiling() {
		status = ScriptStatus.COMPILING;
	}

	public void attach(CompiledBundle result) {
		this.compiled = result;
		failures.addAll(result.failures());
		status = result.classes().isEmpty() && !result.failures().isEmpty()
			? ScriptStatus.FAILED
			: ScriptStatus.COMPILED;
	}

	public void recordCompileCrash(Throwable cause) {
		failures.add(new ScriptFailure(null, "compile", cause));
		status = ScriptStatus.FAILED;
	}

	public synchronized List<BundleEntrypoint> load(ScriptEnv env, Predicate<ScriptSource> enabled) {
		reportPendingFailures();

		List<LoadedEntrypoint> existing = loaded.get(env);
		if (existing != null) {
			return entrypoints(existing);
		}
		CompiledBundle result = compiled;
		if (result == null) {
			return List.of();
		}

		String bundleId = definition.bundleId();
		Map<String, ScriptSource> byClass = definition.byClassName();
		List<LoadedEntrypoint> created = new ArrayList<>();

		for (Map.Entry<String, Class<?>> entry : result.classes().entrySet()) {
			String className = entry.getKey();
			Class<?> clazz = entry.getValue();
			if (className.indexOf('$') >= 0 || !BundleEntrypoint.class.isAssignableFrom(clazz)) {
				continue;
			}
			ScriptSource source = byClass.get(className.substring(className.lastIndexOf('.') + 1));
			if (source == null) {
				LOGGER.debug("Skipping entrypoint class '{}' (no matching source file in bundle '{}')",
					className, bundleId);
				continue;
			}
			if (source.env() != env || !enabled.test(source)) {
				continue;
			}
			try {
				BundleEntrypoint instance = call(
					() -> (BundleEntrypoint) clazz.getDeclaredConstructor().newInstance(),
					label(source, "instantiate"));
				created.add(new LoadedEntrypoint(source, instance));
			} catch (Exception e) {
				fail(source, "instantiate", e);
			}
		}

		List<LoadedEntrypoint> ok = new ArrayList<>();
		for (LoadedEntrypoint candidate : created) {
			try {
				run(candidate.entrypoint()::onLoad, label(candidate.source(), "onLoad"));
				ok.add(candidate);
			} catch (Exception e) {
				fail(candidate.source(), "onLoad", e);
			}
		}

		Comparator<BundleEntrypoint> byPriority = Priority.comparing(BundleEntrypoint::getPriority);
		ok.sort((a, b) -> byPriority.compare(a.entrypoint(), b.entrypoint()));
		loaded.put(env, ok);
		if (!ok.isEmpty()) {
			status = ScriptStatus.LOADED;
		}

		int failed = created.size() - ok.size();
		if (failed > 0) {
			LOGGER.warn("Bundle '{}' {} scripts: {} loaded, {} failed", bundleId, env.getName(), ok.size(), failed);
		} else {
			LOGGER.info("Bundle '{}' {} scripts: {} loaded", bundleId, env.getName(), ok.size());
		}
		return entrypoints(ok);
	}

	/**
	 * Calls onUnload (reverse load order), then drops the classes and closes the class loader.
	 */
	public synchronized void unload() {
		for (List<LoadedEntrypoint> list : loaded.values()) {
			List<LoadedEntrypoint> reversed = new ArrayList<>(list);
			Collections.reverse(reversed);
			for (LoadedEntrypoint entry : reversed) {
				try {
					run(entry.entrypoint()::onUnload, label(entry.source(), "onUnload"));
				} catch (Exception e) {
					fail(entry.source(), "onUnload", e);
				}
			}
		}
		loaded.clear();
		CompiledBundle old = compiled;
		compiled = null;
		if (old != null) {
			old.close();
		}
		status = ScriptStatus.UNLOADED;
	}

	private void fail(ScriptSource source, String stage, Throwable cause) {
		ScriptFailure failure = new ScriptFailure(source.className(), stage, cause);
		failures.add(failure);
		reporter.report(definition.bundleId(), failure);
		reported = failures.size();
	}

	private void reportPendingFailures() {
		for (int i = reported; i < failures.size(); i++) {
			reporter.report(definition.bundleId(), failures.get(i));
		}
		reported = failures.size();
	}

	private String label(ScriptSource source, String stage) {
		return definition.bundleId() + "/" + source.env().getName() + "/" + source.className() + "/" + stage;
	}

	private void run(Runnable runnable, String label) throws Exception {
		if (policy.hasTimeout()) {
			ScriptTimeout.run(runnable, policy.timeoutSeconds(), label);
			return;
		}
		try {
			runnable.run();
		} catch (Error error) {
			throw wrap(error);
		}
	}

	private <T> T call(Callable<T> callable, String label) throws Exception {
		if (policy.hasTimeout()) {
			return ScriptTimeout.call(callable, policy.timeoutSeconds(), label);
		}
		try {
			return callable.call();
		} catch (Error error) {
			throw wrap(error);
		}
	}

	private record LoadedEntrypoint(ScriptSource source, BundleEntrypoint entrypoint) {
	}
}