package io.github.luckymcdev.foundryengine.common;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.client.util.KeyBinding;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A Place for Commonly used Constants.
 * Similar to {@link Instances} except tats for Instances.
 */
public interface Commons {
    /**
     * Common Logger. Don't use, create your own.
     */
    Logger LOGGER = LogUtils.getLogger();
    /** Modid for FoundryEngine. */
    String MODID = "foundryengine";
    /** Mod Name */
    String MODNAME = "Foundry Engine";

    /** Base Config Dir*/
    Path CONFIG = FMLPaths.CONFIGDIR.get();
    /** FoundryEngine config dir*/
    Path FOUNDRY_ENGINE_CONFIG = CONFIG.resolve(MODID);
    /** WIP database config dir.*/
    Path DATABASE_CONFIG = CONFIG.resolve("database");

    /** Game Dir*/
    Path GAME = FMLPaths.GAMEDIR.get();
    /** FoundryEngine Game Dir*/
    Path FOUNDRY_ENGINE = GAME.resolve("FoundryEngine");
    /**
     * Bundles Path
     */
    Path BUNDLES = FOUNDRY_ENGINE.resolve("bundles");
    /**
     * Cache Path
     */
    Path CACHE = FOUNDRY_ENGINE.resolve(".cache");
    /**
     * Config Path
     */
    Path CONFIG_FE = FOUNDRY_ENGINE.resolve("config");


    KeyBinding EDITOR_KEY = new KeyBinding(
            new KeyMapping(
                    Component.translatable("key.foundryengine.editor").getString(),
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_F6,
                    KeyMapping.Category.DEBUG
            ),
            () -> {
            }
    );

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
    static String getIdSource(Identifier location) {
        return getIdSource(location, StandardCharsets.UTF_8);
    }

    /**
     * {@link #getIdSource(Identifier)}
     * but with a specifiable {@link Charset}
     * @param location the Identifier.
     * @param charset the {@link Charset} with which to load the Identifier
     * @return the String Content of the File.
     */
    static String getIdSource(Identifier location, Charset charset) {
        try (InputStream stream = Instances.getResourceManager().getResourceOrThrow(location).open();
             Reader reader = new InputStreamReader(stream, charset);
             BufferedReader br = new BufferedReader(reader)) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage() + Arrays.toString(e.getStackTrace()));
            return "";
        }
    }

    static <V> Supplier<V> supOf(V value) {
        return () -> value;
    }

    /**
     * Utility Method used by most Constructors of Managers in {@link InstancesInternal}
     * to make sure users don't instantiate them.
     *
     * @param target the class which requires only internal access.
     */
    static void requireInternalAccess(Class<?> target) {
        Class<?> caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(frames -> frames
                        .map(StackWalker.StackFrame::getDeclaringClass)
                        .filter(clazz -> clazz != Commons.class && clazz != target)
                        .findFirst()
                        .orElse(null));

        if (caller != InstancesInternal.class) {
            throw new IllegalCallerException(
                    "Engine Security Violation: [" + target.getName() + "] cannot be created manually.\n" +
                            "Refer to the API at: " + Instances.class.getName()
            );
        }
    }
}
