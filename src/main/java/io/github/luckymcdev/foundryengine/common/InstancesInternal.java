package io.github.luckymcdev.foundryengine.common;

import io.github.luckymcdev.foundryengine.client.TbRenderer;
import io.github.luckymcdev.foundryengine.client.editor.BuiltInEditor;
import io.github.luckymcdev.foundryengine.client.imgui.ImGuiHandler;
import io.github.luckymcdev.foundryengine.client.opengl.OpenGlStack;
import io.github.luckymcdev.foundryengine.client.opengl.framebuffer.FrameBufferManager;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ShaderManager;
import io.github.luckymcdev.foundryengine.client.post.PostProcessManager;
import io.github.luckymcdev.foundryengine.client.util.KeyBindingManager;

class InstancesInternal {
    public static final OpenGlStack OPEN_GL_STACK = new OpenGlStack();
    public static final BuiltInEditor EDITOR = new BuiltInEditor();
    public static final TbRenderer RENDERER = new TbRenderer();
    public static final ImGuiHandler IMGUI_HANDLER = new ImGuiHandler();
    public static final PostProcessManager POST_PROCESS_MANAGER = new PostProcessManager();
    public static final FrameBufferManager FRAME_BUFFER_MANAGER = new FrameBufferManager();
    public static final ShaderManager SHADER_MANAGER = new ShaderManager();
    public static final KeyBindingManager KEY_BINDING_MANAGER = new KeyBindingManager();
}
