package io.github.luckymcdev.foundryengine.client.imgui.context;

import imgui.binding.ImGuiStruct;

public interface ContextType<T extends ImGuiStruct> {
    T create();

    T getCurrent();

    void setCurrent(T context);

    void destroy(T context);
}
