package de.luckymcdev.foundryengine.plugin;

import de.luckymcdev.foundryengine.dsl.FoundryEngineExtension;
import de.luckymcdev.foundryengine.dsl.VitePressExtension;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;

/**
 * Registers the Docusaurus npm task wrappers.
 */
public class DocusaurusComponent implements FoundryEngineComponent {

	private final FoundryEngineExtension extension;

	public DocusaurusComponent(FoundryEngineExtension extension) {
		this.extension = extension;
	}

	@Override
	public void apply(Project project) {
		VitePressExtension vitepress = extension.getVitepress();
		String sourceDir = vitepress.getSourceDir().get();
		Directory docsDir = project.getLayout().getProjectDirectory().dir(sourceDir);

		registerNpmTask(project, docsDir, "npmInstallDeps", "Installs npm dependencies for the docs site.", "install");
		registerNpmTask(project, docsDir, "docsStart", "Starts the VitePress dev server for the docs site.", "run", "start");
		registerNpmTask(project, docsDir, "docsBuild", "Builds the VitePress docs site.", "run", "build");
		registerNpmTask(project, docsDir, "docsServe", "Previews the built VitePress docs site.", "run", "serve");

		project.getTasks().named("docsStart", task -> task.dependsOn("npmInstallDeps"));
		project.getTasks().named("docsBuild", task -> task.dependsOn("npmInstallDeps"));
		project.getTasks().named("docsServe", task -> task.dependsOn("docsBuild"));
	}

	private void registerNpmTask(Project project, Directory docsDir, String name, String description, String... args) {
		project.getTasks().register(name, NpmTask.class, task -> {
			task.setGroup("documentation");
			task.setDescription(description);
			task.getWorkingDirectory().set(docsDir);
			task.getArguments().set(java.util.List.of(args));
		});
	}
}