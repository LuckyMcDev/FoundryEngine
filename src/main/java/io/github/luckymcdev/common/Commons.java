package io.github.luckymcdev.common;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public interface Commons {
    Logger LOGGER = LogUtils.getLogger();
    String MODID = "foundryengine";

    static Identifier id(@NotNull String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    static String getRlSource(Identifier location) {
        return getRlSource(location, StandardCharsets.UTF_8);
    }

    static String getRlSource(Identifier location, Charset charset) {
        try (InputStream stream = Instances.getResourceManager().getResourceOrThrow(location).open();
             Reader reader = new InputStreamReader(stream, charset);
             BufferedReader br = new BufferedReader(reader)) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage());
            return "";
        }
    }
}
