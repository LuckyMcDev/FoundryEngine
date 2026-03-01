package io.github.luckymcdev.foundryengine.mixin.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DataGenerator.class)
public class DataGeneratorMixin {

    /**
     * Redirects the purge call to a no-op (does nothing).
     * This prevents the generator from deleting "stale" files.
     */
    @Redirect(
            method = "run",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/data/HashCache;purgeStaleAndWrite()V")
    )
    private void stopPurging(HashCache instance) {
    }
}
