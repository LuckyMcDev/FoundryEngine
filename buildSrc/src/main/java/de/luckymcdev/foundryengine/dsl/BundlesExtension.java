package de.luckymcdev.foundryengine.dsl;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Declares libraries bundled into the mod and the example-bundle copy paths.
 */
public abstract class BundlesExtension {

	@Inject
	public BundlesExtension() {
		getExampleBundlesSource().convention("ExampleBundles");
		getBundlesSubPath().convention("FoundryEngine/bundles");
	}

	/**
	 * Bundled libraries, applied as {@code jarJar(api(implementation(...)))}.
	 */
	public abstract NamedDomainObjectContainer<BundleDependency> getDependencies();

	/**
	 * Source directory of the example bundles (relative to the project root).
	 */
	public abstract Property<String> getExampleBundlesSource();

	/**
	 * Sub-path under each run directory where bundles are copied.
	 */
	public abstract Property<String> getBundlesSubPath();

	/**
	 * Adds a bundled library, e.g. {@code include('org.apache.groovy:groovy')}.
	 */
	public void include(String coordinate) {
		includeWithVersion(coordinate, null);
	}

	/**
	 * Adds a bundled library with an explicit version, e.g. {@code include('org.apache.groovy:groovy', '5.0.4')}.
	 */
	public void include(String coordinate, String version) {
		includeWithVersion(coordinate, version);
	}

	/**
	 * Adds a bundled library whose version comes from a provider, e.g. {@code include('a:b', libs.versions.groovy)}.
	 */
	public void include(String coordinate, Provider<String> version) {
		includeWithVersion(coordinate, version.get());
	}

	/**
	 * Adds a bundled library with exclusions, e.g. {@code include('io.github.spair:imgui-java-lwjgl3') { exclude 'org.lwjgl' } }.
	 */
	public void include(String coordinate, Consumer<BundleDependency> action) {
		action.accept(byCoordinate(coordinate));
	}

	/**
	 * Adds a bundled library with a provider version and exclusions.
	 */
	public void include(String coordinate, Provider<String> version, Action<? super BundleDependency> action) {
		includeWithVersion(coordinate, version.get());
		action.execute(byCoordinate(coordinate));
	}

	private BundleDependency byCoordinate(String coordinate) {
		BundleDependency dep = getDependencies().findByName(coordinate);
		if (dep == null) {
			dep = getDependencies().create(coordinate);
			dep.getCoordinate().set(coordinate);
		}
		return dep;
	}

	private void includeWithVersion(String coordinate, String version) {
		BundleDependency dep = byCoordinate(coordinate);
		if (version != null) {
			dep.getVersion().set(version);
		}
	}
}