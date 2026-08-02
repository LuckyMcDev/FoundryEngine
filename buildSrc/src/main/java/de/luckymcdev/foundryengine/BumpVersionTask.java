package de.luckymcdev.foundryengine;

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
import java.util.List;
import java.util.stream.Collectors;

public abstract class BumpVersionTask extends DefaultTask {

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

		List<String> lines = Files.readAllLines(file.toPath());
		List<String> updatedLines = lines.stream()
			.map(line -> line.startsWith("mod_version=") ? "mod_version=" + newVersion : line)
			.collect(Collectors.toList());

		Files.write(file.toPath(), updatedLines);
		getLogger().lifecycle("Bumped mod_version: {} -> {}", currentVersionStr, newVersion);

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
}