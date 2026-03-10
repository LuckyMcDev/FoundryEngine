package io.github.luckymcdev.foundryengine.client.imgui.context;

import imgui.ImGui;
import imgui.binding.ImGuiStruct;
import imgui.internal.ImGuiContext;

/**
 * A simple Wrapper for handling ImGuiStructs / Contexts.
 * See {@link ImGuiContextTypes#IMGUI}, {@link ImGuiContextTypes#IMNODES} and {@link ImGuiContextTypes#IMPLOT}
 * New Context Types have to be added to the {@link ImGuiContextStack} via {@link ImGuiContextStack#addContextType(ContextType)}
 *
 * @param <T> A Context of ImGuiStruct Type. eg {@link imgui.internal.ImGuiContext}
 */
@Deprecated(forRemoval = true)
public interface ContextType<T extends ImGuiStruct> {
    /**
     * Create a new Context. Eg: {@link ImGui#createContext()}
     *
     * @return the context.
     */
    T create();

    /**
     * Get the current Context. Eg: {@link ImGui#getCurrentContext()}
     * @return the current Context.
     */
    T getCurrent();

    /**
     * Set the current Context to a different Context. Eg: {@link ImGui#setCurrentContext(ImGuiContext)}
     * @param context the Context to switch to.
     */
    void setCurrent(T context);

    /**
     * Destroy a Context. Eg: {@link ImGui#destroyContext(ImGuiContext)}
     * @param context the Context to destroy.
     */
    void destroy(T context);
}
