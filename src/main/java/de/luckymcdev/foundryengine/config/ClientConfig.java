package de.luckymcdev.foundryengine.config;

import de.luckymcdev.foundryengine.client.editor.styles.ImThemes;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;

public final class ClientConfig extends EngineConfig {
    public static final String[] FONT_OPTION_VALUES = {"MINIMAL", "NORMAL", "DISABLED"};
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.ConfigValue<String> SELECTED_THEME =
            BUILDER.comment("The currently selected ImGui theme. Available themes: " + ImThemes.getAvailableThemeNames())
                    .define("SELECTED_THEME", ImThemes.BESS_DARK_IM_THEME.getName());
    public static final ModConfigSpec.ConfigValue<String> FONT_OPTION =
            BUILDER.comment("What should happen with fonts.")
                    .comment("Can be: " + Arrays.toString(FONT_OPTION_VALUES))
                    .comment("")
                    .define("FONT_OPTION", "NORMAL");

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

    public static final ModConfigSpec.BooleanValue SHOW_SLOT_TOOLTIP =
            BUILDER.comment("If slot tooltips are shown")
                    .define("SHOW_SLOT_TOOLTIP", true);

    public static final ModConfigSpec.ConfigValue<String> BLOCK_ENTITY_RENDER_DISTANCE =
            BUILDER.comment("From how far away Block entities are rendered.")
                    .comment("There are 3 modes. full, half, vanilla")
                    .comment("full -> Uses your render distance.",
                            "half -> Uses Half your render distance",
                            "vanilla -> Uses the vanilla 64 blocks")
                    .define("BLOCK_ENTITY_RENDER_DISTANCE", "vanilla");

    public static int getComputedBlockEntityRenderDistance() {
        int effectiveRdBlocks = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
        return switch (BLOCK_ENTITY_RENDER_DISTANCE.get()) {
            case "full" -> effectiveRdBlocks;
            case "half" -> effectiveRdBlocks / 2;
            default -> 64;
        };
    }

    @Override
    public ModConfigSpec spec() {
        return BUILDER.build();
    }

    @Override
    public ModConfig.Type type() {
        return ModConfig.Type.CLIENT;
    }
}