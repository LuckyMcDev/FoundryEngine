package de.luckymcdev.foundryengine.common.world.entity;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.world.entity.display.EngineBlockDisplay;
import de.luckymcdev.foundryengine.common.world.entity.display.EngineItemDisplay;
import de.luckymcdev.foundryengine.common.world.entity.display.EngineTextDisplay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class EngineEntities {

    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(Common.MODID);

    public static final Supplier<EntityType<EngineBlockDisplay>> BLOCK_DISPLAY = create(
            "block_display",
            EngineBlockDisplay::new,
            MobCategory.MISC
    );

    public static final Supplier<EntityType<EngineItemDisplay>> ITEM_DISPLAY = create(
            "item_display",
            EngineItemDisplay::new,
            MobCategory.MISC
    );

    public static final Supplier<EntityType<EngineTextDisplay>> TEXT_DISPLAY = create(
            "text_display",
            EngineTextDisplay::new,
            MobCategory.MISC
    );

    private static <E extends Entity> Supplier<EntityType<E>> create(String name, EntityType.EntityFactory<E> factory, MobCategory category) {
        return ENTITY_TYPES.registerEntityType(
                name,
                factory,
                category,
                builder -> builder
                        .noLootTable()
                        .sized(1.0F, 1.0F)
                        .clientTrackingRange(10)
                        .updateInterval(1)
        );
    }

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}