package de.luckymcdev.foundryengine.common.font;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public record TTFFile(Identifier id, Identifier resource, FontVariant variant, short[] glyphRanges) {
    public static final short[] DEFAULT_GLYPH_RANGES = {
            0x0020, 0x00FF,             // Basic Latin
            0x0100, 0x017F,             // Latin Extended-A
            0x0400, 0x052F,             // Cyrillic
            0x3040, 0x30FF,             // Hiragana & Katakana
            (short) 0xE200, (short) 0xE2A9, // Font Awesome extension 1
            (short) 0xED00, (short) 0xF2FF, // Font Awesome extension 2
            0                           // null-terminator
    };

    public static final short[] GLYPH_RANGES_MINIMAL = {
            0x0020, 0x00FF,   // Basic Latin
            0x0100, 0x017F,   // Latin Extended-A
            0
    };

    public static final TTFFile JETBRAINS_MONO_LIGHT = new TTFFile(
            Common.id("jetbrains_mono_nerd/light"),
            Common.id("font/jetbrainsmononerdfontmonolight.ttf"),
            FontVariant.LIGHT, DEFAULT_GLYPH_RANGES);

    public static final TTFFile JETBRAINS_MONO_MEDIUM = new TTFFile(
            Common.id("jetbrains_mono_nerd/medium"),
            Common.id("font/jetbrainsmononerdfontmonomedium.ttf"),
            FontVariant.MEDIUM, DEFAULT_GLYPH_RANGES);

    public static final TTFFile JETBRAINS_MONO_SEMIBOLD = new TTFFile(
            Common.id("jetbrains_mono_nerd/semibold"),
            Common.id("font/jetbrainsmononerdfontmonosemibold.ttf"),
            FontVariant.SEMIBOLD, DEFAULT_GLYPH_RANGES);

    public static final TTFFile JETBRAINS_MONO_REGULAR = new TTFFile(
            Common.id("jetbrains_mono_nerd/regular"),
            Common.id("font/jetbrainsmononerdfontmonoregular.ttf"),
            FontVariant.REGULAR, DEFAULT_GLYPH_RANGES);

    public static final TTFFile JETBRAINS_MONO_BOLD = new TTFFile(
            Common.id("jetbrains_mono_nerd/bold"),
            Common.id("font/jetbrainsmononerdfontmonobold.ttf"),
            FontVariant.BOLD, DEFAULT_GLYPH_RANGES);

    public static final TTFFile JETBRAINS_MONO_ITALIC = new TTFFile(
            Common.id("jetbrains_mono_nerd/italic"),
            Common.id("font/jetbrainsmononerdfontmonoitalic.ttf"),
            FontVariant.ITALIC, DEFAULT_GLYPH_RANGES);

    public static final TTFFile JETBRAINS_MONO_BOLD_ITALIC = new TTFFile(
            Common.id("jetbrains_mono_nerd/bold_italic"),
            Common.id("font/jetbrainsmononerdfontmonobolditalic.ttf"),
            FontVariant.BOLD_ITALIC, DEFAULT_GLYPH_RANGES);

    public static final TTFFile[] JETBRAINS_MONO_NERDFONT_ALL = {
            JETBRAINS_MONO_LIGHT,
            JETBRAINS_MONO_REGULAR,
            JETBRAINS_MONO_MEDIUM,
            JETBRAINS_MONO_SEMIBOLD,
            JETBRAINS_MONO_BOLD,
            JETBRAINS_MONO_ITALIC,
            JETBRAINS_MONO_BOLD_ITALIC
    };
    public static final TTFFile FALLBACK_JB = new TTFFile(
            Common.id("jetbrains/fallback"),
            Common.id("font/jetbrainsmonoregular.ttf"),
            FontVariant.REGULAR, GLYPH_RANGES_MINIMAL
    );

    public byte[] load(ResourceManager resourceManager) {
        try (var in = resourceManager.getResource(resource).orElseThrow().open()) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new EngineException("Failed to read TTF file: " + id, e);
        }
    }

    public enum FontVariant {
        LIGHT, REGULAR, MEDIUM, SEMIBOLD, BOLD, ITALIC, BOLD_ITALIC;

        public boolean isBold() {
            return this == BOLD || this == SEMIBOLD || this == BOLD_ITALIC;
        }

        public boolean isItalic() {
            return this == ITALIC || this == BOLD_ITALIC;
        }
    }
}