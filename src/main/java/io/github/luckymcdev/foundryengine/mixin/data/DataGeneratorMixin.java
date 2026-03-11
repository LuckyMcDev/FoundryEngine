package io.github.luckymcdev.foundryengine.mixin.data;

import io.github.luckymcdev.foundryengine.common.data.DataGeneratorContext;
import io.github.luckymcdev.foundryengine.interfaces.EngineDataGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

/**
 * Mixin to selectively stop the DataGenerator from purging.
 * Only disables purging when the generator was created by FoundryEngine.
 */
@Mixin(DataGenerator.class)
public class DataGeneratorMixin {

    /**
     * Redirects the purge call to a no-op if it's a EngineDataGenerator.
     * This prevents the generator from deleting "stale" files.
     */
    @Redirect(
            method = "run",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/data/HashCache;purgeStaleAndWrite()V")
    )
    private void conditionallyStopPurging(HashCache instance) throws IOException {
        EngineDataGenerator currentGenerator = DataGeneratorContext.getCurrentGenerator();
        if (currentGenerator != null && currentGenerator.shouldSkipPurging()) {
            return;
        }
        instance.purgeStaleAndWrite();
    }
}