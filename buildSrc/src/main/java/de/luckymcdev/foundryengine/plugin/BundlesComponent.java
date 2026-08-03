package de.luckymcdev.foundryengine.plugin;

import de.luckymcdev.foundryengine.dsl.BundleDependency;
import de.luckymcdev.foundryengine.dsl.BundlesExtension;
import de.luckymcdev.foundryengine.dsl.FoundryEngineExtension;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.Copy;

import java.util.ArrayList;
import java.util.List;

/**
 * Declares bundled libraries ({@code bundles { include(...) }}) and the
 * {@code copyExampleBundles*} tasks.
 */
public class BundlesComponent implements FoundryEngineComponent {

	private final FoundryEngineExtension extension;

	public BundlesComponent(FoundryEngineExtension extension) {
		this.extension = extension;
	}

	private static String versionOf(String coordinate) {
		int firstColon = coordinate.indexOf(':');
		int secondColon = coordinate.indexOf(':', firstColon + 1);
		if (secondColon < 0) {
			return null;
		}
		return coordinate.substring(secondColon + 1);
	}

	@Override
	public void apply(Project project) {
		registerCopyTasks(project);
		project.afterEvaluate(p -> applyBundledDependencies(p));
	}

	private void applyBundledDependencies(Project project) {
		BundlesExtension bundles = extension.getBundles();
		DependencyHandler dependencies = project.getDependencies();

		for (BundleDependency dep : bundles.getDependencies()) {
			String coordinate = dep.getCoordinate().get();
			String version = dep.getVersion().getOrNull();
			if (version == null) {
				version = versionOf(coordinate);
			}
			String gav = version == null || version.isEmpty() ? coordinate : coordinate + ":" + version;
			applyBundle(project, dependencies, gav, dep.getExcludes().get());
		}
	}

	private void applyBundle(Project project, DependencyHandler dependencies, String gav, List<String> excludes) {
		Dependency base = dependencies.create(gav);
		if (base instanceof org.gradle.api.artifacts.ExternalModuleDependency external) {
			for (String exclude : excludes) {
				java.util.Map<String, String> rule = new java.util.HashMap<>();
				String[] parts = exclude.split(":", 2);
				rule.put("group", parts[0]);
				if (parts.length > 1) {
					rule.put("module", parts[1]);
				}
				external.exclude(rule);
			}
		}
		// jarJar(api(implementation(gav)))
		dependencies.add("implementation", base);
		dependencies.add("api", base);
		dependencies.add("jarJar", base);
	}

	private void registerCopyTasks(Project project) {
		BundlesExtension bundles = extension.getBundles();
		String source = bundles.getExampleBundlesSource().get();
		String bundlesSubPath = bundles.getBundlesSubPath().get();
		List<String> runDirs = getRunDirs(project);

		List<String> copyTaskNames = new ArrayList<>();
		for (String dir : runDirs) {
			String taskName = "copyExampleBundlesTo" + capitalize(dir);
			String destination = "runs/" + dir + "/" + bundlesSubPath;
			copyTaskNames.add(taskName);
			project.getTasks().register(taskName, Copy.class, task -> {
				task.setGroup("foundryengine");
				task.setDescription("Copies the Example Bundles to the " + dir + " run directory for testing.");
				task.from(source);
				task.into(destination);
			});
		}

		project.getTasks().register("copyExampleBundles", task -> {
			task.setGroup("foundryengine");
			task.setDescription("Copies the Example Bundles to all run directories for testing.");
			task.dependsOn(copyTaskNames);
		});
	}

	private List<String> getRunDirs(Project project) {
		return extension.getMinecraft().getRuns().getNames().stream()
			.filter(name -> !name.equals("data"))
			.toList();
	}

	private String capitalize(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}
}