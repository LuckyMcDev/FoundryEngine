package de.luckymcdev.foundryengine.common.font;

import de.luckymcdev.foundryengine.client.imgui.ImGuiFontManager;
import net.minecraft.resources.Identifier;

import java.util.List;

public class BuiltInFonts {
    public static final Identifier LIGHT = TTFFile.JETBRAINS_MONO_LIGHT.id();
    public static final Identifier REGULAR = TTFFile.JETBRAINS_MONO_REGULAR.id();
    public static final Identifier MEDIUM = TTFFile.JETBRAINS_MONO_MEDIUM.id();
    public static final Identifier SEMIBOLD = TTFFile.JETBRAINS_MONO_SEMIBOLD.id();
    public static final Identifier BOLD = TTFFile.JETBRAINS_MONO_BOLD.id();
    public static final Identifier ITALIC = TTFFile.JETBRAINS_MONO_ITALIC.id();
    public static final Identifier BOLD_ITALIC = TTFFile.JETBRAINS_MONO_BOLD_ITALIC.id();
    public static final Identifier FALLBACK_JB = TTFFile.FALLBACK_JB.id();
    public static final List<Identifier> NORMAL_LIST = List.of(
            LIGHT, REGULAR, MEDIUM, SEMIBOLD, BOLD, ITALIC, BOLD_ITALIC
    );
    public static final List<Identifier> MINIMAL_LIST = List.of(
            FALLBACK_JB
    );

    public static void registerAll(ImGuiFontManager manager) {
        registerJetbrainsMonoNerdFontMono(manager);
        manager.registerFont(TTFFile.FALLBACK_JB);
    }

    /**
     * Registers all built-in fonts to the manager.
     */
    public static void registerJetbrainsMonoNerdFontMono(ImGuiFontManager manager) {
        for (TTFFile ttf : TTFFile.JETBRAINS_MONO_NERDFONT_ALL) {
            manager.registerFont(ttf);
        }
    }
}