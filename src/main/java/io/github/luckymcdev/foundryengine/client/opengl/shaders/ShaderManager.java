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

public class ShaderManager implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ShaderCompiler SHADER_COMPILER = new ShaderCompiler();
    private static final GLSLPreProcessorManager PRE_PROCESSOR_MANAGER = new GLSLPreProcessorManager();
    private static final GenericRegistry<Identifier, Shader> SHADERS = new GenericRegistry<>();
    private static final GenericRegistry<Identifier, ShaderProgram> PROGRAMS = new GenericRegistry<>();

    public void register(Shader shader) {
        SHADERS.register(shader.getId(), shader);
    }

    public void remove(Shader shader) {
        SHADERS.remove(shader.getId());
    }

    public void register(ShaderProgram program) {
        PROGRAMS.register(program.getId(), program);
    }

    public void remove(ShaderProgram program) {
        PROGRAMS.remove(program.getId());
    }

    public void reload() throws ShaderException {
        getCompiler().clearCache();

        for (Shader shader : SHADERS.getValues()) {
            LOGGER.info("reloading shader: {}", shader.getId());
            shader.reload();
        }
        for(ShaderProgram program : PROGRAMS.getValues() ) {
            LOGGER.info("reloading program: {}", program.getId());
            program.reload();
        }
    }

    public ShaderCompiler getCompiler() {
        return SHADER_COMPILER;
    }

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
