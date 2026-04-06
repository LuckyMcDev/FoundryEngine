package de.luckymcdev.foundryengine.config;

import de.luckymcdev.foundryengine.client.editor.styles.ImThemes;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig extends EngineConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> SELECTED_THEME =
            BUILDER.comment("The currently selected ImGui theme. Available themes: " + ImThemes.getAvailableThemeNames())
                    .define("selectedTheme", ImThemes.BESS_DARK_IM_THEME.getName());

    public static final ModConfigSpec.BooleanValue RENDER_OFFHAND =
            BUILDER.comment("A Funny Offhand rendering technique. It makes it be rendered the same way the main hand is.")
                    .define("RENDER_OFFHAND", false);

    public static final ModConfigSpec.BooleanValue AUTO_EXPORT =
            BUILDER.comment("Automatically run the icon exporter on login when the registry has changed or no export exists yet.")
                    .define("AUTO_EXPORT", true);

    public static final ModConfigSpec.IntValue ICON_SIZE =
            BUILDER.comment("How Large the icons of the icon exporter should be, default is 64")
                    .comment("If they are larger, the game takes longer to create them.")
                    .defineInRange("ICON_SIZE", 64, 16, 256);

    @Override
    public ModConfigSpec spec() {
        return BUILDER.build();
    }

    @Override
    public ModConfig.Type type() {
        return ModConfig.Type.CLIENT;
    }
}