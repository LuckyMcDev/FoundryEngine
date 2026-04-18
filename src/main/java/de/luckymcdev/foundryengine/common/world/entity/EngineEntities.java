package de.luckymcdev.foundryengine.common.world.entity;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class EngineEntities {

    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(Common.MODID);

    public static final Supplier<EntityType<EngineBlockDisplay>> BLOCK_DISPLAY =
            ENTITY_TYPES.registerEntityType(
                    "block_display",
                    EngineBlockDisplay::new,
                    MobCategory.MISC,
                    builder -> builder
                            .noLootTable()
                            .sized(0.0F, 0.0F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
            );

    public static final Supplier<EntityType<EngineItemDisplay>> ITEM_DISPLAY =
            ENTITY_TYPES.registerEntityType(
                    "item_display",
                    EngineItemDisplay::new,
                    MobCategory.MISC,
                    builder -> builder
                            .noLootTable()
                            .sized(0.0F, 0.0F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
            );

    public static final Supplier<EntityType<EngineTextDisplay>> TEXT_DISPLAY =
            ENTITY_TYPES.registerEntityType(
                    "text_display",
                    EngineTextDisplay::new,
                    MobCategory.MISC,
                    builder -> builder
                            .noLootTable()
                            .sized(0.0F, 0.0F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
            );

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}