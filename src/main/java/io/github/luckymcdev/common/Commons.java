package io.github.luckymcdev.common;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public interface Commons {
    Logger LOGGER = LogUtils.getLogger();
    String MODID = "toolboxlib";

    static ResourceLocation id(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    static String getRlSource(ResourceLocation location, Charset charset) {
        try (InputStream stream = Instances.getResourceManager().getResourceOrThrow(location).open();
             Reader reader = new InputStreamReader(stream, charset);
             BufferedReader br = new BufferedReader(reader)) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage());
            return "";
        }
    }

    static String getRlSource(ResourceLocation location) {
        return getRlSource(location, StandardCharsets.UTF_8);
    }
}
