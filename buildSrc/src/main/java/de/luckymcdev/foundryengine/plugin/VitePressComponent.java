package de.luckymcdev.foundryengine.plugin;

import de.luckymcdev.foundryengine.dsl.FoundryEngineExtension;
import de.luckymcdev.foundryengine.dsl.VitePressExtension;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;

/**
 * Registers the VitePress {@code docs:dev}/{@code docs:build}/{@code docs:preview}
 * npm task wrappers.
 */
public class VitePressComponent implements FoundryEngineComponent {

	private final FoundryEngineExtension extension;

	public VitePressComponent(FoundryEngineExtension extension) {
		this.extension = extension;
	}

	@Override
	public void apply(Project project) {
		VitePressExtension vitepress = extension.getVitepress();
		String sourceDir = vitepress.getSourceDir().get();
		Directory docsDir = project.getLayout().getProjectDirectory().dir(sourceDir);

		registerNpmTask(project, docsDir, "npmInstallDeps", "Installs npm dependencies for the docs site.", "install");
		registerNpmTask(project, docsDir, "docsDev", "Starts the VitePress dev server for the docs site.", "run", "docs:dev");
		registerNpmTask(project, docsDir, "docsBuild", "Builds the VitePress docs site.", "run", "docs:build");
		registerNpmTask(project, docsDir, "docsPreview", "Previews the built VitePress docs site.", "run", "docs:preview");

		project.getTasks().named("docsDev", task -> task.dependsOn("npmInstallDeps"));
		project.getTasks().named("docsBuild", task -> task.dependsOn("npmInstallDeps"));
		project.getTasks().named("docsPreview", task -> task.dependsOn("docsBuild"));
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