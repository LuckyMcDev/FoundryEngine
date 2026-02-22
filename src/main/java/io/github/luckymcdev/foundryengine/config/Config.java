package io.github.luckymcdev.foundryengine.config;

import io.github.luckymcdev.foundryengine.common.Common;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The Config for FoundryEngine.
 */
@EventBusSubscriber(modid = Common.MODID)
public class Config {
    private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    /**
     * {@link ModConfigSpec} for FoundryEngines Common Config.
     */
    public static final ModConfigSpec COMMON_SPEC = COMMON_BUILDER.build();

    /// Private Constructor
    private Config() {
    }

    /**
     * On Config Load Handler.
     *
     * @param event config load event.
     */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }
}
