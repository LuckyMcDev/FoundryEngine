package de.luckymcdev.foundryengine.dsl;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * Options for the {@code injectors} block of a mixin config.
 */
public abstract class MixinInjectorsOptions {

	@Inject
	public MixinInjectorsOptions() {
	}

	/**
	 * Default value for {@code require} on injectors without an explicit value.
	 */
	public abstract Property<Integer> getDefaultRequire();

	/**
	 * Group name used for injectors without an explicit group.
	 */
	public abstract Property<String> getDefaultGroup();

	/**
	 * Namespace for custom injection points and dynamic selectors.
	 */
	public abstract Property<String> getNamespace();

	/**
	 * Fully-qualified custom injection point classes to register.
	 */
	public abstract ListProperty<String> getInjectionPoints();

	/**
	 * Fully-qualified dynamic selector classes to register.
	 */
	public abstract ListProperty<String> getDynamicSelectors();

	/**
	 * Maximum allowed {@code by} value for {@code shift}.
	 */
	public abstract Property<Integer> getMaxShiftBy();

	public void injectionPoint(String className) {
		getInjectionPoints().add(className);
	}

	public void dynamicSelector(String className) {
		getDynamicSelectors().add(className);
	}

	/**
	 * Adopts options set on {@code defaults} that this object has not set itself.
	 */
	public void applyDefaultsIfAbsent(MixinInjectorsOptions defaults) {
		if (!getDefaultRequire().isPresent()) {
			getDefaultRequire().convention(defaults.getDefaultRequire());
		}
		if (!getDefaultGroup().isPresent()) {
			getDefaultGroup().convention(defaults.getDefaultGroup());
		}
		if (!getNamespace().isPresent()) {
			getNamespace().convention(defaults.getNamespace());
		}
		if (getInjectionPoints().getOrElse(java.util.List.of()).isEmpty()) {
			getInjectionPoints().set(defaults.getInjectionPoints());
		}
		if (getDynamicSelectors().getOrElse(java.util.List.of()).isEmpty()) {
			getDynamicSelectors().set(defaults.getDynamicSelectors());
		}
		if (!getMaxShiftBy().isPresent()) {
			getMaxShiftBy().convention(defaults.getMaxShiftBy());
		}
	}
}