package de.luckymcdev.foundryengine.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class GenerateFullChangelogTask extends DefaultTask {

	@Inject
	public GenerateFullChangelogTask(Project project) {
		getChangelogFile().convention(project.getLayout().getProjectDirectory().file("CHANGELOG.md"));
		getExtraArgs().convention(List.of("-vv"));
	}

	@Inject
	protected abstract ExecOperations getExecOperations();

	@Input
	public abstract ListProperty<String> getExtraArgs();

	@OutputFile
	public abstract RegularFileProperty getChangelogFile();

	@TaskAction
	public void execute() {
		File outputFile = getChangelogFile().getAsFile().get();

		List<String> command = new ArrayList<>();
		command.add("git-cliff");
		command.add("-o");
		command.add(outputFile.getAbsolutePath());
		command.addAll(getExtraArgs().get());

		getLogger().lifecycle("Running: {}", String.join(" ", command));

		ExecResult result = getExecOperations().exec(spec -> {
			spec.commandLine(command);
			spec.setStandardOutput(System.out);
			spec.setErrorOutput(System.err);
		});

		if (result.getExitValue() != 0) {
			throw new GradleException("git-cliff failed with exit code " + result.getExitValue());
		}

		getLogger().lifecycle("Changelog written to {}", outputFile);
	}
}