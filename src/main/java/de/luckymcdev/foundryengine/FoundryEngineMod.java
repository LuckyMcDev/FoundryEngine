package de.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.api.event.BundleEvents;
import de.luckymcdev.foundryengine.api.event.ClientEvents;
import de.luckymcdev.foundryengine.api.event.ServerEvents;
import de.luckymcdev.foundryengine.api.event.registry.RegistryEvent;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.log.EngineLogAppender;
import de.luckymcdev.foundryengine.common.network.TestPacket;
import de.luckymcdev.foundryengine.common.network.packets.ServerBoundChangeWeatherPacket;
import de.luckymcdev.foundryengine.common.network.packets.ServerBoundSetTimePacket;
import de.luckymcdev.foundryengine.common.network.packets.ServerBoundTeleportPacket;
import de.luckymcdev.foundryengine.common.network.packets.explorer.*;
import de.luckymcdev.foundryengine.common.registry.EngineRegistries;
import de.luckymcdev.foundryengine.common.vpacks.BundleVirtualPacks;
import de.luckymcdev.foundryengine.common.vpacks.event.RegisterVirtualPackEvent;
import de.luckymcdev.foundryengine.config.Config;
import de.luckymcdev.foundryengine.server.command.FoundryCommands;
import de.luckymcdev.foundryengine.server.packs.EngineRepositorySource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Main Mod Entrypoint for FoundryEngine.
 */
@Mod(Common.MODID)
public class FoundryEngineMod {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final IEventBus BUS = NeoForge.EVENT_BUS;
    public static ArtifactVersion modVersion;
    private static @Nullable IEventBus modBus;

    /**
     * Initializes the mod and registers events.
     *
     * @param modBus       the mod event bus
     * @param modContainer the mod container
     */
    public FoundryEngineMod(IEventBus modBus, ModContainer modContainer) {
        FoundryEngineMod.modBus = modBus;
        FoundryEngineMod.modVersion = modContainer.getModInfo().getVersion();

        registerModBus(modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::onConstruct);
        modBus.addListener(this::onAddPackFinders);
        modBus.addListener(this::onRegisterPayloadHandlers);
        BUS.addListener(this::onRegisterCommands);

        BUS.addListener(this::onRegisterVirtualPacks);
        BUS.addListener(Common.getSceneManager()::entityJoinLevel);
        BUS.addListener(Common.getSceneManager()::entityLeaveLevel);

        BUS.addListener(BundleEvents::_postVanillaGame);

        BUS.addListener(ServerEvents::_postAboutToStart);
        BUS.addListener(ServerEvents::_postStarting);
        BUS.addListener(ServerEvents::_postStarted);
        BUS.addListener(ServerEvents::_postStopping);
        BUS.addListener(ServerEvents::_postStopped);
        BUS.addListener(ServerEvents::_postTick);

        BUS.addListener(ClientEvents::_postTick);
        BUS.addListener(ClientEvents::_postStopped);
        BUS.addListener(ClientEvents::_postStopping);
        BUS.addListener(ClientEvents::_postChat);
        BUS.addListener(ClientEvents::_postRenderGui);
        BUS.addListener(ClientEvents::_postRenderGuiLayer);
        BUS.addListener(ClientEvents::_postRenderHand);
        BUS.addListener(ClientEvents::_postRenderAfterLevel);
        modBus.addListener(ClientEvents::_postKeyMappings);

        Config.registerCommon(modContainer);
        Config.registerStartup(modContainer);
        LOGGER.debug("Foundry Engine version {} initialized", modVersion);
    }

    @ApiStatus.Internal
    public static IEventBus getModBus() {
        return modBus;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

        //Common.getGameBehaviorManager().register(Common.id("direct_world_load"),
        //        new DirectWorldLoadBehavior("testWorld")
        //);

        //Common.getGameStageHandler().dimensions().requireStages(Level.END, "end");
        //Common.getGameStageHandler().item().requireStages(Items.STICK, "stick");
        //Common.getGameStageHandler().loot().requireStages(BuiltInLootTables.END_CITY_TREASURE, "mineshaft");
        //Common.getGameStageHandler().mobs().requireStages(EntityType.ZOMBIE, "zombie");

        Common.getNetworkManager().register(TestPacket.DEFINITION);
        Common.getNetworkManager().register(ServerBoundSetTimePacket.DEFINITION);
        Common.getNetworkManager().register(ServerBoundChangeWeatherPacket.DEFINITION);
        Common.getNetworkManager().register(ClientBoundFileListPacket.DEFINITION);
        Common.getNetworkManager().register(ClientBoundFileContentPacket.DEFINITION);
        Common.getNetworkManager().register(ServerBoundRequestFileListPacket.DEFINITION);
        Common.getNetworkManager().register(ServerBoundRequestFileContentPacket.DEFINITION);
        Common.getNetworkManager().register(ServerBoundSaveFilePacket.DEFINITION);
        Common.getNetworkManager().register(ServerBoundTeleportPacket.DEFINITION);
    }

    private void registerModBus(IEventBus modBus) {
        Common.getGameStageHandler().register(modBus);
        EngineRegistries.register(modBus);
    }

    private void onAddPackFinders(AddPackFindersEvent event) {
        event.addRepositorySource(new EngineRepositorySource(event.getPackType()));
    }

    private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        Common.getNetworkManager().handleRegistration(event);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        FoundryCommands.registerAll(event.getDispatcher(), event.getBuildContext());
    }

    private void onRegisterVirtualPacks(RegisterVirtualPackEvent.BeforeUser event) {
        BundleVirtualPacks.create().forEach(event::addPack);
    }

    private void onConstruct(final FMLConstructModEvent event) {
        try {
            Common.getBundleManager().discover(Common.BUNDLES);
            if (modBus == null) return;
            modBus.addListener((RegisterEvent ev) -> {
                RegistryEvent registryEvent = new RegistryEvent(ev, modBus);
                ModLoader.postEvent(registryEvent);
                BundleEvents._postRegistry(registryEvent);
            });
        } catch (IOException e) {
            LOGGER.error("Error while Loading Bundles: {}", e.getLocalizedMessage());
        }

        EngineLogAppender.Holder.addAppender();
    }
}