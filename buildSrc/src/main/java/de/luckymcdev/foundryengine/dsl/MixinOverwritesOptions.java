package de.luckymcdev.foundryengine.dsl;

import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * Options for the {@code overwrites} block of a mixin config.
 */
public abstract class MixinOverwritesOptions {

	@Inject
	public MixinOverwritesOptions() {
	}

	/**
	 * Upgrade the visibility of overwrites that are narrower than their target.
	 */
	public abstract Property<Boolean> getConformVisibility();

	/**
	 * Require {@code @Overwrite} annotations on overwrite methods.
	 */
	public abstract Property<Boolean> getRequireAnnotations();

	/**
	 * Adopts options set on {@code defaults} that this object has not set itself.
	 */
	public void applyDefaultsIfAbsent(MixinOverwritesOptions defaults) {
		if (!getConformVisibility().isPresent()) {
			getConformVisibility().convention(defaults.getConformVisibility());
		}
		if (!getRequireAnnotations().isPresent()) {
			getRequireAnnotations().convention(defaults.getRequireAnnotations());
		}
	}
}