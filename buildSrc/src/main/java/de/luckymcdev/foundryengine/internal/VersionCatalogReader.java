package de.luckymcdev.foundryengine.internal;

import org.gradle.api.Project;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.api.artifacts.VersionCatalog;
import org.gradle.api.artifacts.VersionCatalogsExtension;

import java.util.Optional;

/**
 * Reads the project's {@code libs} version catalog and falls back to
 * {@code gradle.properties} for keys that are not yet declared there.
 */
public class VersionCatalogReader {

	private final Project project;
	private final VersionCatalog catalog;

	public VersionCatalogReader(Project project) {
		this.project = project;
		VersionCatalogsExtension catalogs = project.getExtensions().findByType(VersionCatalogsExtension.class);
		this.catalog = catalogs != null ? catalogs.find("libs").orElse(null) : null;
	}

	/**
	 * @param propertyName {@code gradle.properties} key
	 * @param catalogName  catalog version alias, or {@code null} to skip the catalog
	 * @return the value from the catalog, else from {@code gradle.properties}, else {@code null}
	 */
	public String value(String propertyName, String catalogName) {
		if (catalogName != null && catalog != null) {
			Optional<String> version = catalog.findVersion(catalogName).map(vc -> vc.getRequiredVersion());
			if (version.isPresent() && !version.get().isEmpty()) {
				return version.get();
			}
		}
		return project.getProviders().gradleProperty(propertyName).getOrNull();
	}

	/**
	 * Looks up a library by catalog alias, falling back to the raw {@code group:artifact[:version]} string.
	 */
	public Optional<String> libraryCoordinate(String catalogName) {
		if (catalog == null || catalogName == null) {
			return Optional.empty();
		}
		return catalog.findLibrary(catalogName)
			.flatMap(provider -> Optional.ofNullable(provider.getOrNull()))
			.map(this::coordinate);
	}

	/**
	 * Looks up the version of a catalog library, e.g. the {@code junit} alias.
	 */
	public Optional<String> libraryVersion(String catalogName) {
		if (catalog == null || catalogName == null) {
			return Optional.empty();
		}
		return catalog.findLibrary(catalogName)
			.flatMap(provider -> Optional.ofNullable(provider.getOrNull()))
			.map(dep -> dep.getVersionConstraint().getRequiredVersion());
	}

	/**
	 * Looks up a bundle by catalog alias, returning all library coordinates it contains.
	 */
	public java.util.List<String> bundleCoordinates(String catalogName) {
		if (catalog == null || catalogName == null) {
			return java.util.List.of();
		}
		return catalog.findBundle(catalogName)
			.map(bundle -> bundle.get().stream().map(this::coordinate).toList())
			.orElse(java.util.List.of());
	}

	private String coordinate(MinimalExternalModuleDependency dep) {
		String version = dep.getVersionConstraint().getRequiredVersion();
		return dep.getModule().getGroup() + ":" + dep.getModule().getName()
			+ (version == null || version.isEmpty() ? "" : ":" + version);
	}

	public Project getProject() {
		return project;
	}
}