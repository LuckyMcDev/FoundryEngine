package de.luckymcdev.foundryengine.bundle;

import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.bundle.common.BundleManager;
import de.luckymcdev.foundryengine.bundle.common.modcompat.BundleModContainer;
import de.luckymcdev.foundryengine.data.common.gen.BundleDataGenerator;
import de.luckymcdev.foundryengine.event.common.BundleEvents;
import de.luckymcdev.foundryengine.network.common.packet.BundleHashPacket;
import de.luckymcdev.foundryengine.pack.server.DynamicPackRepository;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Mod(BundleMod.MODID)
public final class BundleMod {
	public static final String MODID = "foundryengine_bundle";
	private static final Logger LOGGER = LogUtils.getLogger();

	public BundleMod(IEventBus modBus, ModContainer modContainer) {
		BundleEvents.Internal.register(NeoForge.EVENT_BUS);
		modBus.addListener(this::onConstruct);
		modBus.addListener(this::onCommonSetup);
		modBus.addListener(this::onClientSetup);
		modBus.addListener(this::onDedicatedServerSetup);
		modBus.addListener(this::onPostInit);
		modBus.addListener(this::onAddPackFinders);
		var bus = NeoForge.EVENT_BUS;
		bus.addListener(this::onServerAboutToStart);
		bus.addListener(this::onServerStarting);
		bus.addListener(this::onServerStopping);
	}

	private void onConstruct(FMLConstructModEvent event) {
		try {
			Common.getBundleManager().discover(Common.BUNDLES);
			Common.getBundleManager().refreshModList();
			Common.getBundleManager().reportScriptErrors();
		} catch (IOException e) {
			LOGGER.error("Error while loading bundles", e);
		}
	}

	private void onCommonSetup(FMLCommonSetupEvent event) {
		BundleEvents.Internal.postCommonSetup(event);
		BundleDataGenerator.runAll();

		Common.getNetworkManager().register(BundleHashPacket.DEFINITION);
	}

	private void onClientSetup(FMLClientSetupEvent event) {
		BundleEvents.Internal.postClientSetup(event);
	}

	private void onDedicatedServerSetup(FMLDedicatedServerSetupEvent event) {
		BundleEvents.Internal.postDedicatedServerSetup(event);
	}

	private void onPostInit(InterModProcessEvent event) {
		BundleEvents.Internal.postPostInit(event);
	}

	private void onAddPackFinders(AddPackFindersEvent event) {
		PackType type = event.getPackType();
		var bundles = new DynamicPackRepository(type, "foundryengine/bundles", "FoundryEngine: Bundles",
			"Foundry Engine Bundle Resource Files",
			() -> Common.getBundleManager().getBundles().stream()
				.map(b -> type == PackType.CLIENT_RESOURCES ? b.bundleFiles().assets() : b.bundleFiles().data())
				.filter(Files::exists).toList(), Pack.Position.TOP, false);
		Path generatedPath = type == PackType.CLIENT_RESOURCES
			? BundleDataGenerator.getGeneratedAssetsPath() : BundleDataGenerator.getGeneratedDataPath();
		try {
			Files.createDirectories(generatedPath);
		} catch (IOException e) {
			LOGGER.error("Could not create generated bundle directory {}", generatedPath, e);
		}
		var generated = new DynamicPackRepository(type, "foundryengine/bundles_generated", "FoundryEngine: Generated",
			"Foundry Engine Generated Resource Files", () -> List.of(generatedPath), Pack.Position.BOTTOM, true);
		event.addRepositorySource(bundles);
		event.addRepositorySource(generated);
	}

	private void onServerAboutToStart(ServerAboutToStartEvent event) {
		Common.getBundleManager().setServer(event.getServer());
	}

	private void onServerStarting(ServerStartingEvent event) {
		Common.getBundleManager().loadServerScripts();
		Common.getBundleManager().reportScriptErrors();
	}

	private void onServerStopping(ServerStoppingEvent event) {
		Common.getBundleManager().setServer(null);
	}
}
