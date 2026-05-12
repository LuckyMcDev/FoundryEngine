package de.luckymcdev.foundryengine.common.font;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.imgui.ImGuiFontManager;
import de.luckymcdev.foundryengine.config.ClientConfig;
import net.minecraft.resources.Identifier;

public class BuiltInFonts {
    public static final Identifier LIGHT = TTFFile.JETBRAINS_MONO_LIGHT.id();
    public static final Identifier REGULAR = TTFFile.JETBRAINS_MONO_REGULAR.id();
    public static final Identifier MEDIUM = TTFFile.JETBRAINS_MONO_MEDIUM.id();
    public static final Identifier SEMIBOLD = TTFFile.JETBRAINS_MONO_SEMIBOLD.id();
    public static final Identifier BOLD = TTFFile.JETBRAINS_MONO_BOLD.id();
    public static final Identifier ITALIC = TTFFile.JETBRAINS_MONO_ITALIC.id();
    public static final Identifier BOLD_ITALIC = TTFFile.JETBRAINS_MONO_BOLD_ITALIC.id();
    public static final Identifier FALLBACK_JB = TTFFile.FALLBACK_JB.id();

    /**
     * Registers all built-in fonts to the manager.
     */
    public static void registerAll(ImGuiFontManager manager) {
        for (TTFFile ttf : TTFFile.JETBRAINS_MONO_NERDFONT_ALL) {
            manager.registerFont(ttf, 20.0f);
        }
        if (ClientConfig.IMGUI_FONTS_FALLBACK.getAsBoolean()) {
            Client.LOGGER.debug("Fallback Fonts used.");
            manager.setDefaultFont(FALLBACK_JB);
        } else {
            Client.LOGGER.debug("Regular Font used.");
            manager.setDefaultFont(REGULAR);
        }
    }
}