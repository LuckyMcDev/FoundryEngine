package io.github.luckymcdev.common.font;

import io.github.luckymcdev.common.Commons;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public record TTFFile(ResourceLocation id, ResourceLocation resource) {
    // Predefined fonts
    public static final TTFFile JETBRAINS_MONO_NERDFONT_REGULAR =
            new TTFFile(ResourceLocation.fromNamespaceAndPath(Commons.MODID, "fonts/jetbrainsmononerdfontmono-regular"));

    public TTFFile(ResourceLocation id) {
        this(id, id.withPath(p -> p + ".ttf"));
    }

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
