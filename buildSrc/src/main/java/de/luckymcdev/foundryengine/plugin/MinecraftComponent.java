package de.luckymcdev.foundryengine.plugin;

import de.luckymcdev.foundryengine.dsl.FoundryEngineExtension;
import de.luckymcdev.foundryengine.dsl.MinecraftExtension;
import de.luckymcdev.foundryengine.dsl.RunConfig;
import net.neoforged.moddevgradle.dsl.NeoForgeExtension;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.slf4j.event.Level;

import java.nio.file.Path;

/**
 * Applies {@code net.neoforged.moddev} and configures the NeoForge extension,
 * runs, mods, unit tests, and the {@code src/generated/resources} source set.
 */
public class MinecraftComponent implements FoundryEngineComponent {

	private static final String MIXIN_JAVAAGENT = ".gradle\\caches\\modules-2\\files-2.1\\net.fabricmc\\sponge-mixin"
		+ "\\0.17.3+mixin.0.8.7\\41c4a3984a80f4679e759fb9f495587acc5cdac7\\sponge-mixin-0.17.3+mixin.0.8.7.jar";

	private final FoundryEngineExtension extension;

	public MinecraftComponent(FoundryEngineExtension extension) {
		this.extension = extension;
	}

	@Override
	public void apply(Project project) {
		project.getPlugins().apply("java-library");
		project.getPlugins().apply("idea");
		project.getPlugins().apply("net.neoforged.moddev");

		NeoForgeExtension neoForge = project.getExtensions().getByType(NeoForgeExtension.class);
		MinecraftExtension minecraft = extension.getMinecraft();
		String modId = extension.getMod().getId().get();

		neoForge.setVersion(minecraft.getNeoVersion().get());

		String configuredJavaagent = minecraft.getMixinJavaagent().getOrNull();
		String javaagent = (configuredJavaagent == null || configuredJavaagent.isEmpty())
			? Path.of(System.getProperty("user.home"), MIXIN_JAVAAGENT).toString()
			: configuredJavaagent;

		// Common run settings
		neoForge.getRuns().configureEach(run -> {
			run.getSystemProperties().put("forge.logging.markers", "REGISTRIES");
			run.getSystemProperties().put("terminal.ansi", "true");
			run.getJvmArguments().addAll(
				"-XX:+IgnoreUnrecognizedVMOptions",
				"-XX:+AllowEnhancedClassRedefinition",
				"-javaagent:'" + javaagent + "'");
			run.getLogLevel().set(Level.DEBUG);
		});

		// Configured runs
		minecraft.getRuns().forEach(runConfig -> registerRun(project, neoForge, modId, runConfig));

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
		SourceSet main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		neoForge.getMods().register(modId, mod -> mod.sourceSet(main));

		neoForge.getUnitTest().enable();
		neoForge.getUnitTest().getTestedMod().set(neoForge.getMods().named(modId));

		// Generated resources are part of the main source set
		main.getResources().srcDir("src/generated/resources");
	}

	private void registerRun(Project project, NeoForgeExtension neoForge, String modId, RunConfig config) {
		String name = config.getName();
		neoForge.getRuns().register(name, run -> {
			if (config.getClient().get()) {
				run.client();
			}
			if (config.getServer().get()) {
				run.server();
			}
			if (config.getClientData().get()) {
				run.clientData();
			}
			if (config.getServerData().get()) {
				run.serverData();
			}
			String type = config.getType().get();
			if (type != null && !type.isEmpty()) {
				run.getType().set(type);
			}
			run.getProgramArguments().addAll(config.getProgramArguments());
			run.getSystemProperties().putAll(config.getSystemProperties());
			run.getJvmArguments().addAll(config.getJvmArguments());
			run.getSystemProperties().put("neoforge.enabledGameTestNamespaces", modId);
			String gameDir = config.getGameDirectory().get();
			if (gameDir != null && !gameDir.isEmpty()) {
				run.getGameDirectory().set(project.file(gameDir));
			}
		});
	}
}