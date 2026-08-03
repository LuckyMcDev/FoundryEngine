package de.luckymcdev.foundryengine.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs an {@code npm} command in a given working directory.
 */
public abstract class NpmTask extends DefaultTask {

	private static String npmCommand() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.contains("win") ? "npm.cmd" : "npm";
	}

	@InputDirectory
	public abstract DirectoryProperty getWorkingDirectory();

	@Input
	public abstract ListProperty<String> getArguments();

	@Inject
	protected abstract ExecOperations getExecOperations();

	@TaskAction
	public void run() {
		List<String> command = new ArrayList<>();
		command.add(npmCommand());
		command.addAll(getArguments().get());

		getLogger().lifecycle("Running: {} in {}", String.join(" ", command), getWorkingDirectory().get().getAsFile());

		ExecResult result = getExecOperations().exec(spec -> {
			spec.setWorkingDir(getWorkingDirectory().get().getAsFile());
			spec.commandLine(command);
			spec.setStandardOutput(System.out);
			spec.setErrorOutput(System.err);
		});

		if (result.getExitValue() != 0) {
			throw new GradleException("npm " + String.join(" ", getArguments().get())
				+ " failed with exit code " + result.getExitValue());
		}
	}
}