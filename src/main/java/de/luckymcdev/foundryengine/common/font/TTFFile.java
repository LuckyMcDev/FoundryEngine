package de.luckymcdev.foundryengine.common.font;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.List;

public record TTFFile(Identifier id, Identifier resource, short[] glyphRanges) {
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

    public static final FontFamily JETBRAINS_MONO_NERD = new FontFamily(
            face("jetbrains_mono_nerd/light", "font/jetbrainsmononerdfontmonolight.ttf", DEFAULT_GLYPH_RANGES),
            face("jetbrains_mono_nerd/regular", "font/jetbrainsmononerdfontmonoregular.ttf", DEFAULT_GLYPH_RANGES),
            face("jetbrains_mono_nerd/medium", "font/jetbrainsmononerdfontmonomedium.ttf", DEFAULT_GLYPH_RANGES),
            face("jetbrains_mono_nerd/semibold", "font/jetbrainsmononerdfontmonosemibold.ttf", DEFAULT_GLYPH_RANGES),
            face("jetbrains_mono_nerd/bold", "font/jetbrainsmononerdfontmonobold.ttf", DEFAULT_GLYPH_RANGES),
            face("jetbrains_mono_nerd/italic", "font/jetbrainsmononerdfontmonoitalic.ttf", DEFAULT_GLYPH_RANGES),
            face("jetbrains_mono_nerd/bold_italic", "font/jetbrainsmononerdfontmonobolditalic.ttf", DEFAULT_GLYPH_RANGES)
    );
    public static final TTFFile FALLBACK_JB = new TTFFile(
            Common.id("jetbrains/fallback"),
            Common.id("font/jetbrainsmonoregular.ttf"),
            GLYPH_RANGES_MINIMAL
    );

    private static TTFFile face(String idPath, String resourcePath, short[] glyphRanges) {
        return new TTFFile(Common.id(idPath), Common.id(resourcePath), glyphRanges);
    }

    /**
     * Loads the TTF file bytes from the resource manager.
     */
    public byte[] load(ResourceManager resourceManager) {
        try (var in = resourceManager.getResource(resource).orElseThrow(
                () -> new EngineException("Missing TTF resource: " + resource + " for font " + id)
        ).open()) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new EngineException("Failed to read TTF file: " + id, e);
        }
    }

    public record FontFamily(TTFFile light, TTFFile regular, TTFFile medium, TTFFile semibold, TTFFile bold,
                             TTFFile italic, TTFFile boldItalic) {
        public List<TTFFile> all() {
            return List.of(light, regular, medium, semibold, bold, italic, boldItalic);
        }

        public List<Identifier> ids() {
            return all().stream().map(TTFFile::id).toList();
        }

        public TTFFile face(boolean isBold, boolean isItalic) {
            if (isBold && isItalic) {
                return this.boldItalic;
            }
            if (isBold) {
                return this.bold;
            }
            if (isItalic) {
                return this.italic;
            }
            return this.regular;
        }
    }
}
