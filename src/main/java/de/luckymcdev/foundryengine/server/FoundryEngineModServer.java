package de.luckymcdev.foundryengine.server;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.config.Config;
import de.luckymcdev.foundryengine.server.command.FoundryCommands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

/**
 * Dedicated Server Mod Entrypoint for FoundryEngine.
 */
@Mod(value = Common.MODID, dist = Dist.DEDICATED_SERVER)
public class FoundryEngineModServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final IEventBus BUS = NeoForge.EVENT_BUS;

    public FoundryEngineModServer(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener(this::onServerSetup);

        BUS.addListener(this::onAddReloadListeners);
        BUS.addListener(this::onRegisterCommands);
        BUS.addListener(Common.getGameStageHandler()::onPlayerTick);

        Config.registerServer(modContainer);

        LOGGER.debug("FoundryEngineModServer initialized");
    }

    private void onServerSetup(FMLDedicatedServerSetupEvent event) {
        LOGGER.debug("FoundryEngineModServer setup called");
    }

    private void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Common.id("bundle_manager"), Common.getBundleManager());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        FoundryCommands.registerAll(event.getDispatcher(), event.getBuildContext());
    }
}