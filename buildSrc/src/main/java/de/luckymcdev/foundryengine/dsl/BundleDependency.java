package de.luckymcdev.foundryengine.dsl;

import org.gradle.api.Named;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * A library bundled into the mod via {@code jarJar(api(implementation(...)))}.
 */
public abstract class BundleDependency implements Named {

	private final String name;

	@Inject
	public BundleDependency(String name) {
		this.name = name;
		getVersion().convention("");
		getExcludes().convention(java.util.List.of());
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * The {@code group:artifact} (or full {@code group:artifact:version}) coordinate.
	 */
	public abstract Property<String> getCoordinate();

	/**
	 * Optional version. Falls back to the version embedded in {@link #getCoordinate()}.
	 */
	public abstract Property<String> getVersion();

	/**
	 * {@code group:artifact} pairs to exclude transitively.
	 */
	public abstract ListProperty<String> getExcludes();

	public void exclude(String group, String artifact) {
		getExcludes().add(group + ":" + artifact);
	}

	public void exclude(String group) {
		getExcludes().add(group);
	}
}