package io.github.luckymcdev.foundryengine.client.opengl.shaders;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.compiler.ShaderCompiler;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.exeption.ShaderException;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.preprocessing.GLSLPreProcessorManager;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.program.ShaderProgram;
import io.github.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * A Manager for all Shaders. If you're just using the {@link io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline}
 * or any other Post Processing System you do not need to call this, it is called automatically.
 * However, if you're doing your own Shader stuff, you should register all your {@link Shader} and {@link ShaderProgram} here.
 */
public class ShaderManager implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ShaderCompiler SHADER_COMPILER = new ShaderCompiler();
    private static final GLSLPreProcessorManager PRE_PROCESSOR_MANAGER = new GLSLPreProcessorManager();
    private static final GenericRegistry<Identifier, Shader> SHADERS = new GenericRegistry<>();
    private static final GenericRegistry<Identifier, ShaderProgram> PROGRAMS = new GenericRegistry<>();

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
     * @param shader to remove
     */
    public void remove(Shader shader) {
        SHADERS.remove(shader.getId());
    }

    /**
     * Registers a ShaderProgram
     * @param program registrar
     */
    public void register(ShaderProgram program) {
        PROGRAMS.register(program.getId(), program);
    }

    /**
     * Removes a Shader Program
     * @param program to remove.
     */
    public void remove(ShaderProgram program) {
        PROGRAMS.remove(program.getId());
    }

    /**
     * Reloads All Shaders and Programs.
     * Shaders first, then Programs to avoid wrong ordering.
     * @throws ShaderException exception if something goes wrong when reloading.
     */
    public void reload() throws ShaderException {
        getCompiler().clearCache();

        for (Shader shader : SHADERS.values()) {
            LOGGER.info("reloading shader: {}", shader.getId());
            shader.reload();
        }
        for (ShaderProgram program : PROGRAMS.values()) {
            LOGGER.info("reloading program: {}", program.getId());
            program.reload();
        }
    }

    /**
     * Returns the Main Shader Compiler
     * @return the Shader Compiler
     */
    public ShaderCompiler getCompiler() {
        return SHADER_COMPILER;
    }

    /**
     * Returns the Glsl Pre Processor Manager.
     * @return the glsl pre processor manager.
     */
    public GLSLPreProcessorManager getPreProcessorManager() {
        return PRE_PROCESSOR_MANAGER;
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        try {
            LOGGER.info("Reloading Shader Manager");
            reload();
        } catch (ShaderException e) {
            LOGGER.error("{}{}", e.getMessage(), e.getGlError());
        }
    }
}
