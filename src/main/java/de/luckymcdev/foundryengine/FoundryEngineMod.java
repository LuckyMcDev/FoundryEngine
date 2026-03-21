package de.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.game.behavior.DirectWorldLoadBehavior;
import de.luckymcdev.foundryengine.common.log.EngineLogAppender;
import de.luckymcdev.foundryengine.common.registry.EngineRegistries;
import de.luckymcdev.foundryengine.common.thread.RegisterEngineThreadEvent;
import de.luckymcdev.foundryengine.common.vpacks.BundleVirtualPacks;
import de.luckymcdev.foundryengine.common.vpacks.event.RegisterVirtualPackEvent;
import de.luckymcdev.foundryengine.config.Config;
import de.luckymcdev.foundryengine.server.command.FoundryCommands;
import de.luckymcdev.foundryengine.server.packs.EngineRepositorySource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Main Mod Entrypoint for FoundryEngine.
 */
@Mod(Common.MODID)
public class FoundryEngineMod {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final IEventBus BUS = NeoForge.EVENT_BUS;
    private static IEventBus MODBUS;

    /**
     * Initializes the mod and registers events.
     *
     * @param modBus  the mod event bus
     * @param modContainer the mod container
     */
    public FoundryEngineMod(IEventBus modBus, ModContainer modContainer) {
        LOGGER.debug("FoundryEngineMod setup called");
        MODBUS = modBus;

        registerModBus(modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::onConstruct);
        modBus.addListener(this::onAddPackFinders);

        BUS.post(new RegisterEngineThreadEvent());

        BUS.addListener(this::onAddReloadListeners);
        BUS.addListener(this::onRegisterCommands);
        BUS.addListener(Common.getGameStageHandler()::onPlayerTick);

        BUS.addListener(this::onRegisterVirtualPacks);

        Common.getGameBehaviorManager().register(Common.id("direct_world_load"),
                new DirectWorldLoadBehavior("testWorld")
        );

        Common.getGameStageHandler().dimensions().requireStages(Level.END, "end");
        Common.getGameStageHandler().item().requireStages(Items.STICK, "stick");
        Common.getGameStageHandler().loot().requireStages(BuiltInLootTables.END_CITY_TREASURE, "mineshaft");
        Common.getGameStageHandler().mobs().requireStages(EntityType.ZOMBIE, "zombie");

        Config.registerOthers(modContainer);
    }

    private void registerModBus(IEventBus modBus) {
        Common.getGameStageHandler().register(modBus);
        EngineRegistries.register(modBus);
    }

    private void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Common.id("bundle_manager"), Common.getBundleManager());
    }

    private void onAddPackFinders(AddPackFindersEvent event) {
        event.addRepositorySource(new EngineRepositorySource(event.getPackType()));
    }

    private void onRegisterVirtualPacks(RegisterVirtualPackEvent.BeforeUser event) {
        BundleVirtualPacks.create().forEach(event::addPack);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        FoundryCommands.registerAll(event.getDispatcher(), event.getBuildContext());
    }

    private void onConstruct(final FMLConstructModEvent event) {
        try {
            Common.getBundleManager().discover(Common.BUNDLES);
        } catch (IOException e) {
            LOGGER.error("Error while Loading Bundles: {}", e.getLocalizedMessage());
        }

        EngineLogAppender.Holder.addAppender();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    @ApiStatus.Internal
    public static IEventBus getModBus() {
        return MODBUS;
    }
}
