package de.luckymcdev.foundryengine.client.opengl.shaders;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.opengl.compiler.ShaderCompiler;
import de.luckymcdev.foundryengine.client.opengl.exeption.ShaderException;
import de.luckymcdev.foundryengine.client.opengl.preprocessing.GLSLPreProcessorManager;
import de.luckymcdev.foundryengine.client.opengl.program.ShaderProgram;
import de.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A Manager for all Shaders. If you're just using the {@link PostProcessPipeline}
 * or any other Post Processing System you do not need to call this, it is called automatically.
 * However, if you're doing your own Shader stuff, you should register all your {@link Shader} and {@link ShaderProgram} here.
 */
public class ShaderManager implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ShaderCompiler SHADER_COMPILER = new ShaderCompiler();
    private static final GLSLPreProcessorManager PRE_PROCESSOR_MANAGER = new GLSLPreProcessorManager();
    private static final GenericRegistry<Identifier, Shader> SHADERS = new GenericRegistry<>();
    private static final GenericRegistry<Identifier, ShaderProgram> PROGRAMS = new GenericRegistry<>();
    private final ConcurrentHashMap<Identifier, String> sourceCache = new ConcurrentHashMap<>();

    public ShaderManager() {
    }

    /**
     * Registers a Shader
     *
     * @param shader registrar
     */
    public void register(Shader shader) {
        SHADERS.register(shader.getId(), shader);
    }

    /**
     * Removes a Shader
     *
     * @param shader to remove
     */
    public void remove(Shader shader) {
        SHADERS.remove(shader.getId());
    }

    /**
     * Registers a ShaderProgram
     *
     * @param program registrar
     */
    public void register(ShaderProgram program) {
        PROGRAMS.register(program.getId(), program);
    }

    /**
     * Removes a Shader Program
     *
     * @param program to remove.
     */
    public void remove(ShaderProgram program) {
        PROGRAMS.remove(program.getId());
    }

    /**
     * Reloads All Shaders and Programs.
     * Shaders first, then Programs to avoid wrong ordering.
     *
     * @throws ShaderException exception if something goes wrong when reloading.
     */
    public void reload() throws ShaderException {
        // Clear caches to force fresh loads
        clearSourceCache();
        getCompiler().clearCache();

        for (Shader shader : SHADERS.values()) {
            LOGGER.debug("reloading shader: {}", shader.getId());
            shader.reload();
        }
        for (ShaderProgram program : PROGRAMS.values()) {
            LOGGER.debug("reloading program: {}", program.getId());
            program.reload();
        }
    }

    /**
     * Returns the Main Shader Compiler
     *
     * @return the Shader Compiler
     */
    public ShaderCompiler getCompiler() {
        return SHADER_COMPILER;
    }

    /**
     * Returns the Glsl Pre Processor Manager.
     *
     * @return the glsl pre processor manager.
     */
    public GLSLPreProcessorManager getPreProcessorManager() {
        return PRE_PROCESSOR_MANAGER;
    }

    /**
     * Gets shader source from cache or loads it from the file system.
     * This significantly reduces heap allocations by avoiding repeated file I/O
     * for the same shader sources.
     *
     * @param location the shader file location
     * @return the raw (unprocessed) shader source code
     */
    public String getCachedSource(Identifier location) {
        return sourceCache.computeIfAbsent(location, Client::getIdSource);
    }

    /**
     * Invalidates a specific shader source in the cache.
     * Useful for hot-reloading a single shader during development.
     *
     * @param location the shader file location to invalidate
     */
    public void invalidateSource(Identifier location) {
        sourceCache.remove(location);
        LOGGER.debug("Invalidated cached source for: {}", location);
    }

    /**
     * Clears all cached shader sources.
     * Called automatically during resource reload.
     */
    public void clearSourceCache() {
        int count = sourceCache.size();
        sourceCache.clear();
        LOGGER.debug("Cleared {} cached shader sources", count);
    }

    /**
     * Gets cache statistics for debugging and monitoring.
     *
     * @return formatted string with cache size and estimated memory usage
     */
    public String getSourceCacheStats() {
        int count = sourceCache.size();
        long totalChars = sourceCache.values().stream()
                .mapToInt(String::length)
                .sum();
        long estimatedKB = totalChars * 2 / 1024;

        return String.format("Shader Source Cache: %d files, ~%d KB", count, estimatedKB);
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        try {
            LOGGER.debug("Reloading Shader Manager");
            reload();
        } catch (ShaderException e) {
            LOGGER.error("{}{}", e.getMessage(), e.getGlError());
        }
    }
}