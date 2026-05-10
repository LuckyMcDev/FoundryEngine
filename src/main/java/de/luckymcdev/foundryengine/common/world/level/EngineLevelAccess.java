package de.luckymcdev.foundryengine.common.world.level;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface EngineLevelAccess {
    void engine$setTickWhenEmpty(boolean tickWhenEmpty);

    boolean engine$shouldTick();
}
