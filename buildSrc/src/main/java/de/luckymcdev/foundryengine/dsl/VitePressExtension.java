package de.luckymcdev.foundryengine.dsl;

import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * VitePress documentation site configuration.
 */
public abstract class VitePressExtension {

	@Inject
	public VitePressExtension() {
		getSourceDir().convention("docs");
	}

	/**
	 * Directory holding the VitePress site (relative to the project root).
	 */
	public abstract Property<String> getSourceDir();
}