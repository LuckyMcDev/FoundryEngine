package de.luckymcdev.foundryengine.client.imgui;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.imgui.backend.ImGuiImplGl3;
import de.luckymcdev.foundryengine.common.font.TTFFile;
import imgui.ImFont;
import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import imgui.ImGui;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.util.function.Function;

/**
 * Manages ImGui font loading, atlas building and texture creation.
 * Highly configurable to support custom fonts and glyph ranges.
 */
public final class ImGuiFontManager {
    public static final short[] DEFAULT_GLYPH_RANGES = {
            0x0020, 0x00FF,   // Basic Latin
            0x0100, 0x017F,   // Latin Extended-A
            0x0400, 0x052F,   // Cyrillic
            0x3040, 0x30FF,   // Hiragana & Katakana
            (short) 0xE200, (short) 0xE2A9, // FA Extension
            (short) 0xED00, (short) 0xF2FF, // Font Awesome
            0
    };
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ImGuiImplGl3 glImpl;
    private ImFont currentFont;
    private short[] glyphRanges = DEFAULT_GLYPH_RANGES;
    private int oversampleH = 3;
    private int oversampleV = 3;
    private float rasterizerMultiply = 1.2f;
    private float glyphOffsetX = 0.0f;
    private float glyphOffsetY = 0.0f;
    private float fontSize = 20.0f;

    /**
     * Optional external font provider. Receives the current {@link ResourceManager} and returns the TTF bytes.
     * By default, {@link TTFFile#JETBRAINS_MONO_NERDFONT_REGULAR} is used.
     */
    private Function<ResourceManager, byte[]> fontProvider;

    public ImGuiFontManager(ImGuiImplGl3 glImpl) {
        this.glImpl = glImpl;
    }

    public void setGlyphRanges(short[] ranges) {
        this.glyphRanges = ranges;
    }

    public void setOversample(int h, int v) {
        this.oversampleH = h;
        this.oversampleV = v;
    }

    public void setRasterizerMultiply(float multiply) {
        this.rasterizerMultiply = multiply;
    }

    public void setGlyphOffset(float x, float y) {
        this.glyphOffsetX = x;
        this.glyphOffsetY = y;
    }

    public void setFontSize(float size) {
        this.fontSize = size;
    }

    /**
     * Sets a custom font provider. The function receives the {@link ResourceManager} on reload
     * and must return the raw TTF bytes. Return {@code null} to fall back to the default font.
     */
    public void setFontProvider(Function<ResourceManager, byte[]> provider) {
        this.fontProvider = provider;
    }

    /**
     * Ensures a minimal default font atlas exists. Call once after ImGui context creation
     * but before resource reload, to provide a fallback font for early rendering.
     */
    public void initializeDefaultFont() {
        ImFontAtlas fonts = ImGui.getIO().getFonts();
        if (!fonts.isBuilt()) {
            currentFont = fonts.addFontDefault();
            if (!fonts.build()) {
                LOGGER.error("Failed to build default font atlas!");
            }
            glImpl.createFontsTexture();
        }
    }

    /**
     * Clears the current font atlas, loads the configured font (custom or default)
     * and rebuilds the font texture. Should be called on resource reload.
     *
     * @param resourceManager the current resource manager, passed to the font provider
     */
    public void loadFonts(ResourceManager resourceManager) {
        ImFontAtlas fonts = ImGui.getIO().getFonts();

        fonts.clear();
        glImpl.destroyFontsTexture();

        ImFontConfig config = new ImFontConfig();
        config.setGlyphRanges(glyphRanges);
        config.setOversampleH(oversampleH);
        config.setOversampleV(oversampleV);
        config.setRasterizerMultiply(rasterizerMultiply);
        config.setGlyphOffset(glyphOffsetX, glyphOffsetY);

        boolean fontLoaded = false;

        try {
            byte[] ttfBytes = null;

            // Try external provider first
            if (fontProvider != null) {
                ttfBytes = fontProvider.apply(resourceManager);
            }
            // Default provider: built-in JetBrains Mono
            if (ttfBytes == null) {
                ttfBytes = TTFFile.JETBRAINS_MONO_NERDFONT_REGULAR.load(resourceManager);
            }

            if (ttfBytes != null) {
                currentFont = fonts.addFontFromMemoryTTF(ttfBytes, fontSize, config);
                fontLoaded = (currentFont != null);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load custom font: {}", e.getMessage(), e);
        }

        // 3. Fall back to default font if custom font failed
        if (!fontLoaded) {
            LOGGER.warn("Using default font because custom font could not be loaded.");
            currentFont = fonts.addFontDefault();
        }

        // 4. Build atlas, with an extra safety fallback
        if (!fonts.build()) {
            LOGGER.error("Failed to build font atlas with custom font; falling back to default.");
            fonts.clear();
            currentFont = fonts.addFontDefault();
            if (!fonts.build()) {
                LOGGER.error("Failed to build even the default font atlas!");
            }
        }

        // 5. Update font texture
        glImpl.createFontsTexture();

        // 6. Clean up
        config.destroy();
        fonts.clearTexData();

        if (ImGui.getFont() == null) {
            LOGGER.error("Font still null after loading, reinitializing with default.");
            fonts.clear();
            fonts.addFontDefault();
            fonts.build();
            glImpl.createFontsTexture();
        }
    }

    /**
     * Destroys the font texture and clears the font reference.
     */
    public void destroy() {
        glImpl.destroyFontsTexture();
        currentFont = null;
    }

    public ImFont getFont() {
        return currentFont;
    }
}