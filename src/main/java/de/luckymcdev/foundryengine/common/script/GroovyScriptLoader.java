package de.luckymcdev.foundryengine.common.script;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import de.luckymcdev.foundryengine.common.priority.Priority;
import de.luckymcdev.foundryengine.config.StartupConfig;
import de.luckymcdev.foundryengine.server.Server;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Loads and compiles Groovy bundle scripts into entrypoints.
 */
public class GroovyScriptLoader {
	private static final Logger LOGGER = LogUtils.getLogger();

	public List<BundleEntrypoint> loadCommon(BundleFiles files, GroovyBundleScriptEngine engine, String bundleId) {
		return load(files, engine, BundleFiles.ScriptFiles::common, "common", bundleId);
	}

	public List<BundleEntrypoint> loadClient(BundleFiles files, GroovyBundleScriptEngine engine, String bundleId) {
		return load(files, engine, BundleFiles.ScriptFiles::client, "client", bundleId);
	}

	public List<BundleEntrypoint> loadServer(BundleFiles files, GroovyBundleScriptEngine engine, String bundleId) {
		return load(files, engine, BundleFiles.ScriptFiles::server, "server", bundleId);
	}

	private List<BundleEntrypoint> load(BundleFiles files, GroovyBundleScriptEngine engine,
	                                    Function<BundleFiles.ScriptFiles, Path> pathGetter, String envName, String bundleId) {
		List<BundleEntrypoint> entrypoints = new ArrayList<>();

		if (!StartupConfig.SCRIPTING_ENABLED.get()) {
			return entrypoints;
		}

		Path envPath = pathGetter.apply(files.scripts());

		List<Path> scriptPaths = files.scripts().collection().stream()
			.filter(p -> p.startsWith(envPath))
			.filter(p -> p.toString().endsWith(".groovy"))
			.toList();

		for (Path scriptPath : scriptPaths) {
			BundleEntrypoint entrypoint = null;
			String filename = scriptPath.getFileName().toString();

			try {
				entrypoint = loadScriptClass(scriptPath, files, engine);
			} catch (Exception e) {
				LOGGER.warn("Failed to compile {} script '{}' for bundle '{}'", envName, filename, bundleId, e);
				ModLoadingIssue issue = ModLoadingIssue.error(String.format(
					"Failed to compile %s script '%s' for bundle '%s': %s", envName, filename, bundleId, e.getMessage()));
				ModLoader.addLoadingIssue(issue);
				if (Server.getServer() != null) {
					String loc = e.getStackTrace().length > 0 ? " (" + e.getStackTrace()[0].getFileName() + ":" + e.getStackTrace()[0].getLineNumber() + ")" : "";
					Server.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§c[Script Error] Compile " + envName + " script '" + filename + "' for bundle '" + bundleId + "': " + e + loc), false);
				}
			}

			if (entrypoint != null) {
				entrypoints.add(entrypoint);
				try {
					entrypoint.onLoad();
				} catch (Exception e) {
					LOGGER.warn("Failed to run onLoad for {} script '{}' in bundle '{}'", envName, filename, bundleId, e);
					ModLoadingIssue issue = ModLoadingIssue.error(String.format(
						"Failed to run onLoad for %s script '%s' in bundle '%s': %s", envName, filename, bundleId, e.getMessage()));
					ModLoader.addLoadingIssue(issue);
					if (Server.getServer() != null) {
						String loc = e.getStackTrace().length > 0 ? " (" + e.getStackTrace()[0].getFileName() + ":" + e.getStackTrace()[0].getLineNumber() + ")" : "";
						Server.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§c[Script Error] onLoad " + envName + " script '" + filename + "' for bundle '" + bundleId + "': " + e + loc), false);
					}
				}
			}
		}

		entrypoints.sort(Priority.comparing(BundleEntrypoint::getPriority));
		return entrypoints;
	}

	private @Nullable BundleEntrypoint loadScriptClass(Path scriptPath, BundleFiles files,
	                                                   GroovyBundleScriptEngine engine) throws Exception {
		String scriptName = files.scripts().root().relativize(scriptPath).toString().replace('\\', '/');
		Class<?> scriptClass = engine.loadClass(scriptName);

		if (BundleEntrypoint.class.isAssignableFrom(scriptClass)) {
			return (BundleEntrypoint) scriptClass.getDeclaredConstructor().newInstance();
		}

		return null;
	}
}