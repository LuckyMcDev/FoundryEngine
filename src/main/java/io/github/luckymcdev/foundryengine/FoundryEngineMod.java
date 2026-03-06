package io.github.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.common.Common;
import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import io.github.luckymcdev.foundryengine.common.data.EngineGenerator;
import io.github.luckymcdev.foundryengine.common.game.DirectWorldLoadBehavior;
import io.github.luckymcdev.foundryengine.common.log.EngineLogAppender;
import io.github.luckymcdev.foundryengine.common.thread.RegisterEngineThreadEvent;
import io.github.luckymcdev.foundryengine.config.Config;
import io.github.luckymcdev.foundryengine.server.command.FoundryCommands;
import io.github.luckymcdev.foundryengine.server.packs.EngineRepositorySource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Main Mod Entrypoint for FoundryEngine.
 */
@Mod(Common.MODID)
public class FoundryEngineMod {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final IEventBus BUS = NeoForge.EVENT_BUS;

    /**
     * Initializes the mod and registers events.
     *
     * @param modEventBus  the mod event bus
     * @param modContainer the mod container
     */
    public FoundryEngineMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.debug("FoundryEngineMod setup called");

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConstruct);
        modEventBus.addListener(this::onAddPackFinders);
        modEventBus.addListener(this::onRegister);

        BUS.post(new RegisterEngineThreadEvent());

        BUS.addListener(this::onAddReloadListeners);
        BUS.addListener(this::onRegisterCommands);

        Common.getGameBehaviorManager().register(Common.id("direct_world_load"),
                new DirectWorldLoadBehavior("testWorld")
        );

        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.Client.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.Common.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.Server.SERVER_SPEC);
        modContainer.registerConfig(ModConfig.Type.STARTUP, Config.Startup.STARTUP_SPEC);
    }

    public void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Common.id("bundle_manager"), Common.getBundleManager());
    }

    private void onAddPackFinders(AddPackFindersEvent event) {
        event.addRepositorySource(new EngineRepositorySource(event.getPackType()));
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        FoundryCommands.registerAll(event.getDispatcher());
    }

    private void onConstruct(final FMLConstructModEvent event) {
        try {
            Common.getBundleManager().discover(Common.BUNDLES);
        } catch (IOException e) {
            LOGGER.error("Error while Loading Bundles: {}", e.getLocalizedMessage());
        }

        EngineLogAppender.Holder.addAppender();
    }

    private void onRegister(RegisterEvent event) {
        for (Bundle bundle : Common.getBundleManager().getBundles()) {
            bundle.bundleBus().post(event);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        try {
            new EngineGenerator().run();
        } catch (IOException e) {
            LOGGER.error("{}{}", e.getLocalizedMessage(), e.getStackTrace());
        }
    }
}
