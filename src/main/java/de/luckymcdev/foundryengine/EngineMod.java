package de.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.command.server.FoundryCommands;
import de.luckymcdev.foundryengine.config.Config;
import de.luckymcdev.foundryengine.config.StartupConfig;
import de.luckymcdev.foundryengine.data.common.gen.BundleDataGenerator;
import de.luckymcdev.foundryengine.event.common.*;
import de.luckymcdev.foundryengine.event.common.modification.BlockModificationEvent;
import de.luckymcdev.foundryengine.event.common.modification.ItemModificationEvent;
import de.luckymcdev.foundryengine.event.common.registry.RegistryEvent;
import de.luckymcdev.foundryengine.network.common.packet.CustomDataPacket;
import de.luckymcdev.foundryengine.network.common.packet.sync.SavedDataSyncPacket;
import de.luckymcdev.foundryengine.registry.common.RegistryCollector;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.CrashReportCallables;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeVersion;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Core FoundryEngine entrypoint — owns only cross-cutting concerns:
 * registry collection, shared packets, block/item modification and saved data.
 */
@Mod(value = Common.MODID)
public class EngineMod {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final IEventBus BUS = NeoForge.EVENT_BUS;
	public static @Nullable ArtifactVersion modVersion;
	private static @Nullable IEventBus modBus;

	public EngineMod(IEventBus modBus, ModContainer modContainer) {
		EngineMod.modBus = modBus;
		EngineMod.modVersion = modContainer.getModInfo().getVersion();

		ClientEvents.Internal.registerModBus(modBus);

		registerInternalEvents();

		modBus.addListener(this::onCommonSetup);
		modBus.addListener(this::onRegisterPayloadHandlers);
		modBus.addListener(EventPriority.LOWEST, this::onLoadComplete);
		modBus.addListener(this::onItemModification);
		modBus.addListener(this::onEngineRegister);
		modBus.addListener(Config::onLoad);
		modBus.addListener(Config::onReload);

		BUS.addListener(this::onRegisterCommands);
		BUS.addListener(this::onServerStarted);
		BUS.addListener(this::onServerStopping);
		BUS.addListener(this::onPlayerChangedDimension);

		registerCrashReportCallables();

		Config.registerCommon(modContainer);
		Config.registerStartup(modContainer);

		if (StartupConfig.CLEAR_DATA_CACHE.get()) {
			try {
				FileUtils.deleteDirectory(BundleDataGenerator.OUTPUT_ROOT.toFile());
			} catch (IOException e) {
				LOGGER.error("Could not clear Data Cache.");
			}
			StartupConfig.CLEAR_DATA_CACHE.set(false);
		}

		LOGGER.info("""
				
				‚Ėą‚Ėą‚Ėą‚Ėą‚Ėą‚Ėą‚Ėą‚ēó‚Ėą‚Ėą‚Ėą‚Ėą‚Ėą‚Ėą‚Ėą‚ēó
				‚Ėą‚Ėą‚ēĒ‚ēź‚ēź‚ēź‚ēź‚ēĚ‚Ėą‚Ėą‚ēĒ‚ēź‚ēź‚ēź‚ēź‚ēĚ  Foundry Engine {}
				‚Ėą‚Ėą‚Ėą‚Ėą‚Ėą‚ēó  ‚Ėą‚Ėą‚Ėą‚Ėą‚Ėą‚ēó    Running on NeoForge {}
				‚Ėą‚Ėą‚ēĒ‚ēź‚ēź‚ēĚ  ‚Ėą‚Ėą‚ēĒ‚ēź‚ēź‚ēĚ    Minecraft {}
				‚Ėą‚Ėą‚ēĎ     ‚Ėą‚Ėą‚Ėą‚Ėą‚Ėą‚Ėą‚Ėą‚ēó  Platform {}
				‚ēö‚ēź‚ēĚ     ‚ēö‚ēź‚ēź‚ēź‚ēź‚ēź‚ēź‚ēĚ""",
			modVersion,
			NeoForgeVersion.getVersion(),
			SharedConstants.getCurrentVersion().name(),
			Util.getPlatform().name());
	}

	@ApiStatus.Internal
	public static @Nullable IEventBus getModBus() {
		return modBus;
	}

	private void registerInternalEvents() {
		BlockEvents.Internal.register(BUS);
		ClientEvents.Internal.register(BUS);
		CommandEvents.Internal.register(BUS);
		EntityEvents.Internal.register(BUS);
		ItemEvents.Internal.register(BUS);
		LevelEvents.Internal.register(BUS);
		NetworkEvents.Internal.register(BUS);
		PlayerEvents.Internal.register(BUS);
		RecipeEvents.Internal.register(BUS);
		ServerEvents.Internal.register(BUS);
		StageEvents.Internal.register(BUS);
		SlotEvents.Internal.register(BUS);
	}

	private void registerCrashReportCallables() {
		CrashReportCallables.registerCrashCallable("FoundryEngine Bundles", () -> {
			var bundles = Common.getBundleManager().getBundles();
			if (bundles.isEmpty()) {
				return "None";
			}
			var sb = new StringBuilder("\n");
			for (var bundle : bundles) {
				var info = bundle.info();
				sb.append("\t\t- ").append(info.id()).append(" v").append(info.versionInfo());
				if (!info.authors().isEmpty()) {
					sb.append(" by ").append(String.join(", ", info.authors()));
				}
				sb.append('\n');
			}
			return sb.toString().stripTrailing();
		}, () -> Common.getBundleManager().anyBundles());

		CrashReportCallables.registerCrashCallable("FoundryEngine Game Sessions", () -> {
			var sessions = Common.getGameManager().getAllSessions();
			if (sessions.isEmpty()) {
				return "None";
			}
			var sb = new StringBuilder("\n");
			for (var session : sessions) {
				sb.append("\t\t- ").append(session.id()).append('\n');
			}
			return sb.toString().stripTrailing();
		}, () -> Common.getGameManager().anySession());
	}

	private void onCommonSetup(FMLCommonSetupEvent event) {
		var network = Common.getNetworkManager();
		network.register(SavedDataSyncPacket.DEFINITION);
		network.register(CustomDataPacket.DEFINITION);
	}

	private void onEngineRegister(RegisterEvent event) {
		if (modBus == null) {
			return;
		}
		RegistryCollector collector = new RegistryCollector();
		Common.setRegistryCollector(collector);
		RegistryEvent registryEvent = new RegistryEvent(event, collector);
		modBus.post(registryEvent);
		BundleEvents.Internal.postRegistry(registryEvent);
	}

	private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
		Common.getNetworkManager().handleRegistration(event);
	}

	private void onLoadComplete(FMLLoadCompleteEvent event) {
		event.enqueueWork(() -> {
			for (Block block : BuiltInRegistries.BLOCK) {
				var e = new BlockModificationEvent(block);
				BUS.post(e);
			}
		});
	}

	private void onItemModification(ModifyDefaultComponentsEvent event) {
		ItemModificationEvent.bind(event);
		for (Item item : BuiltInRegistries.ITEM) {
			var e = new ItemModificationEvent(item);
			BUS.post(e);
		}
		ItemModificationEvent.flush();
	}

	private void onRegisterCommands(RegisterCommandsEvent event) {
		FoundryCommands.registerAll(event.getDispatcher(), event.getBuildContext());
	}

	private void onServerStarted(ServerStartedEvent event) {
		Common.getSavedDataManager().syncToAll();
	}

	private void onServerStopping(ServerStoppingEvent event) {
		Common.getSavedDataManager().save();
	}

	private void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer) {
			Common.getSavedDataManager().syncToAll();
		}
	}
}