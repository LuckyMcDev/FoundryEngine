package io.github.luckymcdev.common;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface Commons {
    String MODID = "toolboxlib";

    static ResourceLocation id(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
