package io.github.luckymcdev.client.imgui.context;

import imgui.binding.ImGuiStruct;

import java.util.ArrayList;
import java.util.List;

public class ImGuiContextStack {
    private final List<ContextEntry<?>> contexts = new ArrayList<>();

    public ImGuiContextStack() {
    }

    // Add a context type
    public <T extends ImGuiStruct> void addContextType(ContextType<T> type) {
        contexts.add(new ContextEntry<>(type));
    }

    // Push: switch to stored contexts, return previous ones
    public ImGuiContextStack push() {
        ImGuiContextStack previous = new ImGuiContextStack();

        for (ContextEntry<?> entry : contexts) {
            // Save current context and switch
            entry.pushAndSavePrevious(previous);
        }

        return previous;
    }

    // Pop: restore contexts from this stack
    public void pop() {
        for (ContextEntry<?> entry : contexts) {
            entry.restore();
        }
    }

    // Destroy all contexts in this stack
    public void destroy() {
        for (ContextEntry<?> entry : contexts) {
            entry.destroy();
        }
        contexts.clear();
    }

    // Internal storage
    private static class ContextEntry<T extends ImGuiStruct> {
        final ContextType<T> type;
        final T context;

        ContextEntry(ContextType<T> type) {
            this.type = type;
            this.context = type.create();
        }

        ContextEntry(ContextType<T> type, T context) {
            this.type = type;
            this.context = context;
        }

        void pushAndSavePrevious(ImGuiContextStack previous) {
            // Save current context
            T currentContext = type.getCurrent();
            previous.contexts.add(new ContextEntry<>(type, currentContext));

            // Switch to this entry's context
            type.setCurrent(context);
        }

        void restore() {
            type.setCurrent(context);
        }

        void destroy() {
            type.destroy(context);
        }
    }
}