package io.github.luckymcdev.foundryengine.client.imgui.context;

import imgui.binding.ImGuiStruct;

/**
 * A simple Wrapper for handling ImGuiStructs / Contexts.
 * See {@link ImGuiContextTypes#IMGUI}, {@link ImGuiContextTypes#IMNODES} and {@link ImGuiContextTypes#IMPLOT}
 * New Context Types have to be added to the {@link ImGuiContextStack} via {@link ImGuiContextStack#addContextType(ContextType)}
 *
 * @param <T> A Context of ImGuiStruct Type. eg {@link imgui.internal.ImGuiContext}
 */
public interface ContextType<T extends ImGuiStruct> {
    T create();

    T getCurrent();

    void setCurrent(T context);

    void destroy(T context);
}
