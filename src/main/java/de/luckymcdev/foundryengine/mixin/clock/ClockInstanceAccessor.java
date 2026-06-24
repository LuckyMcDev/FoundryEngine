package de.luckymcdev.foundryengine.mixin.clock;

import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for clock instance internals (paused, totalTicks, partialTick, rate).
 */
@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.clock.ServerClockManager.ClockInstance.class)
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
