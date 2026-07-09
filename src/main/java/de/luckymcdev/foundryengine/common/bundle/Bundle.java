package de.luckymcdev.foundryengine.common.bundle;

import de.luckymcdev.foundryengine.common.bundle.config.BundleConfig;
import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import de.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import de.luckymcdev.foundryengine.common.bundle.registry.BundleCreativeModeTab;
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint;
import de.luckymcdev.foundryengine.common.script.GroovyBundleScriptEngine;
import de.luckymcdev.foundryengine.common.script.GroovyScriptLoader;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Bundle {
	private final BundleInfo info;
	private final BundleFiles bundleFiles;
	private final GroovyBundleScriptEngine scriptEngine;
	private final BundleCreativeModeTab creativeModeTab;
	private final BundleConfig bundleConfig;
	private final List<BundleEntrypoint> commonEntrypoints = new ArrayList<>();
	private final List<BundleEntrypoint> clientEntrypoints = new ArrayList<>();
	private final List<BundleEntrypoint> serverEntrypoints = new ArrayList<>();

	public Bundle(BundleInfo info, BundleFiles bundleFiles, GroovyBundleScriptEngine scriptEngine,
	              BundleCreativeModeTab creativeModeTab, BundleConfig bundleConfig) {
		this.info = info;
		this.bundleFiles = bundleFiles;
		this.scriptEngine = scriptEngine;
		this.creativeModeTab = creativeModeTab;
		this.bundleConfig = bundleConfig;
	}

	public void loadCommon(GroovyScriptLoader loader) {
		commonEntrypoints.addAll(loader.loadCommon(bundleFiles, scriptEngine, info.id()));
	}

	public void loadClient(GroovyScriptLoader loader) {
		clientEntrypoints.addAll(loader.loadClient(bundleFiles, scriptEngine, info.id()));
	}

	public void loadServer(GroovyScriptLoader loader) {
		serverEntrypoints.addAll(loader.loadServer(bundleFiles, scriptEngine, info.id()));
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

	public void unload() {
		entrypoints().forEach(BundleEntrypoint::onUnload);
		scriptEngine.close();
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

	public BundleConfig bundleConfig() {
		return bundleConfig;
	}

	public GroovyBundleScriptEngine scriptEngine() {
		return scriptEngine;
	}

	public BundleCreativeModeTab creativeModeTab() {
		return creativeModeTab;
	}

	public Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(info.id(), path);
	}
}