package io.github.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.common.Commons;
import io.github.luckymcdev.foundryengine.common.Instances;
import io.github.luckymcdev.foundryengine.common.opencl.ClDispatch;
import io.github.luckymcdev.foundryengine.common.thread.RegisterEngineThreadEvent;
import io.github.luckymcdev.foundryengine.config.Config;
import io.github.luckymcdev.foundryengine.server.packs.EngineRepositorySource;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Main Mod Entrypoint for FoundryEngine.
 */
@Mod(Commons.MODID)
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

        NeoForge.EVENT_BUS.register(this);
        Instances.post(new RegisterEngineThreadEvent());

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
    }

    private void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES || event.getPackType() == PackType.SERVER_DATA) {
            event.addRepositorySource(new EngineRepositorySource(event.getPackType()));
        }
    }

    private void onConstruct(final FMLConstructModEvent event) {
        try {
            Instances.getBundleManager().discover(Commons.BUNDLES);
        } catch (IOException e) {
            LOGGER.error("Error while Loading Bundles: {}", e.getLocalizedMessage());
        }

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        try {
            Instances.getFileManager().createMainDirectory();
        } catch (IOException e) {
            LOGGER.error("{}{}", e.getLocalizedMessage(), e.getStackTrace());
        }

        Instances.getThreadManager().execute(ClDispatch.CL_THREAD, () -> {
            //OpenClExample.visualize(15630, 8640, 300.0f, 32, false);
        });
    }

    @SubscribeEvent
    public void onRegisterEngineThread(RegisterEngineThreadEvent event) {
        event.register(ClDispatch.CL_THREAD);
    }
}
