package io.github.luckymcdev.common.font;

import io.github.luckymcdev.common.Commons;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public record TTFFile(ResourceLocation id, ResourceLocation resource) {
    // Predefined fonts
    public static final TTFFile JETBRAINS_MONO_NERDFONT_REGULAR =
            new TTFFile(Commons.id("jetbrains_mono_nerd"), Commons.id("fonts/jetbrainsmononerdfontmono-regular.ttf"));

    public static Object2ObjectOpenHashMap<ResourceLocation, TTFFile> find(ResourceManager resourceManager) {
        var map = new Object2ObjectOpenHashMap<ResourceLocation, TTFFile>();
        map.put(JETBRAINS_MONO_NERDFONT_REGULAR.id(), JETBRAINS_MONO_NERDFONT_REGULAR);
        return map;
    }

    public byte[] load(ResourceManager resourceManager) throws IOException {
        try (var in = resourceManager.getResource(resource).orElseThrow().open()) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read TTF file: " + id, e);
        }
    }
}
