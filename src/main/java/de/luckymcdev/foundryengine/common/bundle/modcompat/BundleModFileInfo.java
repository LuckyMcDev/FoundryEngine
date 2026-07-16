package de.luckymcdev.foundryengine.common.bundle.modcompat;

import de.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import net.neoforged.neoforgespi.language.IConfigurable;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;

import java.util.List;
import java.util.Map;

public class BundleModFileInfo implements IModFileInfo {

	private final BundleModInfo modInfo;
	private final BundleInfo bundleInfo;
	private final IConfigurable config;

	public BundleModFileInfo(BundleModInfo modInfo, BundleInfo bundleInfo, IConfigurable config) {
		this.modInfo = modInfo;
		this.bundleInfo = bundleInfo;
		this.config = config;
	}

	@Override
	public List<IModInfo> getMods() {
		return List.of(modInfo);
	}

	@Override
	public List<LanguageSpec> requiredLanguageLoaders() {
		return List.of();
	}

	@Override
	public boolean showAsResourcePack() {
		return false;
	}

	@Override
	public boolean showAsDataPack() {
		return false;
	}

	@Override
	public Map<String, Object> getFileProperties() {
		return Map.of();
	}

	@Override
	public String getLicense() {
		return "Bundle";
	}

	@Override
	public String versionString() {
		return bundleInfo.versionInfo().toString();
	}

	@Override
	public List<String> usesServices() {
		return List.of();
	}

	@Override
	public IModFile getFile() {
		return null;
	}

	@Override
	public IConfigurable getConfig() {
		return config;
	}
}
