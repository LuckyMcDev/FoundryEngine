package de.luckymcdev.foundryengine.common.bundle.modcompat;

import com.google.common.collect.ImmutableMap;
import de.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import net.neoforged.fml.jarcontents.EmptyJarContents;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.loading.LogMarkers;
import net.neoforged.fml.loading.modscan.ModClassVisitor;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class BundleModFile implements IModFile {
	private final String id;
	private final Path filePath;
	private final JarContents contents;
	private IModFileInfo modFileInfo;

	public BundleModFile(BundleInfo bundleInfo, Path filePath) {
		this.id = bundleInfo.id();
		this.filePath = filePath;
		this.contents = new EmptyJarContents(filePath);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public JarContents getContents() {
		return contents;
	}

	@Override
	public Supplier<Map<String, Object>> getSubstitutionMap() {
		return ImmutableMap::of;
	}

	@Override
	public Type getType() {
		return Type.MOD;
	}

	@Override
	public Path getFilePath() {
		return filePath;
	}

	@Override
	public List<IModInfo> getModInfos() {
		return modFileInfo != null ? modFileInfo.getMods() : List.of();
	}

	@Override
	public ModFileScanData getScanResult() {
		ModFileScanData result = new ModFileScanData();
		result.addModFileInfo(modFileInfo);
		return result;
	}

	@Override
	public String getFileName() {
		return filePath.getFileName().toString();
	}

	@Override
	public ModFileDiscoveryAttributes getDiscoveryAttributes() {
		return ModFileDiscoveryAttributes.DEFAULT;
	}

	@Override
	public IModFileInfo getModFileInfo() {
		return modFileInfo;
	}

	public void setModFileInfo(IModFileInfo modFileInfo) {
		this.modFileInfo = modFileInfo;
	}
}
