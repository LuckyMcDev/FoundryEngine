package io.github.luckymcdev.client.imgui.context;

import imgui.ImGui;
import imgui.binding.ImGuiStruct;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesContext;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.internal.ImGuiContext;

public class ImGuiContextTypes {

    // Built-in context types
    public static final ContextType<ImGuiContext> IMGUI = new ContextType<>() {
        @Override
        public ImGuiContext create() {
            return new ImGuiContext(ImGui.createContext().ptr);
        }

        @Override
        public ImGuiContext getCurrent() {
            return new ImGuiContext(ImGui.getCurrentContext().ptr);
        }

        @Override
        public void setCurrent(ImGuiContext context) {
            ImGui.setCurrentContext(context);
        }

        @Override
        public void destroy(ImGuiContext context) {
            ImGui.destroyContext(context);
        }
    };

    public static final ContextType<ImPlotContext> IMPLOT = new ContextType<>() {
        @Override
        public ImPlotContext create() {
            return new ImPlotContext(ImPlot.createContext().ptr);
        }

        @Override
        public ImPlotContext getCurrent() {
            return new ImPlotContext(ImPlot.getCurrentContext().ptr);
        }

        @Override
        public void setCurrent(ImPlotContext context) {
            ImPlot.setCurrentContext(context);
        }

        @Override
        public void destroy(ImPlotContext context) {
            ImPlot.destroyContext(context);
        }
    };

    public static final ContextType<ImNodesContext> IMNODES = new ContextType<>() {
        @Override
        public ImNodesContext create() {
            return new ImNodesContext(ImNodes.createContext().ptr);
        }

        @Override
        public ImNodesContext getCurrent() {
            return new ImNodesContext(ImNodes.getCurrentContext().ptr);
        }

        @Override
        public void setCurrent(ImNodesContext context) {
            ImNodes.setCurrentContext(context);
        }

        @Override
        public void destroy(ImNodesContext context) {
            ImNodes.destroyContext(context);
        }
    };
}
