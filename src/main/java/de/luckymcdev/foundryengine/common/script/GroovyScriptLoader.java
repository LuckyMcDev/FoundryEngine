package de.luckymcdev.foundryengine.common.script;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import de.luckymcdev.foundryengine.common.priority.Priority;
import de.luckymcdev.foundryengine.config.StartupConfig;
import de.luckymcdev.foundryengine.server.Server;
import groovy.lang.GroovyCodeSource;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.tools.GroovyClass;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class GroovyScriptLoader {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final Map<String, BundleCompileResult> bundleCache = new ConcurrentHashMap<>();

	private static Map<String, Path> buildClassNameMap(List<Path> scriptPaths) {
		Map<String, Path> map = new HashMap<>();
		for (Path p : scriptPaths) {
			String fn = p.getFileName().toString();
			if (fn.endsWith(".groovy")) {
				map.put(fn.substring(0, fn.length() - ".groovy".length()), p);
			}
		}
		return map;
	}

	private static void reportOnLoadFailure(EnvType envType, String bundleId, String message) {
		LOGGER.warn("Failed to run onLoad for {} script in bundle '{}': {}",
			envType.getName(), bundleId, message);
		ModLoadingIssue issue = ModLoadingIssue.error(String.format(
			"Failed to run onLoad for %s script in bundle '%s': %s",
			envType.getName(), bundleId, message));
		ModLoader.addLoadingIssue(issue);
		if (Server.getServer() != null) {
			Server.getServer().getPlayerList().broadcastSystemMessage(
				Component.literal("§c[Script Error] onLoad " + envType.getName() + " script in bundle '" + bundleId + "': " + message),
				false);
		}
	}

	public List<BundleEntrypoint> loadCommon(BundleFiles files, String bundleId) {
		return loadEnv(files, bundleId, EnvType.COMMON, BundleCompileResult::common);
	}

	public List<BundleEntrypoint> loadClient(BundleFiles files, String bundleId) {
		return loadEnv(files, bundleId, EnvType.CLIENT, BundleCompileResult::client);
	}

	public List<BundleEntrypoint> loadServer(BundleFiles files, String bundleId) {
		return loadEnv(files, bundleId, EnvType.SERVER, BundleCompileResult::server);
	}

	private List<BundleEntrypoint> loadEnv(BundleFiles files, String bundleId, EnvType envType,
	                                       Function<BundleCompileResult, List<BundleEntrypoint>> resultExtractor) {
		if (!StartupConfig.SCRIPTING_ENABLED.get()) {
			return List.of();
		}

		BundleCompileResult result = bundleCache.computeIfAbsent(bundleId, id -> {
			LOGGER.info("Compiling all scripts for bundle '{}'", bundleId);
			return compileBundle(files, bundleId);
		});

		List<BundleEntrypoint> entrypoints = resultExtractor.apply(result);
		List<BundleEntrypoint> loaded = new ArrayList<>();

		for (BundleEntrypoint entrypoint : entrypoints) {
			try {
				ScriptTimeout.run(entrypoint::onLoad,
					StartupConfig.SCRIPT_TIMEOUT_SECONDS.get(), bundleId + "/" + envType.getName() + "/onLoad");
				loaded.add(entrypoint);
			} catch (ScriptTimeoutException ste) {
				LOGGER.error("onLoad timed out for {} script in bundle '{}' after {}s, skipping",
					envType.getName(), bundleId, StartupConfig.SCRIPT_TIMEOUT_SECONDS.get());
				reportOnLoadFailure(envType, bundleId,
					"timed out after " + StartupConfig.SCRIPT_TIMEOUT_SECONDS.get() + " seconds (entrypoint skipped)");
			} catch (Exception e) {
				reportOnLoadFailure(envType, bundleId, e.getMessage());
			}
		}

		loaded.sort(Priority.comparing(BundleEntrypoint::getPriority));

		int total = entrypoints.size();
		int succeeded = loaded.size();
		int failed = total - succeeded;
		if (failed > 0) {
			LOGGER.warn("Bundle '{}' {} scripts: {} loaded, {} failed (of {})",
				bundleId, envType.getName(), succeeded, failed, total);
		} else {
			LOGGER.info("Bundle '{}' {} scripts: {} loaded",
				bundleId, envType.getName(), succeeded);
		}

		return loaded;
	}

	private BundleCompileResult compileBundle(BundleFiles files, String bundleId) {
		Path scriptRoot = files.scripts().root();
		Path commonPath = files.scripts().common();
		Path clientPath = files.scripts().client();
		Path serverPath = files.scripts().server();

		List<Path> allScriptPaths = files.scripts().collection().stream()
			.filter(p -> p.toString().endsWith(".groovy"))
			.toList();

		if (allScriptPaths.isEmpty()) {
			return new BundleCompileResult(List.of(), List.of(), List.of());
		}

		Map<String, Path> classNameToPath = buildClassNameMap(allScriptPaths);

		try {
			List<GroovyClass> compiledClasses = Common.getScriptShell()
				.compileBundle(allScriptPaths, scriptRoot);
			return defineAndSplit(compiledClasses, classNameToPath,
				commonPath, clientPath, serverPath, bundleId);
		} catch (MultipleCompilationErrorsException mce) {
			LOGGER.warn("Batch compile failed for bundle '{}', falling back to per-file compilation", bundleId);
			for (var msg : mce.getErrorCollector().getErrors()) {
				if (msg instanceof SyntaxErrorMessage sem) {
					LOGGER.warn("  {}", sem.getCause().getMessage());
				} else {
					LOGGER.warn("  {}", msg);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Batch compile failed unexpectedly for bundle '{}'", bundleId, e);
		}

		return fallbackCompile(allScriptPaths, files, bundleId,
			commonPath, clientPath, serverPath);
	}

	private BundleCompileResult defineAndSplit(List<GroovyClass> compiledClasses,
	                                           Map<String, Path> classNameToPath,
	                                           Path commonPath, Path clientPath, Path serverPath,
	                                           String bundleId) {
		ScriptShell shell = Common.getScriptShell();

		Map<String, Class<?>> definedClasses = new HashMap<>();
		for (GroovyClass gc : compiledClasses) {
			try {
				Class<?> clazz = shell.defineClass(gc.getName(), gc.getBytes());
				definedClasses.put(gc.getName(), clazz);
			} catch (Exception e) {
				LOGGER.warn("Failed to define class '{}' in bundle '{}': {}",
					gc.getName(), bundleId, e.getMessage());
			}
		}

		List<BundleEntrypoint> common = new ArrayList<>();
		List<BundleEntrypoint> client = new ArrayList<>();
		List<BundleEntrypoint> server = new ArrayList<>();

		for (Map.Entry<String, Class<?>> entry : definedClasses.entrySet()) {
			String className = entry.getKey();
			Class<?> clazz = entry.getValue();

			if (className.contains("$")) {
				continue;
			}
			if (!BundleEntrypoint.class.isAssignableFrom(clazz)) {
				continue;
			}

			String simpleName = className.contains(".")
				? className.substring(className.lastIndexOf('.') + 1)
				: className;

			Path sourcePath = classNameToPath.get(simpleName);
			if (sourcePath == null) {
				LOGGER.debug("Skipping entrypoint class '{}' (no matching source file in bundle '{}')",
					className, bundleId);
				continue;
			}

			try {
				BundleEntrypoint entrypoint = (BundleEntrypoint) clazz.getDeclaredConstructor().newInstance();

				if (sourcePath.startsWith(commonPath)) {
					common.add(entrypoint);
				} else if (sourcePath.startsWith(clientPath)) {
					client.add(entrypoint);
				} else if (sourcePath.startsWith(serverPath)) {
					server.add(entrypoint);
				} else {
					LOGGER.warn("Script class '{}' in bundle '{}' is outside known env directories",
						className, bundleId);
				}
			} catch (Exception e) {
				String filename = sourcePath.getFileName().toString();
				LOGGER.warn("Failed to instantiate entrypoint '{}' in bundle '{}': {}",
					filename, bundleId, e.getMessage());
			}
		}

		return new BundleCompileResult(common, client, server);
	}

	private BundleCompileResult fallbackCompile(List<Path> scriptPaths, BundleFiles files, String bundleId,
	                                            Path commonPath, Path clientPath, Path serverPath) {
		ScriptShell shell = Common.getScriptShell();
		List<BundleEntrypoint> common = new ArrayList<>();
		List<BundleEntrypoint> client = new ArrayList<>();
		List<BundleEntrypoint> server = new ArrayList<>();

		for (Path scriptPath : scriptPaths) {
			String filename = scriptPath.getFileName().toString();
			BundleEntrypoint entrypoint = null;

			try {
				Class<?> scriptClass = shell.parseClass(new GroovyCodeSource(scriptPath.toUri().toURL()));
				if (BundleEntrypoint.class.isAssignableFrom(scriptClass)) {
					entrypoint = (BundleEntrypoint) scriptClass.getDeclaredConstructor().newInstance();
				}
			} catch (MultipleCompilationErrorsException mce) {
				for (var msg : mce.getErrorCollector().getErrors()) {
					if (msg instanceof SyntaxErrorMessage sem) {
						LOGGER.warn("Script error in '{}' ({},{}): {}",
							filename, sem.getCause().getLine(), sem.getCause().getStartColumn(), sem.getCause().getMessage());
					} else {
						LOGGER.warn("Script error in '{}': {}", filename, msg);
					}
				}
				LOGGER.warn("Failed to compile script '{}' for bundle '{}'", filename, bundleId, mce);
				ModLoadingIssue issue = ModLoadingIssue.error(String.format(
					"Failed to compile script '%s' for bundle '%s': %s", filename, bundleId, mce.getMessage()));
				ModLoader.addLoadingIssue(issue);
				if (Server.getServer() != null) {
					String loc = mce.getStackTrace().length > 0
						? " (" + mce.getStackTrace()[0].getFileName() + ":" + mce.getStackTrace()[0].getLineNumber() + ")"
						: "";
					Server.getServer().getPlayerList().broadcastSystemMessage(
						Component.literal("§c[Script Error] Compile script '" + filename + "' for bundle '" + bundleId + "': " + mce + loc),
						false);
				}
			} catch (Exception e) {
				LOGGER.warn("Failed to compile script '{}' for bundle '{}'", filename, bundleId, e);
				ModLoadingIssue issue = ModLoadingIssue.error(String.format(
					"Failed to compile script '%s' for bundle '%s': %s", filename, bundleId, e.getMessage()));
				ModLoader.addLoadingIssue(issue);
				if (Server.getServer() != null) {
					String loc = e.getStackTrace().length > 0
						? " (" + e.getStackTrace()[0].getFileName() + ":" + e.getStackTrace()[0].getLineNumber() + ")"
						: "";
					Server.getServer().getPlayerList().broadcastSystemMessage(
						Component.literal("§c[Script Error] Compile script '" + filename + "' for bundle '" + bundleId + "': " + e + loc),
						false);
				}
			}

			if (entrypoint != null) {
				if (scriptPath.startsWith(commonPath)) {
					common.add(entrypoint);
				} else if (scriptPath.startsWith(clientPath)) {
					client.add(entrypoint);
				} else if (scriptPath.startsWith(serverPath)) {
					server.add(entrypoint);
				}
			}
		}

		return new BundleCompileResult(common, client, server);
	}

	public void invalidateCache() {
		bundleCache.clear();
	}

	public enum EnvType {
		CLIENT("client"),
		SERVER("server"),
		COMMON("common");

		private final String name;

		EnvType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private record BundleCompileResult(
		List<BundleEntrypoint> common,
		List<BundleEntrypoint> client,
		List<BundleEntrypoint> server
	) {
	}
}
