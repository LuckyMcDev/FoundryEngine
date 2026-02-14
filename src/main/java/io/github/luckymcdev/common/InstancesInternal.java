package io.github.luckymcdev.common;

import io.github.luckymcdev.client.TbRenderer;
import io.github.luckymcdev.client.editor.BuiltInEditor;
import io.github.luckymcdev.client.imgui.ImGuiHandler;
import io.github.luckymcdev.client.opengl.OpenGlStack;
import io.github.luckymcdev.client.opengl.framebuffer.FrameBufferManager;
import io.github.luckymcdev.client.opengl.shaders.ShaderManager;
import io.github.luckymcdev.client.post.PostProcessManager;

class InstancesInternal {
    public static final OpenGlStack OPEN_GL_STACK = new OpenGlStack();
    public static final BuiltInEditor EDITOR = new BuiltInEditor();
    public static final TbRenderer RENDERER = new TbRenderer();
    public static final ImGuiHandler IMGUI_HANDLER = new ImGuiHandler();
    public static final PostProcessManager POST_PROCESS_MANAGER = new PostProcessManager();
    public static final FrameBufferManager FRAME_BUFFER_MANAGER = new FrameBufferManager();
    public static final ShaderManager SHADER_MANAGER = new ShaderManager();
}
