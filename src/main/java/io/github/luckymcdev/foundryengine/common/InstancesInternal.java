package io.github.luckymcdev.foundryengine.common;

import io.github.luckymcdev.foundryengine.client.editor.BuiltInEditor;
import io.github.luckymcdev.foundryengine.client.imgui.ImGuiManager;
import io.github.luckymcdev.foundryengine.client.opengl.OpenGlStack;
import io.github.luckymcdev.foundryengine.client.opengl.framebuffer.FrameBufferManager;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ShaderManager;
import io.github.luckymcdev.foundryengine.client.post.PostProcessManager;
import io.github.luckymcdev.foundryengine.client.util.KeyBindingManager;

class InstancesInternal {
    static final OpenGlStack OPEN_GL_STACK = new OpenGlStack();
    static final BuiltInEditor EDITOR = new BuiltInEditor();
    static final ImGuiManager IMGUI_HANDLER = new ImGuiManager();
    static final PostProcessManager POST_PROCESS_MANAGER = new PostProcessManager();
    static final FrameBufferManager FRAME_BUFFER_MANAGER = new FrameBufferManager();
    static final ShaderManager SHADER_MANAGER = new ShaderManager();
    static final KeyBindingManager KEY_BINDING_MANAGER = new KeyBindingManager();
}
