package de.luckymcdev.foundryengine.dsl;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * Minecraft / NeoForge configuration. Values fall back to the version catalog or
 * {@code gradle.properties}.
 */
public abstract class MinecraftExtension {

	@Inject
	public MinecraftExtension() {
	}

	/**
	 * Minecraft version (used for JEI coordinates and the TOML dependency range).
	 */
	public abstract Property<String> getMinecraftVersion();

	/**
	 * Minecraft version range in the TOML dependency.
	 */
	public abstract Property<String> getMinecraftVersionRange();

	/**
	 * NeoForge version passed to {@code neoForge.version}.
	 */
	public abstract Property<String> getNeoVersion();

	/**
	 * NeoForge version range in the TOML dependency.
	 */
	public abstract Property<String> getNeoVersionRange();

	/**
	 * FML loader version range in the TOML header.
	 */
	public abstract Property<String> getLoaderVersionRange();

	/**
	 * Run configuration container.
	 */
	public abstract NamedDomainObjectContainer<RunConfig> getRuns();

	/**
	 * Absolute path to the mixin {@code -javaagent} jar.
	 */
	public abstract Property<String> getMixinJavaagent();
}