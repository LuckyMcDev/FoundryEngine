package de.luckymcdev.foundryengine.client.imgui;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.imgui.backend.ImGuiRenderer;
import de.luckymcdev.foundryengine.common.font.TTFFile;
import imgui.ImFont;
import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import imgui.ImGui;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;

/**
 * Manages multiple ImGui fonts. Register fonts, then call {@link #loadFonts(ResourceManager)}
 * to build the atlas. Use {@link #getFont(Identifier)} to retrieve an ImFont and
 * {@link #pushFont(Identifier)} / {@link #popFont()} for easy switching.
 */
public final class ImGuiFontManager {
    public static final short[] DEFAULT_GLYPH_RANGES = {
            0x0020, 0x00FF,   // Basic Latin
            0x0100, 0x017F,   // Latin Extended-A
            0x0400, 0x052F,   // Cyrillic
            0x3040, 0x30FF,   // Hiragana & Katakana
            0
    };
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ImGuiRenderer renderer;
    private final List<FontRegistration> registrations = new ArrayList<>();
    private final Map<Identifier, ImFont> loadedFonts = new LinkedHashMap<>();
    private @Nullable ImFont actualImGuiDefaultFont;
    private @Nullable Identifier defaultFontId;
    private short[] globalGlyphRanges = DEFAULT_GLYPH_RANGES;
    private int oversampleH = 3;
    private int oversampleV = 3;
    private float rasterizerMultiply = 1.2f;
    private float glyphOffsetX = 0.0f;
    private float glyphOffsetY = 0.0f;

    public ImGuiFontManager(ImGuiRenderer renderer) {
        this.renderer = renderer;
    }

    public void setGlobalGlyphRanges(short[] ranges) {
        this.globalGlyphRanges = ranges;
    }

    public void setGlobalOversample(int h, int v) {
        this.oversampleH = h;
        this.oversampleV = v;
    }

    public void setGlobalRasterizerMultiply(float mul) {
        this.rasterizerMultiply = mul;
    }

    public void setGlobalGlyphOffset(float x, float y) {
        this.glyphOffsetX = x;
        this.glyphOffsetY = y;
    }

    public void registerFont(TTFFile ttfFile) {
        registerFont(ttfFile, 18.0f);
    }

    public void registerFont(TTFFile ttfFile, float fontSize) {
        registerFont(ttfFile, fontSize, null);
    }

    public void registerFont(TTFFile ttfFile, float fontSize, @Nullable ImFontConfig customConfig) {
        Objects.requireNonNull(ttfFile, "TTFFile cannot be null");
        registrations.add(new TTFFileRegistration(ttfFile, fontSize, customConfig));
    }

    public void registerRawFont(Identifier id, Function<ResourceManager, byte[]> ttfProvider,
                                float fontSize, ImFontConfig config) {
        registrations.add(new RawFontRegistration(id, ttfProvider, fontSize, config));
    }

    public void setDefaultFont(Identifier id) {
        this.defaultFontId = id;
    }

    /**
     * Loads all registered fonts. Pass an empty list to load all fonts,
     * or a non-empty list to load only the fonts with matching IDs.
     */
    public void loadFonts(ResourceManager resourceManager, List<Identifier> filter) {
        ImFontAtlas atlas = ImGui.getIO().getFonts();
        atlas.clear();
        renderer.destroyFontsTexture();

        loadedFonts.clear();
        actualImGuiDefaultFont = null;

        for (FontRegistration reg : registrations) {
            if (!filter.isEmpty() && !filter.contains(reg.getId())) {
                continue;
            }
            try {
                byte[] ttfData = reg.loadBytes(resourceManager);
                if (ttfData == null) {
                    LOGGER.warn("Skipping font {} – no TTF data", reg.getId());
                    continue;
                }

                ImFontConfig fontConfig;
                if (reg.getCustomConfig() != null) {
                    fontConfig = reg.getCustomConfig();
                } else {
                    fontConfig = new ImFontConfig();
                    fontConfig.setOversampleH(oversampleH);
                    fontConfig.setOversampleV(oversampleV);
                    fontConfig.setRasterizerMultiply(rasterizerMultiply);
                    fontConfig.setGlyphOffset(glyphOffsetX, glyphOffsetY);

                    short[] ranges = reg.getGlyphRanges();
                    if (ranges == null || ranges.length == 0) {
                        ranges = globalGlyphRanges;
                    }
                    fontConfig.setGlyphRanges(ranges);
                }

                ImFont font = atlas.addFontFromMemoryTTF(ttfData, reg.getFontSize(), fontConfig);
                if (font != null) {
                    loadedFonts.put(reg.getId(), font);
                    LOGGER.debug("Loaded font: {}", reg.getId());
                } else {
                    LOGGER.error("Failed to add font from TTF: {}", reg.getId());
                }

                if (reg.getCustomConfig() == null) {
                    fontConfig.destroy();
                }
            } catch (Exception e) {
                LOGGER.error("Exception while loading font {}: {}", reg.getId(), e.getMessage(), e);
            }
        }

        if (loadedFonts.isEmpty()) {
            LOGGER.warn("No fonts were loaded – falling back to ImGui default font");
            actualImGuiDefaultFont = atlas.addFontDefault();
            loadedFonts.put(Identifier.withDefaultNamespace("default"), actualImGuiDefaultFont);
        }

        if (!atlas.build()) {
            LOGGER.error("Failed to build font atlas! Falling back to a minimal default font.");
            atlas.clear();
            loadedFonts.clear();
            actualImGuiDefaultFont = atlas.addFontDefault();
            loadedFonts.put(Identifier.withDefaultNamespace("default"), actualImGuiDefaultFont);
            if (!atlas.build()) {
                throw new IllegalStateException("Could not build even the default font atlas");
            }
        }

        renderer.createFontsTexture();

        ImFont defaultFont = defaultFontId != null ? loadedFonts.get(defaultFontId) : loadedFonts.values().iterator().next();
        if (defaultFont != null) {
            actualImGuiDefaultFont = defaultFont;
            ImGui.getIO().setFontDefault(defaultFont);
        } else if (!loadedFonts.isEmpty()) {
            actualImGuiDefaultFont = loadedFonts.values().iterator().next();
            ImGui.getIO().setFontDefault(actualImGuiDefaultFont);
        }

        atlas.clearTexData();
    }

    public void loadFonts(ResourceManager resourceManager) {
        loadFonts(resourceManager, List.of());
    }

    public void destroy() {
        renderer.destroyFontsTexture();
        loadedFonts.clear();
        registrations.clear();
        defaultFontId = null;
        actualImGuiDefaultFont = null;
    }

    public ImFont getCurrent() {
        return ImGui.getFont();
    }

    public ImFont getFont(Identifier id) {
        ImFont font = loadedFonts.get(id);
        if (font == null) {
            LOGGER.error("Font '{}' not found, returning default font.", id);
            return actualImGuiDefaultFont != null ? actualImGuiDefaultFont : ImGui.getIO().getFontDefault();
        }
        return font;
    }

    public Set<Identifier> getLoadedFontIds() {
        return Collections.unmodifiableSet(loadedFonts.keySet());
    }

    public void withFont(Identifier font, Runnable runnable) {
        pushFont(font);
        runnable.run();
        popFont();
    }

    public void pushFont(Identifier id) {
        ImGui.pushFont(getFont(id));
    }

    public void popFont() {
        ImGui.popFont();
    }

    private interface FontRegistration {
        Identifier getId();

        byte[] loadBytes(ResourceManager rm) throws Exception;

        float getFontSize();

        ImFontConfig getCustomConfig();

        short[] getGlyphRanges();
    }

    private record TTFFileRegistration(TTFFile file, float fontSize,
                                       @Nullable ImFontConfig customConfig) implements FontRegistration {
        @Override
        public Identifier getId() {
            return file.id();
        }

        @Override
        public byte[] loadBytes(ResourceManager rm) {
            return file.load(rm);
        }

        @Override
        public short[] getGlyphRanges() {
            return file.glyphRanges();
        }

        @Override
        public float getFontSize() {
            return fontSize;
        }

        @Override
        public ImFontConfig getCustomConfig() {
            return customConfig;
        }
    }

    private record RawFontRegistration(Identifier id, Function<ResourceManager, byte[]> provider,
                                       float fontSize, ImFontConfig customConfig) implements FontRegistration {
        @Override
        public byte[] loadBytes(ResourceManager rm) {
            return provider.apply(rm);
        }

        @Override
        public short[] getGlyphRanges() {
            return null;
        }

        @Override
        public Identifier getId() {
            return id;
        }

        @Override
        public float getFontSize() {
            return fontSize;
        }

        @Override
        public ImFontConfig getCustomConfig() {
            return customConfig;
        }
    }
}