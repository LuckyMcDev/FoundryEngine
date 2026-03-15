package io.github.luckymcdev.foundryengine.config;

import io.github.luckymcdev.foundryengine.common.Common;
import io.github.luckymcdev.foundryengine.common.exeptions.UtilityClassException;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The Config for FoundryEngine.
 */
@EventBusSubscriber(modid = Common.MODID)
public class Config {

    private Config() {
    }

    /**
     * On Config Load Handler.
     *
     * @param event config load event.
     */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // No functionality needed, may be added at some point tho.
    }

    /**
     * Common Config
     */
    public static class Common {
        private Common() {
            throw new UtilityClassException();
        }
        private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
        public static final ModConfigSpec COMMON_SPEC = COMMON_BUILDER.build();
    }

    /**
     * Client Config
     */
    public static class Client {
        private Client() {
            throw new UtilityClassException();
        }
        private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
        public static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();
    }

    /**
     * Server Config
     */
    public static class Server {
        private Server() {
            throw new UtilityClassException();
        }
        private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
        public static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();
    }

    /**
     * Startup Config
     */
    public static class Startup {
        private Startup() {
            throw new UtilityClassException();
        }
        private static final ModConfigSpec.Builder STARTUP_BUILDER = new ModConfigSpec.Builder();

        public static final ModConfigSpec.BooleanValue SCRIPTING_ENABLED = STARTUP_BUILDER
                .comment("This enables / disables the loading of Scripts from Bundles.")
                .define("SCRIPTING_ENABLED", true);

        public static final ModConfigSpec.BooleanValue RESOURCES_ENABLED = STARTUP_BUILDER
                .comment("This enables / disables the loading of Resources from Bundles.")
                .define("RESOURCES_ENABLED", true);

        public static final ModConfigSpec.BooleanValue EVAL_COMMAND_ENABLED = STARTUP_BUILDER
                .comment("This enables / disables the /engine eval command.")
                .define("EVAL_COMMAND_ENABLED", true);

        public static final ModConfigSpec.IntValue EVAL_COMMAND_PERMISSION = STARTUP_BUILDER
                .comment("This is the level at which the eval command can be ran.")
                .defineInRange("EVAL_COMMAND_PERMISSION", 4, 0, 4);

        public static final ModConfigSpec STARTUP_SPEC = STARTUP_BUILDER.build();
    }
}
