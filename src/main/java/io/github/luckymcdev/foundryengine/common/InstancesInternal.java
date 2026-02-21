package io.github.luckymcdev.foundryengine.common;

import io.github.luckymcdev.foundryengine.client.editor.EditorManager;
import io.github.luckymcdev.foundryengine.client.imgui.ImGuiManager;
import io.github.luckymcdev.foundryengine.client.opengl.OpenGlStack;
import io.github.luckymcdev.foundryengine.client.opengl.framebuffer.FrameBufferManager;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ShaderManager;
import io.github.luckymcdev.foundryengine.client.post.PostProcessManager;
import io.github.luckymcdev.foundryengine.client.util.KeyBindingManager;
import io.github.luckymcdev.foundryengine.common.thread.ThreadManager;

/**
 * Internal storage for engine singleton instances.
 * Access these via the {@link Instances} interface.
 */
public final class InstancesInternal {

    // Core Systems
    static final OpenGlStack OPEN_GL_STACK;
    static final ShaderManager SHADER_MANAGER;
    static final FrameBufferManager FRAME_BUFFER_MANAGER;
    // Rendering
    static final PostProcessManager POST_PROCESS_MANAGER;
    // UI
    static final ImGuiManager IMGUI_MANAGER;
    static final EditorManager EDITOR_MANAGER;
    // Utilities
    static final KeyBindingManager KEY_BINDING_MANAGER;
    static final ThreadManager THREAD_MANAGER;

    static {
        OPEN_GL_STACK = new OpenGlStack();
        SHADER_MANAGER = new ShaderManager();
        FRAME_BUFFER_MANAGER = new FrameBufferManager();

        POST_PROCESS_MANAGER = new PostProcessManager();

        IMGUI_MANAGER = new ImGuiManager();
        EDITOR_MANAGER = new EditorManager();

        KEY_BINDING_MANAGER = new KeyBindingManager();
        THREAD_MANAGER = new ThreadManager();
    }

    private InstancesInternal() {
        throw new UnsupportedOperationException("This is a static instance holder and cannot be instantiated.");
    }
}
