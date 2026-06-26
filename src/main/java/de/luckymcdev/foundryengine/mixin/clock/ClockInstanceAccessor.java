package de.luckymcdev.foundryengine.mixin.clock;

import net.minecraft.world.clock.ServerClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for clock instance internals (paused, totalTicks, partialTick, rate).
 */
@Mixin(ServerClockManager.ClockInstance.class)
public interface ClockInstanceAccessor {
    @Accessor
    boolean isPaused();

    @Accessor
    long getTotalTicks();

    @Accessor
    float getPartialTick();

    @Accessor
    float getRate();
}
