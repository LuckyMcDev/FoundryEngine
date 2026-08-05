package de.luckymcdev.foundryengine.plugin;

import de.luckymcdev.foundryengine.dsl.FoundryEngineExtension;
import de.luckymcdev.foundryengine.dsl.ModExtension;
import me.modmuss50.mpp.ModPublishExtension;
import me.modmuss50.mpp.ReleaseType;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.JavadocMemberLevel;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

/**
 * Configures javadoc/sources jars, Maven publishing, mod publishing, and the
 * versioning/changelog/package-info tasks.
 */
public class PublishingComponent implements FoundryEngineComponent {

	private final FoundryEngineExtension extension;

	public PublishingComponent(FoundryEngineExtension extension) {
		this.extension = extension;
	}

	@Override
	public void apply(Project project) {
		project.getPlugins().apply("maven-publish");
		project.getPlugins().apply("me.modmuss50.mod-publish-plugin");

		ModExtension mod = extension.getMod();
		de.luckymcdev.foundryengine.dsl.PublishingExtension publishing = extension.getPublishing();
		String modVersion = mod.getVersion().get();
		String modName = mod.getName().get();
		String githubRepo = publishing.getGithubRepository().get();
		String githubCommitish = publishing.getGithubCommitish().get();
		ReleaseType releaseType = publishing.getReleaseType().get();
		SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

		// Javadoc configuration
		project.getTasks().named("javadoc", Javadoc.class, javadoc -> {
			StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadoc.getOptions();
			options.setEncoding("UTF-8");
			options.setMemberLevel(JavadocMemberLevel.PROTECTED);
			options.setAuthor(false);
			options.setVersion(true);
			options.setWindowTitle(modName + " " + modVersion);
			options.setDocTitle(modName + " " + modVersion);
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
			jar.from(sourceSets.getByName("main").getAllJava());
		});

		project.getTasks().named("assemble", task -> task.dependsOn("javadocJar"));

		// Mod publishing
		ModPublishExtension publishMods = project.getExtensions().getByType(ModPublishExtension.class);
		publishMods.getChangelog().set(project.getProviders().environmentVariable("CHANGELOG")
			.orElse("No changelog provided."));
		publishMods.getType().set(releaseType);
		publishMods.getModLoaders().add("neoforge");
		publishMods.getVersion().set(modVersion);
		publishMods.getDisplayName().set(modName + " " + modVersion);
		publishMods.getFile().set(project.getTasks().named("jar", Jar.class).flatMap(Jar::getArchiveFile));
		publishMods.getAdditionalFiles().from(
			project.getTasks().named("javadocJar", Jar.class).flatMap(Jar::getArchiveFile),
			project.getTasks().named("sourcesJar", Jar.class).flatMap(Jar::getArchiveFile));
		publishMods.github(github -> {
			github.getAccessToken().set(project.getProviders().environmentVariable("GITHUB_TOKEN"));
			github.getRepository().set(githubRepo);
			github.getCommitish().set(githubCommitish);
			github.getTagName().set("v" + modVersion);
		});
		publishMods.curseforge(curseforge -> {
			curseforge.getAccessToken().set(project.getProviders().environmentVariable("CURSEFORGE_TOKEN"));
			curseforge.getProjectId().set(publishing.getCurseforgeProjectId()
				.orElse(project.getProviders().environmentVariable("CURSEFORGE_PROJECT_ID")));
			curseforge.getMinecraftVersions().add(extension.getMinecraft().getMinecraftVersion());
			curseforge.getJavaVersions().add(JavaVersion.VERSION_25);
			curseforge.getClientRequired().set(true);
			curseforge.getServerRequired().set(true);
		});
		publishMods.modrinth(modrinth -> {
			modrinth.getAccessToken().set(project.getProviders().environmentVariable("MODRINTH_TOKEN"));
			modrinth.getProjectId().set(publishing.getModrinthProjectId()
				.orElse(project.getProviders().environmentVariable("MODRINTH_PROJECT_ID")));
			modrinth.getMinecraftVersions().add(extension.getMinecraft().getMinecraftVersion());
		});

		// Maven publishing
		project.getExtensions().configure(PublishingExtension.class, publishingExt -> {
			publishingExt.getPublications().register("mavenJava", MavenPublication.class, pub -> {
				pub.from(project.getComponents().getByName("java"));
				pub.artifact(project.getTasks().named("javadocJar"));
				pub.artifact(project.getTasks().named("sourcesJar"));
			});
			publishingExt.getRepositories().maven(repo -> {
				repo.setName("Local");
				repo.setUrl(project.file("repo").toURI());
			});
		});

		registerMetaTasks(project, mod);
	}

	private void registerMetaTasks(Project project, ModExtension mod) {
		// Bump version – updates the version catalog
		project.getTasks().register("bumpVersion", BumpVersionTask.class, task -> {
			task.setGroup("foundryengine");
			task.setDescription("Bumps the patch version in gradle/libs.versions.toml and writes the new version to GITHUB_OUTPUT/GITHUB_ENV if running in CI.");
			task.getPropertiesFile().set(project.file("gradle/libs.versions.toml"));
			task.getCurrentVersion().set(mod.getVersion());
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
	}
}