package de.luckymcdev.foundryengine.config;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public abstract class EngineConfig {

    /**
     * The built spec for this config section.
     */
    public abstract ModConfigSpec spec();

    /**
     * The NeoForge config type. Defaults to COMMON.
     */
    public ModConfig.Type type() {
        return ModConfig.Type.COMMON;
    }
}