package de.luckymcdev.foundryengine.common.world.level.runtime;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.WorldData;

public class RuntimeLevelData extends DerivedLevelData {
    final RuntimeLevelConfig config;

    public RuntimeLevelData(WorldData worldData, RuntimeLevelConfig config) {
        super(worldData, worldData.overworldData());
        this.config = config;
    }

    @Override
    public long getGameTime() {
        return this.config.getGameTime();
    }

    @Override
    public void setGameTime(long time) {
        this.config.setGameTime(time);
    }

    @Override
    public Difficulty getDifficulty() {
        if (this.config.shouldMirrorOverworldDifficulty()) {
            return super.getDifficulty();
        }
        return this.config.getDifficulty();
    }
}
