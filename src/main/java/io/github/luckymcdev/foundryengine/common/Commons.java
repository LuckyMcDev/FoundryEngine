package io.github.luckymcdev.foundryengine.common;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.stream.Collectors;

public interface Commons {
    // Consts
    Logger LOGGER = LogUtils.getLogger();
    String MODID = "foundryengine";
    String MODNAME = "Foundry Engine";

    // Config
    Path CONFIG = FMLPaths.CONFIGDIR.get();
    Path FOUNDRY_ENGINE_CONFIG = CONFIG.resolve(MODID);
    Path DATABASE_CONFIG = CONFIG.resolve("database");

    // Game Dir stuff
    Path GAME = FMLPaths.GAMEDIR.get();
    Path FOUNDRY_ENGINE = GAME.resolve("FoundryEngine");
    Path WORKSPACE = FOUNDRY_ENGINE.resolve("workspace");

    /**
     * Returns an {@link Identifier} where the namespace is {@link #MODID}
     *
     * @param path the Path of the {@link Identifier}
     * @return returns the assembled {@link Identifier}
     */
    static Identifier id(@NotNull String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    /**
     * Returns the Content of a {@link Identifier} pointer as a String.
     * @param location the Location where to get the Contents.
     * @return the String content.
     */
    static String getRlSource(Identifier location) {
        return getRlSource(location, StandardCharsets.UTF_8);
    }

    /**
     * {@link #getRlSource(Identifier)}
     * but with a specifiable {@link Charset}
     */
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
