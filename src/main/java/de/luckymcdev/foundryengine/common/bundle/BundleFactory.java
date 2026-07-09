package de.luckymcdev.foundryengine.common.bundle;

import de.luckymcdev.foundryengine.common.bundle.config.BundleConfig;
import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import de.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import de.luckymcdev.foundryengine.common.bundle.registry.BundleCreativeModeTab;
import de.luckymcdev.foundryengine.common.script.GroovyBundleScriptEngine;
import de.luckymcdev.foundryengine.common.script.GroovyScriptLoader;
import net.neoforged.bus.api.IEventBus;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

public class BundleFactory {

	private final GroovyScriptLoader scriptLoader;
	private final IEventBus modBus;
	private final Path configDirectory;

	public BundleFactory(IEventBus modBus, Path configDirectory) {
		this.scriptLoader = new GroovyScriptLoader();
		this.modBus = modBus;
		this.configDirectory = configDirectory;
	}

	public Bundle createBundle(BundleInfo info, Path bundleDir, @Nullable FileSystem zipFs) throws IOException {
		BundleFiles files = BundleFiles.builder().build(bundleDir, zipFs);

		GroovyBundleScriptEngine engine = new GroovyBundleScriptEngine();
		engine.initialize(files);

		BundleCreativeModeTab creativeTab = new BundleCreativeModeTab(info.id(), modBus);
		BundleConfig config = new BundleConfig(info.id(), configDirectory);

		return new Bundle(info, files, engine, creativeTab, config);
	}

	public GroovyScriptLoader getScriptLoader() {
		return scriptLoader;
	}
}