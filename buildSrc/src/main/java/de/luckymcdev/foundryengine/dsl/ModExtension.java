package de.luckymcdev.foundryengine.dsl;

import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * Metadata for the mod. Unless explicitly set, each value falls back to the
 * value present in {@code gradle.properties} or the version catalog.
 */
public abstract class ModExtension {

	@Inject
	public ModExtension() {
	}

	/**
	 * {@code modId} in the {@code neoforge.mods.toml} {@code [[mods]]} block.
	 */
	public abstract Property<String> getId();

	/**
	 * {@code displayName}.
	 */
	public abstract Property<String> getName();

	/**
	 * {@code version}.
	 */
	public abstract Property<String> getVersion();

	/**
	 * Maven {@code group}.
	 */
	public abstract Property<String> getGroup();

	/**
	 * {@code license} in the TOML header.
	 */
	public abstract Property<String> getLicense();

	/**
	 * {@code authors}.
	 */
	public abstract Property<String> getAuthors();

	/**
	 * {@code description}.
	 */
	public abstract Property<String> getDescription();

	/**
	 * {@code displayURL}.
	 */
	public abstract Property<String> getDisplayUrl();

	/**
	 * {@code logoFile}.
	 */
	public abstract Property<String> getLogoFile();

	/**
	 * {@code credits}
	 */
	public abstract Property<String> getCredits();
}