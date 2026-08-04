package de.luckymcdev.foundryengine.plugin;

import de.luckymcdev.foundryengine.dsl.FoundryEngineExtension;
import de.luckymcdev.foundryengine.dsl.MinecraftExtension;
import de.luckymcdev.foundryengine.dsl.MixinConfig;
import de.luckymcdev.foundryengine.dsl.MixinsExtension;
import de.luckymcdev.foundryengine.dsl.ModExtension;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;

/**
 * Generates {@code neoforge.mods.toml} into {@code src/generated/resources} from
 * the extension model and the registered mixin configs.
 */
public class ModsTomlComponent implements FoundryEngineComponent {

	private final FoundryEngineExtension extension;

	public ModsTomlComponent(FoundryEngineExtension extension) {
		this.extension = extension;
	}

	@Override
	public void apply(Project project) {
		ModExtension mod = extension.getMod();
		MinecraftExtension minecraft = extension.getMinecraft();
		MixinsExtension mixins = extension.getMixins();
		DirectoryProperty generatedResources = project.getObjects().directoryProperty();
		generatedResources.set(project.getLayout().getProjectDirectory().dir("src/generated/resources"));

		var task = project.getTasks().register("generateModsToml", GenerateModsTomlTask.class, t -> {
			t.setGroup("foundryengine");
			t.setDescription("Generates META-INF/neoforge.mods.toml from the foundryengine DSL.");
			t.getModId().set(mod.getId());
			t.getModVersion().set(mod.getVersion());
			t.getModName().set(mod.getName());
			t.getModLicense().set(mod.getLicense());
			t.getModAuthors().set(mod.getAuthors());
			t.getModDescription().set(mod.getDescription());
			t.getDisplayUrl().set(mod.getDisplayUrl());
			t.getLogoFile().set(mod.getLogoFile());
			t.getCredits().set(mod.getCredits());
			t.getLoaderVersionRange().set(minecraft.getLoaderVersionRange());
			t.getNeoVersionRange().set(minecraft.getNeoVersionRange());
			t.getMinecraftVersionRange().set(minecraft.getMinecraftVersionRange());
			t.getMixinConfigFiles().set(project.getProviders().provider(() -> {
				String id = mod.getId().get();
				return mixins.getConfigs().stream()
					.map(config -> MixinConfig.fileName(id, config.getName()))
					.sorted()
					.toList();
			}));
			t.getAccessTransformerFiles().set(project.getObjects().listProperty(String.class));
			t.getOutputFile().set(generatedResources.file("META-INF/neoforge.mods.toml"));
		});

		// Ensure generation runs before resources are processed
		project.getTasks().named("processResources", it -> it.dependsOn(task));
	}
}