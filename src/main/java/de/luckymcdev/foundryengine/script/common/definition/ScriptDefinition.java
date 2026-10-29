package de.luckymcdev.foundryengine.script.common.definition;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.bundle.common.info.BundleFiles;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ScriptDefinition(String bundleId, Path root, List<ScriptSource> sources) {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String EXTENSION = ".groovy";

	public ScriptDefinition {
		root = root.toAbsolutePath().normalize();
		sources = List.copyOf(sources);
	}

	/**
	 * Builds a definition from the files of a bundle.
	 */
	public static ScriptDefinition of(String bundleId, BundleFiles files, Set<ScriptEnv> activeEnvs) {
		Path root = realOrNormalized(files.scripts().root());
		Map<ScriptEnv, Path> envRoots = new EnumMap<>(ScriptEnv.class);
		envRoots.put(ScriptEnv.COMMON, realOrNormalized(files.scripts().common()));
		envRoots.put(ScriptEnv.CLIENT, realOrNormalized(files.scripts().client()));
		envRoots.put(ScriptEnv.SERVER, realOrNormalized(files.scripts().server()));

		List<ScriptSource> sources = new ArrayList<>();
		for (Path candidate : files.scripts().collection()) {
			if (!candidate.toString().endsWith(EXTENSION)) {
				continue;
			}
			Path real;
			try {
				real = candidate.toRealPath();
			} catch (IOException e) {
				LOGGER.warn("Skipping unreadable script '{}' in bundle '{}'", candidate, bundleId, e);
				continue;
			}
			if (!real.startsWith(root)) {
				LOGGER.warn("Skipping script '{}' in bundle '{}': resolves outside the script root ({})",
					candidate, bundleId, real);
				continue;
			}
			ScriptEnv env = envOf(real, envRoots);
			if (env == null) {
				LOGGER.debug("Script '{}' in bundle '{}' is outside the env directories, treated as library",
					real.getFileName(), bundleId);
				continue;
			}
			if (!activeEnvs.contains(env)) {
				continue;
			}
			String fileName = real.getFileName().toString();
			sources.add(new ScriptSource(fileName.substring(0, fileName.length() - EXTENSION.length()), real, env));
		}
		sources.sort(Comparator.comparing(ScriptSource::path));
		return new ScriptDefinition(bundleId, root, sources);
	}

	private static ScriptEnv envOf(Path real, Map<ScriptEnv, Path> envRoots) {
		for (Map.Entry<ScriptEnv, Path> entry : envRoots.entrySet()) {
			if (real.startsWith(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	private static Path realOrNormalized(Path path) {
		try {
			return path.toRealPath();
		} catch (IOException e) {
			return path.toAbsolutePath().normalize();
		}
	}

	public List<ScriptSource> sources(ScriptEnv env) {
		return sources.stream().filter(s -> s.env() == env).toList();
	}

	public Map<String, ScriptSource> byClassName() {
		Map<String, ScriptSource> map = new LinkedHashMap<>();
		for (ScriptSource source : sources) {
			map.putIfAbsent(source.className(), source);
		}
		return map;
	}
}