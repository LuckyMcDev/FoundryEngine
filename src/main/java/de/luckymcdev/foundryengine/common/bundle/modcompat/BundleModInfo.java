package de.luckymcdev.foundryengine.common.bundle.modcompat;

import de.luckymcdev.foundryengine.common.bundle.info.BundleDependency;
import de.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import net.neoforged.neoforgespi.language.IConfigurable;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.IModLanguageLoader;
import net.neoforged.neoforgespi.locating.ForgeFeature;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BundleModInfo implements IModInfo, IConfigurable {

	private final BundleInfo bundleInfo;
	private final ArtifactVersion version;
	private BundleModFileInfo owningFile;

	public BundleModInfo(BundleInfo bundleInfo, BundleModFileInfo owningFile) {
		this.bundleInfo = bundleInfo;
		this.owningFile = owningFile;
		this.version = new DefaultArtifactVersion(bundleInfo.versionInfo().toString());
	}

	@Override
	public IModFileInfo getOwningFile() {
		return owningFile;
	}

	public void setOwningFile(BundleModFileInfo owningFile) {
		this.owningFile = owningFile;
	}

	@Override
	public IModLanguageLoader getLoader() {
		return null;
	}

	@Override
	public String getModId() {
		return bundleInfo.id();
	}

	@Override
	public String getDisplayName() {
		return bundleInfo.displayName() + " - B";
	}

	@Override
	public String getDescription() {
		return "Bundle: " + bundleInfo.displayName();
	}

	@Override
	public ArtifactVersion getVersion() {
		return version;
	}

	@Override
	public List<? extends ModVersion> getDependencies() {
		return bundleInfo.dependencies().stream()
			.map(dep -> new BundleModVersion(this, dep))
			.toList();
	}

	@Override
	public List<? extends ForgeFeature.Bound> getForgeFeatures() {
		return List.of();
	}

	@Override
	public String getNamespace() {
		return bundleInfo.id();
	}

	@Override
	public Map<String, Object> getModProperties() {
		return Map.of();
	}

	@Override
	public Optional<URL> getUpdateURL() {
		return Optional.empty();
	}

	@Override
	public Optional<URL> getModURL() {
		return Optional.empty();
	}

	@Override
	public Optional<String> getLogoFile() {
		return Optional.empty();
	}

	@Override
	public boolean getLogoBlur() {
		return true;
	}

	@Override
	public IConfigurable getConfig() {
		return this;
	}

	@Override
	public <T> Optional<T> getConfigElement(String... key) {
		return Optional.empty();
	}

	@Override
	public List<? extends IConfigurable> getConfigList(String... key) {
		return List.of();
	}

	private record BundleModVersion(IModInfo owner, BundleDependency dep) implements ModVersion {
		@Override
		public String getModId() {
			return dep.id();
		}

		@Override
		public VersionRange getVersionRange() {
			return IModInfo.UNBOUNDED;
		}

		@Override
		public DependencyType getType() {
			return DependencyType.REQUIRED;
		}

		@Override
		public Optional<String> getReason() {
			return Optional.empty();
		}

		@Override
		public Ordering getOrdering() {
			return Ordering.NONE;
		}

		@Override
		public DependencySide getSide() {
			return DependencySide.BOTH;
		}

		@Override
		public IModInfo getOwner() {
			return owner;
		}

		@Override
		public void setOwner(IModInfo owner) {
		}

		@Override
		public Optional<URL> getReferralURL() {
			return Optional.empty();
		}
	}
}
