package de.luckymcdev.foundryengine.mixin.level;

import com.google.common.collect.Maps;
import de.luckymcdev.foundryengine.common.world.level.EngineDimensionOptions;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WorldGenSettings.class)
public class WorldGenSettingsMixin {

    @ModifyArg(
            method = "of(Lnet/minecraft/world/level/levelgen/WorldOptions;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/level/levelgen/WorldGenSettings;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/WorldGenSettings;<init>(Lnet/minecraft/world/level/levelgen/WorldOptions;Lnet/minecraft/world/level/levelgen/WorldDimensions;)V"
            ),
            index = 1
    )
    private static WorldDimensions engine$wrapWorldGenSettings(WorldDimensions original) {
        var dimensions = original.dimensions();
        var saveDimensions = Maps.filterEntries(dimensions, entry -> EngineDimensionOptions.SAVE_PROPERTIES_PREDICATE.test(entry.getValue()));

        return new WorldDimensions(saveDimensions);
    }
}
