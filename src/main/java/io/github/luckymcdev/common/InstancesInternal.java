package io.github.luckymcdev.common;

import io.github.luckymcdev.client.TbRenderer;
import io.github.luckymcdev.client.editor.BuiltInEditor;
import io.github.luckymcdev.client.imgui.ImGuiHandler;

class InstancesInternal {
    public static final BuiltInEditor EDITOR = new BuiltInEditor();
    public static final TbRenderer RENDERER = new TbRenderer();
    public static final ImGuiHandler IMGUI_HANDLER = new ImGuiHandler();
}
