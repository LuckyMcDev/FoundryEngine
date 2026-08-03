package de.luckymcdev.foundryengine.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Bumps the patch version and writes the new version to the version catalog
 * ({@code gradle/libs.versions.toml}). For backwards compatibility it also
 * updates {@code mod_version} in {@code gradle.properties} when present.
 */
public abstract class BumpVersionTask extends DefaultTask {

	private static final Pattern CATALOG_MOD_VERSION = Pattern.compile("^\\s*mod-version\\s*=.*");

	@InputFile
	public abstract RegularFileProperty getPropertiesFile();

	@Input
	public abstract Property<String> getCurrentVersion();

	@TaskAction
	public void execute() throws IOException {
		File file = getPropertiesFile().get().getAsFile();
		String currentVersionStr = getCurrentVersion().get();

		String[] parts = currentVersionStr.split("\\.");
		if (parts.length != 3) {
			throw new GradleException("mod_version '" + currentVersionStr + "' is not in x.y.z format");
		}

		int newPatch = Integer.parseInt(parts[2]) + 1;
		String newVersion = parts[0] + "." + parts[1] + "." + newPatch;

		updateFile(file, newVersion);
		getLogger().lifecycle("Bumped mod version: {} -> {}", currentVersionStr, newVersion);

		String githubOutput = System.getenv("GITHUB_OUTPUT");
		if (githubOutput != null) {
			Files.writeString(new File(githubOutput).toPath(), "new_version=" + newVersion + "\n",
				StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
		}

		String githubEnv = System.getenv("GITHUB_ENV");
		if (githubEnv != null) {
			Files.writeString(new File(githubEnv).toPath(), "NEW_VERSION=" + newVersion + "\n",
				StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
		}
	}

	private void updateFile(File file, String newVersion) throws IOException {
		List<String> lines = Files.readAllLines(file.toPath());
		List<String> result = new ArrayList<>();
		boolean catalogFound = false;

		for (String line : lines) {
			if (CATALOG_MOD_VERSION.matcher(line).matches()) {
				result.add(line.replaceFirst("=.*", "= \"" + newVersion + "\""));
				catalogFound = true;
			} else if (line.startsWith("mod_version=")) {
				result.add("mod_version=" + newVersion);
			} else {
				result.add(line);
			}
		}

		if (!catalogFound) {
			result = insertIntoVersions(result, newVersion);
		}

		Files.write(file.toPath(), result);
	}

	private List<String> insertIntoVersions(List<String> lines, String newVersion) {
		List<String> result = new ArrayList<>();
		boolean inserted = false;
		for (String line : lines) {
			result.add(line);
			if (!inserted && line.trim().equals("[versions]")) {
				result.add("mod-version = \"" + newVersion + "\"");
				inserted = true;
			}
		}
		return inserted ? result : lines;
	}
}