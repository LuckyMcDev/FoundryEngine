package de.luckymcdev.foundryengine.client.imgui.context;

import imgui.binding.ImGuiStruct;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * The Context Stack to Manage All ImGui Contexts.
 * An ImGui Context is an {@link ContextType} Which is a wrapper around an {@link ImGuiStruct}
 */
@Deprecated()
public class ImGuiContextStack {
    private final Deque<ImGuiContextStack> stack = new ArrayDeque<>();
    private final List<ContextEntry<?>> contexts = new ArrayList<>();

    public <T extends ImGuiStruct> void addContextType(ContextType<T> type) {
        contexts.add(new ContextEntry<>(type));
    }

    /**
     * Pushes to the previous {@link ImGuiContextStack}
     */
    public void push() {
        ImGuiContextStack previous = new ImGuiContextStack();

        for (ContextEntry<?> entry : contexts) {
            entry.pushAndSavePrevious(previous);
        }

        stack.push(previous);
    }

    /**
     * Restores the Contexts from This Stack.
     */
    public void pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Context stack underflow");
        }

        ImGuiContextStack previous = stack.pop();
        previous.pop();
    }

    /**
     * Destroys all Contexts in this Stack.
     */
    public void destroy() {
        for (ContextEntry<?> entry : contexts) {
            entry.destroy();
        }
        contexts.clear();
    }

    /**
     * INTERNAL Storage for a Context Entry.
     *
     * @param <T> -
     */
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