package de.luckymcdev.foundryengine.dsl;

import org.gradle.api.Named;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * A single mixin configuration. The relative file name is derived from the
 * feature name: {@code mixins/<modId>[.<feature>].mixins.json}.
 *
 * <p>An empty feature name generates the base config {@code mixins/<modId>.mixins.json}.
 */
public abstract class MixinConfig extends MixinOptions implements Named {

	private final String name;

	@Inject
	public MixinConfig(String name, ObjectFactory objects) {
		super(objects);
		this.name = name;
	}

	public static String fileName(String modId, String feature) {
		if (feature == null || feature.isEmpty()) {
			return "mixins/" + modId + ".mixins.json";
		}
		return "mixins/" + modId + "." + feature + ".mixins.json";
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * The package in which this config's mixins live. Empty to derive from the base package + feature.
	 */
	public abstract Property<String> getPackage();

	/**
	 * Computed package: the configured one, or the base package suffixed with the feature.
	 */
	public String resolvePackage(String basePackage) {
		String pkg = getPackage().getOrNull();
		if (pkg != null && !pkg.isEmpty()) {
			return pkg;
		}
		if (name == null || name.isEmpty()) {
			return basePackage;
		}
		return basePackage + "." + name;
	}
}