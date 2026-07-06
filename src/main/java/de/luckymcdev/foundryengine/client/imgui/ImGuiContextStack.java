package de.luckymcdev.foundryengine.client.imgui;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesContext;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.internal.ImGuiContext;

public record ImGuiContextStack(ImGuiContext imGuiContext, ImPlotContext imPlotContext, ImNodesContext imNodesContext) {
	public ImGuiContextStack push() {
		var prevImGuiContext = new ImGuiContext(ImGui.getCurrentContext().ptr);
		var prevImPlotContext = new ImPlotContext(ImPlot.getCurrentContext().ptr);
		ImGui.setCurrentContext(imGuiContext);
		ImPlot.setCurrentContext(imPlotContext);
		ImNodes.setCurrentContext(imNodesContext);
		return new ImGuiContextStack(prevImGuiContext, prevImPlotContext, imNodesContext);
	}

	public void pop() {
		ImGui.setCurrentContext(imGuiContext);
		ImPlot.setCurrentContext(imPlotContext);
		ImNodes.setCurrentContext(imNodesContext);
	}

	public void destroy() {
		ImPlot.destroyContext(imPlotContext);
		ImGui.destroyContext(imGuiContext);
		ImNodes.destroyContext(imNodesContext);
	}
}
