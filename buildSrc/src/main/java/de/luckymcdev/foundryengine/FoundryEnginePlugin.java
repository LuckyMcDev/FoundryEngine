package de.luckymcdev.foundryengine;

import me.modmuss50.mpp.ModPublishExtension;
import me.modmuss50.mpp.ReleaseType;
import net.neoforged.moddevgradle.dsl.NeoForgeExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.JavadocMemberLevel;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.slf4j.event.Level;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class FoundryEnginePlugin implements Plugin<Project> {

	// Default values used if nothing else is provided
	private static final List<String> DEFAULT_RUN_DIRS = Arrays.asList("client", "server", "gameTestServer");
	private static final String DEFAULT_BUNDLES_SUB_PATH = "FoundryEngine/bundles";
	private static final String DEFAULT_EXAMPLE_BUNDLES_SOURCE = "ExampleBundles";
	private static final String DEFAULT_GITHUB_REPO = "LuckyMcDev/FoundryEngine";
	private static final String DEFAULT_GITHUB_COMMITISH = "master";

	private FoundryEngineExtension extension;

	@Override
	public void apply(Project project) {
		// Create extension
		extension = project.getExtensions().create("foundryengine", FoundryEngineExtension.class, project);

		// Apply required plugins
		project.getPlugins().apply("java-library");
		project.getPlugins().apply("maven-publish");
		project.getPlugins().apply("idea");
		project.getPlugins().apply("net.neoforged.moddev");
		project.getPlugins().apply("me.modmuss50.mod-publish-plugin");

		NeoForgeExtension neoForge = project.getExtensions().getByType(NeoForgeExtension.class);
		configureNeoForge(project, neoForge);
		configurePublishing(project);
		registerCustomTasks(project);
	}

	// -----------------------------------------------------------------------------------
	// Helper to get a property from the extension (if set) or fall back to project property
	// -----------------------------------------------------------------------------------
	private String getProperty(Property<String> extensionProp, String projectPropName, Project project) {
		String value = extensionProp.getOrNull();
		if (value != null && !value.isEmpty()) {
			return value;
		}
		if (projectPropName != null) {
			return project.getProviders().gradleProperty(projectPropName).getOrNull();
		}
		return null;
	}

	private List<String> getRunDirs(Project project) {
		List<String> dirs = extension.getRunDirs().getOrNull();
		if (dirs != null && !dirs.isEmpty()) {
			return dirs;
		}
		return DEFAULT_RUN_DIRS;
	}

	// -----------------------------------------------------------------------------------
	// NeoForge configuration
	// -----------------------------------------------------------------------------------
	private void configureNeoForge(Project project, NeoForgeExtension neoForge) {
		String modId = getProperty(extension.getModId(), "mod_id", project);
		String neoVersion = getProperty(extension.getNeoVersion(), "neo_version", project);

		if (modId == null) {
			throw new IllegalArgumentException("mod_id must be set (via extension or project property)");
		}
		if (neoVersion == null) {
			throw new IllegalArgumentException("neo_version must be set (via extension or project property)");
		}

		neoForge.setVersion(neoVersion);

		// Common run settings
		neoForge.getRuns().configureEach(run -> {
			run.getSystemProperties().put("forge.logging.markers", "REGISTRIES");
			run.getSystemProperties().put("terminal.ansi", "true");
			run.getJvmArguments().addAll(
				"-XX:+IgnoreUnrecognizedVMOptions",
				"-XX:+AllowEnhancedClassRedefinition",
				"-javaagent:'" + System.getProperty("user.home")
					+ "\\.gradle\\caches\\modules-2\\files-2.1\\net.fabricmc\\sponge-mixin\\0.17.3+mixin.0.8.7\\41c4a3984a80f4679e759fb9f495587acc5cdac7\\sponge-mixin-0.17.3+mixin.0.8.7.jar'");
			run.getLogLevel().set(Level.DEBUG);
		});

		// Register runs for each directory in the list
		List<String> runDirs = getRunDirs(project);
		for (String dir : runDirs) {
			neoForge.getRuns().register(dir, run -> {
				if ("client".equals(dir)) {
					run.client();
				} else if ("server".equals(dir)) {
					run.server();
					run.programArgument("--nogui");
				} else if ("gameTestServer".equals(dir)) {
					run.getType().set("gameTestServer");
				} else {
					// Generic run – assume it's a server/client based on name? We'll keep it generic.
					// You can add more specific logic if needed.
					run.getType().set(dir); // maybe not ideal, but works.
				}
				run.getSystemProperties().put("neoforge.enabledGameTestNamespaces", modId);
				run.getGameDirectory().set(project.file("runs/" + dir));
			});
		}

		// Data run (always added)
		neoForge.getRuns().register("data", run -> {
			run.clientData();
			run.serverData();
			run.getProgramArguments().addAll(
				"--mod", modId,
				"--all",
				"--output", project.file("src/generated/resources/").getAbsolutePath(),
				"--existing", project.file("src/main/resources/").getAbsolutePath());
			run.getGameDirectory().set(project.file("runs/data"));
		});

		SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
		neoForge.getMods().register(modId, mod -> mod.sourceSet(sourceSets.getByName("main")));

		neoForge.getUnitTest().enable();
		neoForge.getUnitTest().getTestedMod().set(neoForge.getMods().named(modId));

		sourceSets.getByName("main").getResources().srcDir("src/generated/resources");

		// Process resources with property expansion
		project.getTasks().named("processResources", ProcessResources.class, task -> {
			Map<String, Object> replaceProperties = new LinkedHashMap<>();
			replaceProperties.put("minecraft_version", getProperty(extension.getMinecraftVersion(), "minecraft_version", project));
			replaceProperties.put("minecraft_version_range", getProperty(extension.getMinecraftVersionRange(), "minecraft_version_range", project));
			replaceProperties.put("neo_version", neoVersion);
			replaceProperties.put("neo_version_range", getProperty(extension.getNeoVersionRange(), "neo_version_range", project));
			replaceProperties.put("loader_version_range", getProperty(extension.getLoaderVersionRange(), "loader_version_range", project));
			replaceProperties.put("mod_id", modId);
			replaceProperties.put("mod_name", getProperty(extension.getModName(), "mod_name", project));
			replaceProperties.put("mod_license", getProperty(extension.getModLicense(), "mod_license", project));
			replaceProperties.put("mod_version", getProperty(extension.getModVersion(), "mod_version", project));
			replaceProperties.put("mod_authors", getProperty(extension.getModAuthors(), "mod_authors", project));
			replaceProperties.put("mod_description", getProperty(extension.getModDescription(), "mod_description", project));

			// Remove entries with null values to avoid errors
			replaceProperties.values().removeIf(v -> v == null);

			task.getInputs().properties(replaceProperties);
			task.filesMatching("META-INF/neoforge.mods.toml", file -> file.expand(replaceProperties));
		});
	}

	private void configurePublishing(Project project) {
		String modName = getProperty(extension.getModName(), "mod_name", project);
		String modVersion = getProperty(extension.getModVersion(), "mod_version", project);
		String githubRepo = getProperty(extension.getGithubRepository(), null, project);
		if (githubRepo == null) {
			githubRepo = DEFAULT_GITHUB_REPO;
		}
		String githubCommitish = getProperty(extension.getGithubCommitish(), null, project);
		if (githubCommitish == null) {
			githubCommitish = DEFAULT_GITHUB_COMMITISH;
		}

		SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

		// Javadoc configuration
		project.getTasks().named("javadoc", Javadoc.class, javadoc -> {
			StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadoc.getOptions();
			options.setEncoding("UTF-8");
			options.setMemberLevel(JavadocMemberLevel.PROTECTED);
			options.setAuthor(false);
			options.setVersion(true);
			options.setWindowTitle("FoundryEngine " + modVersion);
			options.setDocTitle("FoundryEngine " + modVersion);
			options.links("https://docs.oracle.com/en/java/javase/25/docs/api/");
			javadoc.setSource(sourceSets.getByName("main").getAllJava());
			javadoc.exclude("**/internal/**");
			javadoc.setFailOnError(false);
		});

		// Javadoc JAR
		project.getTasks().register("javadocJar", Jar.class, jar -> {
			jar.setDescription("A Javadoc JAR built with the standard Javadoc tool");
			jar.setGroup("documentation");
			jar.dependsOn("javadoc");
			jar.from(project.getTasks().named("javadoc", Javadoc.class).map(Javadoc::getDestinationDir));
			jar.getArchiveClassifier().set("javadoc");
		});

		// Sources JAR
		project.getTasks().register("sourcesJar", Jar.class, jar -> {
			jar.setDescription("A sources JAR built from the main source set");
			jar.setGroup("build");
			jar.getArchiveClassifier().set("sources");
			jar.from(sourceSets.getByName("main").getAllSource());
		});

		project.getTasks().named("assemble", task -> task.dependsOn("javadocJar"));

		// Mod publishing
		ModPublishExtension publishMods = project.getExtensions().getByType(ModPublishExtension.class);
		publishMods.getChangelog().set(project.getProviders().environmentVariable("CHANGELOG")
			.orElse("No changelog provided."));
		// Release type: from extension or default ALPHA
		ReleaseType releaseType = extension.getReleaseType().getOrNull();
		publishMods.getType().set(releaseType != null ? releaseType : ReleaseType.ALPHA);
		publishMods.getModLoaders().add("neoforge");
		publishMods.getVersion().set(modVersion);
		publishMods.getDisplayName().set(modName + " " + modVersion);
		publishMods.getFile().set(project.getTasks().named("jar", Jar.class).flatMap(Jar::getArchiveFile));
		publishMods.getAdditionalFiles().from(
			project.getTasks().named("javadocJar", Jar.class).flatMap(Jar::getArchiveFile),
			project.getTasks().named("sourcesJar", Jar.class).flatMap(Jar::getArchiveFile));
		String finalGithubRepo = githubRepo;
		String finalGithubCommitish = githubCommitish;
		publishMods.github(github -> {
			github.getAccessToken().set(project.getProviders().environmentVariable("GITHUB_TOKEN"));
			github.getRepository().set(finalGithubRepo);
			github.getCommitish().set(finalGithubCommitish);
			github.getTagName().set("v" + modVersion);
		});

		// Maven publishing
		project.getExtensions().configure(PublishingExtension.class, publishing -> {
			publishing.getPublications().register("mavenJava", MavenPublication.class, pub -> {
				pub.from(project.getComponents().getByName("java"));
				pub.artifact(project.getTasks().named("javadocJar"));
				pub.artifact(project.getTasks().named("sourcesJar"));
			});
			publishing.getRepositories().maven(repo -> {
				repo.setName("Local");
				repo.setUrl(project.file("repo").toURI());
			});
		});
	}

	private void registerCustomTasks(Project project) {
		// Bump version – uses the current version from project properties (or extension)
		project.getTasks().register("bumpVersion", BumpVersionTask.class, task -> {
			task.setGroup("foundryengine");
			task.setDescription("Bumps the patch version in gradle.properties and writes the new version to GITHUB_OUTPUT/GITHUB_ENV if running in CI.");
			task.getPropertiesFile().set(project.file("gradle.properties"));
			// We use the extension's modVersion if set, otherwise fallback to project property
			String currentVersion = getProperty(extension.getModVersion(), "mod_version", project);
			task.getCurrentVersion().set(currentVersion);
		});

		// Changelog generation
		project.getTasks().register("generateFullChangelog", GenerateFullChangelogTask.class, task -> {
			task.setGroup("foundryengine");
			task.setDescription("Generates a full changelog from the last release tag (or a given tag) to HEAD.");
		});

		// Package-info generation
		project.getTasks().register("generatePackageInfo", GeneratePackageInfoTask.class, task -> {
			task.setGroup("foundryengine");
			task.setDescription("Generates missing package-info.java files for all Java source directories.");
			task.getSourceDir().set(project.file("src/main/java"));
		});

		// Pre-commit
		project.getTasks().register("preCommit", task -> {
			task.setGroup("foundryengine");
			task.setDescription("Runs all things needed for a commit.");
			task.dependsOn("check", "runData", "build");
		});

		// Copy example bundles – using the configured run directories and paths
		List<String> runDirs = getRunDirs(project);
		String source = extension.getExampleBundlesSource().getOrNull();
		if (source == null || source.isEmpty()) {
			source = DEFAULT_EXAMPLE_BUNDLES_SOURCE;
		}
		String bundlesSubPath = extension.getBundlesSubPath().getOrNull();
		if (bundlesSubPath == null || bundlesSubPath.isEmpty()) {
			bundlesSubPath = DEFAULT_BUNDLES_SUB_PATH;
		}

		List<String> copyTaskNames = runDirs.stream()
			.map(dir -> "copyExampleBundlesTo" + capitalize(dir))
			.collect(Collectors.toList());

		for (int i = 0; i < runDirs.size(); i++) {
			String dir = runDirs.get(i);
			String taskName = copyTaskNames.get(i);
			String destination = "runs/" + dir + "/" + bundlesSubPath;
			String finalSource = source;
			project.getTasks().register(taskName, Copy.class, task -> {
				task.setGroup("foundryengine");
				task.setDescription("Copies the Example Bundles to the " + dir + " run directory for testing.");
				task.from(finalSource);
				task.into(destination);
			});
		}

		project.getTasks().register("copyExampleBundles", task -> {
			task.setGroup("foundryengine");
			task.setDescription("Copies the Example Bundles to all run directories for testing.");
			task.dependsOn(copyTaskNames);
		});
	}

	private String capitalize(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}

	public static abstract class FoundryEngineExtension {
		private final Project project;

		@Inject
		public FoundryEngineExtension(Project project) {
			this.project = project;
		}

		// Core mod properties
		public abstract Property<String> getModId();

		public abstract Property<String> getModName();

		public abstract Property<String> getModVersion();

		public abstract Property<String> getModLicense();

		public abstract Property<String> getModAuthors();

		public abstract Property<String> getModDescription();

		// Version ranges
		public abstract Property<String> getMinecraftVersion();

		public abstract Property<String> getMinecraftVersionRange();

		public abstract Property<String> getNeoVersion();

		public abstract Property<String> getNeoVersionRange();

		public abstract Property<String> getLoaderVersionRange();

		// Run / bundle paths
		public abstract ListProperty<String> getRunDirs();

		public abstract Property<String> getBundlesSubPath();

		public abstract Property<String> getExampleBundlesSource();

		// GitHub publishing
		public abstract Property<String> getGithubRepository();

		public abstract Property<String> getGithubCommitish();

		public abstract Property<ReleaseType> getReleaseType();

		// Optional: default values can be set in the constructor or via convention
		// For simplicity, we rely on the plugin's defaults.
	}
}