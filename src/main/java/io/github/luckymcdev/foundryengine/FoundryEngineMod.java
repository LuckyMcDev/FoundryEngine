package io.github.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.common.Common;
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
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Main Mod Entrypoint for FoundryEngine.
 */
@Mod(Common.MODID)
public class FoundryEngineMod {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Initializes the mod and registers events.
     *
     * @param modEventBus  the mod event bus
     * @param modContainer the mod container
     */
    public FoundryEngineMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener(this::onConstruct);

        modEventBus.addListener(this::onAddPackFinders);

        Common.post(new RegisterEngineThreadEvent());

        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
    }

    public void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Common.id("bundlemanager"), Common.getBundleManager());
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

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        try {
            Common.getFileManager().createMainDirectory();
        } catch (IOException e) {
            LOGGER.error("{}{}", e.getLocalizedMessage(), e.getStackTrace());
        }
    }
}
