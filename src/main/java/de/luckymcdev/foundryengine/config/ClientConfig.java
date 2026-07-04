package de.luckymcdev.foundryengine.config;

import de.luckymcdev.foundryengine.client.editor.styles.ImThemes;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorTheme;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;

public final class ClientConfig {
    public static final String[] FONT_OPTION_VALUES = {"MINIMAL", "NORMAL", "DISABLED"};

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<String> SELECTED_THEME;
	public static final ModConfigSpec.ConfigValue<String> TEXT_EDITOR_THEME;
    public static final ModConfigSpec.ConfigValue<String> FONT_OPTION;
    public static final ModConfigSpec.BooleanValue RENDER_OFFHAND;
    public static final ModConfigSpec.BooleanValue AUTO_EXPORT;
    public static final ModConfigSpec.IntValue ICON_SIZE;
    public static final ModConfigSpec.BooleanValue SHOW_SLOT_TOOLTIP;
    public static final ModConfigSpec.ConfigValue<String> BLOCK_ENTITY_RENDER_DISTANCE;
    public static final ModConfigSpec.BooleanValue CUSTOM_SKYBOX;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        SELECTED_THEME = builder
                .comment("The currently selected ImGui theme. Available themes: " + ImThemes.getAvailableThemeNames())
                .translation("foundryengine.configuration.selected_theme")
                .define("SELECTED_THEME", ImThemes.BESS_DARK_IM_THEME.getName());

		TEXT_EDITOR_THEME = builder
			.comment("The currently selected ImGui Text Editor theme. Available themes: " + EditorTheme.getAvailableThemeNames())
			.translation("foundryengine.configuration.selected_theme")
			.define("TEXT_EDITOR_THEME", "dark");

        FONT_OPTION = builder
                .comment("What should happen with fonts.")
                .comment("Can be: " + Arrays.toString(FONT_OPTION_VALUES))
                .comment("")
                .translation("foundryengine.configuration.font_option")
                .define("FONT_OPTION", "NORMAL");

        RENDER_OFFHAND = builder
                .comment("A Funny Offhand rendering technique. It makes it be rendered the same way the main hand is.")
                .translation("foundryengine.configuration.render_offhand")
                .define("RENDER_OFFHAND", false);

        AUTO_EXPORT = builder
                .comment("Automatically run the icon exporter on login when the registry has changed or no export exists yet.")
                .translation("foundryengine.configuration.auto_export")
                .define("AUTO_EXPORT", true);

        ICON_SIZE = builder
                .comment("How large the icons of the icon exporter should be, default is 64.")
                .comment("If they are larger, the game takes longer to create them.")
                .translation("foundryengine.configuration.icon_size")
                .defineInRange("ICON_SIZE", 64, 16, 256);

        SHOW_SLOT_TOOLTIP = builder
                .comment("If slot tooltips are shown")
                .translation("foundryengine.configuration.show_slot_tooltip")
                .define("SHOW_SLOT_TOOLTIP", true);

        BLOCK_ENTITY_RENDER_DISTANCE = builder
                .comment("From how far away block entities are rendered.")
                .comment("There are 3 modes. full, half, vanilla")
                .comment("full -> Uses your render distance.",
                        "half -> Uses half your render distance",
                        "vanilla -> Uses the vanilla 64 blocks")
                .translation("foundryengine.configuration.block_entity_render_distance")
                .define("BLOCK_ENTITY_RENDER_DISTANCE", "vanilla");

        CUSTOM_SKYBOX = builder
                .comment("If the custom skybox rendering is used")
                .translation("foundryengine.configuration.custom_skybox")
                .define("CUSTOM_SKYBOX", false);

        SPEC = builder.build();
    }

    public static int getComputedBlockEntityRenderDistance() {
        int effectiveRdBlocks = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
        return switch (BLOCK_ENTITY_RENDER_DISTANCE.get()) {
            case "full" -> effectiveRdBlocks;
            case "half" -> effectiveRdBlocks / 2;
            default -> 64;
        };
    }
}