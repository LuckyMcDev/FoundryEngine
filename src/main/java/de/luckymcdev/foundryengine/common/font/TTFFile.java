package de.luckymcdev.foundryengine.common.font;

import de.luckymcdev.foundryengine.client.imgui.ImGuiManager;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * A Simple Record for a .ttf File for {@link ImGuiManager} to load.
 *
 * @param id       the Identifier of the Font.
 * @param resource the actual Location of the Font File.
 */
public record TTFFile(Identifier id, Identifier resource) {
    // The JetBrains Mono font which is the deafault for ImGui.
    public static final TTFFile JETBRAINS_MONO_NERDFONT_REGULAR =
            new TTFFile(Common.id("jetbrains_mono_nerd"), Common.id("fonts/jetbrainsmononerdfontmono-regular.ttf"));

    public static Object2ObjectOpenHashMap<Identifier, TTFFile> find(ResourceManager resourceManager) {
        var map = new Object2ObjectOpenHashMap<Identifier, TTFFile>();
        map.put(JETBRAINS_MONO_NERDFONT_REGULAR.id(), JETBRAINS_MONO_NERDFONT_REGULAR);
        return map;
    }

    /**
     * Loads the ttf File as a {@code byte[]}
     *
     * @param resourceManager the {@link ResourceManager} with which to load the File.
     * @return the byte array of data.
     */
    public byte[] load(ResourceManager resourceManager) {
        try (var in = resourceManager.getResource(resource).orElseThrow().open()) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new EngineException("Failed to read TTF file: " + id, e);
        }
    }
}
