package de.luckymcdev.foundryengine.plugin;

import de.luckymcdev.foundryengine.dsl.FoundryEngineExtension;
import de.luckymcdev.foundryengine.internal.VersionCatalogReader;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.List;

/**
 * Main entry point for the FoundryEngine build plugin. Creates the
 * {@code foundryengine} extension and applies each concern component in order.
 */
public class FoundryEnginePlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		VersionCatalogReader reader = new VersionCatalogReader(project);
		FoundryEngineExtension extension = project.getObjects().newInstance(FoundryEngineExtension.class);
		project.getExtensions().add("foundryengine", extension);

		applyDefaults(extension, reader);

		// Apply concern components in order
		List<FoundryEngineComponent> components = List.of(
			new MinecraftComponent(extension),
			new ModsTomlComponent(extension),
			new MixinComponent(extension),
			new BundlesComponent(extension),
			new PublishingComponent(extension),
			new VitePressComponent(extension));

		components.forEach(component -> component.apply(project));
	}

	private void applyDefaults(FoundryEngineExtension extension, VersionCatalogReader reader) {
		extension.getMod().getId().convention(reader.value("mod_id", "mod-id"));
		extension.getMod().getName().convention(reader.value("mod_name", "mod-name"));
		extension.getMod().getVersion().convention(reader.value("mod_version", "mod-version"));
		extension.getMod().getGroup().convention(reader.value("mod_group_id", "mod-group"));
		extension.getMod().getLicense().convention(reader.value("mod_license", "mod-license"));
		extension.getMod().getAuthors().convention(reader.value("mod_authors", "mod-authors"));
		extension.getMod().getDescription().convention(reader.value("mod_description", "mod-description"));

		extension.getMinecraft().getMinecraftVersion().convention(reader.value("minecraft_version", "minecraft"));
		extension.getMinecraft().getMinecraftVersionRange().convention(reader.value("minecraft_version_range", "minecraft-range"));
		extension.getMinecraft().getNeoVersion().convention(reader.value("neo_version", "neo"));
		extension.getMinecraft().getNeoVersionRange().convention(reader.value("neo_version_range", "neo-range"));
		extension.getMinecraft().getLoaderVersionRange().convention(reader.value("loader_version_range", "loader-range"));
	}
}