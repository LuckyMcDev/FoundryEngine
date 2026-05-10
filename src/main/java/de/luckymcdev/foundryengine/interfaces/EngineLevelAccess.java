package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.server.level.ServerLevel;

public interface EngineLevelAccess extends EngineInterface<ServerLevel> {
    void engine$setTickWhenEmpty(boolean tickWhenEmpty);

    boolean engine$shouldTick();
}
