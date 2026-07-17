package de.luckymcdev.foundryengine.client.imgui;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import de.luckymcdev.foundryengine.client.imgui.backend.ImGuiImplGl3;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import imgui.ImFont;
import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import imgui.ImGui;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.TrueTypeGlyphProviderDefinition;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Minimal ImGui font manager. Fonts are identified by font definition JSON IDs
 * (same as {@code FontDescription.Resource}). Uses Minecraft's own
 * {@link GlyphProviderDefinition.Conditional#CODEC} to parse the JSON and
 * extract the TTF file path.
 */
public final class ImGuiFontManager {
	public static final short[] DEFAULT_GLYPH_RANGES = {
		0x0020, 0x00FF,
		0x0100, 0x017F,
		0x0400, 0x052F,
		0x3040, 0x30FF,
		(short) 0xE200, (short) 0xE2A9,
		(short) 0xED00, (short) 0xF2FF,
		0
	};
	public static final short[] GLYPH_RANGES_MINIMAL = {
		0x0020, 0x00FF,
		0x0100, 0x017F,
		0
	};
	private static final FileToIdConverter FONT_DEFINITIONS = FileToIdConverter.json("font");
	private static final Gson GSON = new Gson();

	private final ImGuiImplGl3 glImpl;
	private final Map<FontDescription.Resource, ImFont> loadedFonts = new LinkedHashMap<>();
	private @Nullable ImFont defaultFont;
	private short[] globalGlyphRanges = DEFAULT_GLYPH_RANGES;
	private int oversampleH = 3;
	private int oversampleV = 3;
	private float rasterizerMultiply = 1.2f;
	private float glyphOffsetX = 0.0f;
	private float glyphOffsetY = 0.0f;

	public ImGuiFontManager(ImGuiImplGl3 glImpl) {
		this.glImpl = glImpl;
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

	public void load(ResourceManager rm, Collection<FontDescription.Resource> fontIds, FontDescription.@Nullable Resource defaultId) {
		ImFontAtlas atlas = ImGui.getIO().getFonts();
		if (atlas == null) {
			return;
		}

		atlas.clear();
		if (glImpl != null) {
			glImpl.destroyFontsTexture();
		}
		loadedFonts.clear();
		defaultFont = null;

		for (FontDescription.Resource id : fontIds) {
			try {
				byte[] ttfData = loadTtfBytes(id, rm);
				ImFontConfig config = new ImFontConfig();
				config.setOversampleH(oversampleH);
				config.setOversampleV(oversampleV);
				config.setRasterizerMultiply(rasterizerMultiply);
				config.setGlyphOffset(glyphOffsetX, glyphOffsetY);
				config.setGlyphRanges(globalGlyphRanges);
				ImFont font = atlas.addFontFromMemoryTTF(ttfData, 18.0f, config);
				config.destroy();
				if (font != null) {
					loadedFonts.put(id, font);
				}
			} catch (Exception ignored) {
			}
		}

		if (loadedFonts.isEmpty()) {
			defaultFont = atlas.addFontDefault();
			loadedFonts.put(FontDescription.DEFAULT, defaultFont);
		}

		if (!atlas.build()) {
			atlas.clear();
			loadedFonts.clear();
			defaultFont = atlas.addFontDefault();
			loadedFonts.put(FontDescription.DEFAULT, defaultFont);
			if (!atlas.build()) {
				throw new IllegalStateException("Could not build even the default font atlas");
			}
		}

		if (glImpl != null) {
			glImpl.createFontsTexture();
		}

		ImFont selected = defaultId != null ? loadedFonts.get(defaultId) : null;
		if (selected == null && !loadedFonts.isEmpty()) {
			selected = loadedFonts.values().iterator().next();
		}
		if (selected != null) {
			defaultFont = selected;
			ImGui.getIO().setFontDefault(selected);
		}

		atlas.clearTexData();
	}

	public void load(ResourceManager rm, Collection<FontDescription.Resource> fontIds) {
		load(rm, fontIds, null);
	}

	public void destroy() {
		if (glImpl != null) {
			glImpl.destroyFontsTexture();
		}
		loadedFonts.clear();
		defaultFont = null;
	}

	public ImFont getCurrent() {
		return ImGui.getFont();
	}

	public ImFont getFont(FontDescription id) {
		ImFont font = loadedFonts.get(id);
		if (font == null) {
			return defaultFont != null ? defaultFont : ImGui.getIO().getFontDefault();
		}
		return font;
	}

	public Set<FontDescription.Resource> getLoadedFontIds() {
		return Collections.unmodifiableSet(loadedFonts.keySet());
	}

	public void withFont(FontDescription.Resource font, Runnable runnable) {
		pushFont(font);
		runnable.run();
		popFont();
	}

	public void pushFont(FontDescription.Resource id) {
		ImGui.pushFont(getFont(id));
	}

	public void popFont() {
		ImGui.popFont();
	}

	private byte[] loadTtfBytes(FontDescription.Resource fontId, ResourceManager rm) {
		Identifier jsonPath = FONT_DEFINITIONS.idToFile(fontId.id());

		try (Reader reader = rm.getResource(jsonPath)
			.orElseThrow(() -> new EngineException("Missing font definition: " + jsonPath))
			.openAsReader()) {
			JsonElement json = GSON.fromJson(reader, JsonElement.class);
			List<GlyphProviderDefinition.Conditional> providers = GlyphProviderDefinition.Conditional.CODEC.listOf()
				.parse(JsonOps.INSTANCE, json.getAsJsonObject().get("providers"))
				.getOrThrow(JsonParseException::new);

			for (var conditional : providers) {
				if (conditional.definition() instanceof TrueTypeGlyphProviderDefinition ttfDef) {
					Identifier ttfPath = ttfDef.location().withPrefix("font/");
					try (var in = rm.getResource(ttfPath)
						.orElseThrow(() -> new EngineException("Missing TTF resource: " + ttfPath))
						.open()) {
						return in.readAllBytes();
					}
				}
			}
			throw new EngineException("No TTF provider in font definition: " + fontId);
		} catch (IOException e) {
			throw new EngineException("Failed to read font: " + fontId, e);
		}
	}
}
