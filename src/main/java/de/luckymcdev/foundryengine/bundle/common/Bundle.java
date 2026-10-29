package de.luckymcdev.foundryengine.bundle.common;

import de.luckymcdev.foundryengine.bundle.common.config.BundleConfigSpec;
import de.luckymcdev.foundryengine.bundle.common.info.BundleFiles;
import de.luckymcdev.foundryengine.bundle.common.info.BundleInfo;
import de.luckymcdev.foundryengine.bundle.common.registry.BundleCreativeModeTab;
import de.luckymcdev.foundryengine.script.common.BundleEntrypoint;
import de.luckymcdev.foundryengine.script.common.ScriptManager;
import de.luckymcdev.foundryengine.script.common.definition.ScriptEnv;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Bundle {
	private final BundleInfo info;
	private final BundleFiles bundleFiles;
	private final BundleCreativeModeTab creativeModeTab;
	private final BundleConfigSpec configSpec;
	private final List<BundleEntrypoint> commonEntrypoints = new ArrayList<>();
	private final List<BundleEntrypoint> clientEntrypoints = new ArrayList<>();
	private final List<BundleEntrypoint> serverEntrypoints = new ArrayList<>();

	public Bundle(BundleInfo info, BundleFiles bundleFiles,
				  BundleCreativeModeTab creativeModeTab, BundleConfigSpec configSpec) {
		this.info = info;
		this.bundleFiles = bundleFiles;
		this.creativeModeTab = creativeModeTab;
		this.configSpec = configSpec;
	}

	public void loadCommon(ScriptManager manager) {
		commonEntrypoints.addAll(manager.load(info.id(), bundleFiles, ScriptEnv.COMMON));
	}

	public void loadClient(ScriptManager manager) {
		clientEntrypoints.addAll(manager.load(info.id(), bundleFiles, ScriptEnv.CLIENT));
	}

	public void loadServer(ScriptManager manager) {
		serverEntrypoints.addAll(manager.load(info.id(), bundleFiles, ScriptEnv.SERVER));
	}

	public List<BundleEntrypoint> entrypoints() {
		return Stream.of(commonEntrypoints, clientEntrypoints, serverEntrypoints)
			.flatMap(List::stream)
			.toList();
	}

	public List<BundleEntrypoint> commonEntrypoints() {
		return Collections.unmodifiableList(commonEntrypoints);
	}

	public List<BundleEntrypoint> clientEntrypoints() {
		return Collections.unmodifiableList(clientEntrypoints);
	}

	public List<BundleEntrypoint> serverEntrypoints() {
		return Collections.unmodifiableList(serverEntrypoints);
	}

	/**
	 * Clears the cached entrypoint references. Unloading (calling {@code onUnload} and closing
	 * the class loader) is owned by {@link ScriptManager} — callers must go through
	 * {@code ScriptManager.unloadBundle(id)}.
	 */
	public void unload() {
		commonEntrypoints.clear();
		clientEntrypoints.clear();
		serverEntrypoints.clear();
	}

	public BundleInfo info() {
		return info;
	}

	public BundleFiles bundleFiles() {
		return bundleFiles;
	}

	public BundleConfigSpec configSpec() {
		return configSpec;
	}

	public BundleCreativeModeTab creativeModeTab() {
		return creativeModeTab;
	}

	public Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(info.id(), path);
	}
}