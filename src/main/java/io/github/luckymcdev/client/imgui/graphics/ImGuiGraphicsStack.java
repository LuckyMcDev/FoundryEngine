package io.github.luckymcdev.client.imgui.graphics;

import imgui.ImFont;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;

import java.util.ArrayDeque;
import java.util.Deque;

public class ImGuiGraphicsStack {
    private final Deque<StackFrame> stack = new ArrayDeque<>();

    private StackFrame currentFrame() {
        if (stack.isEmpty()) {
            throw new IllegalStateException(
                    "No stack frame exists. You must call push() before pushing style vars or colors."
            );
        }
        return stack.peek();
    }

    private void pushStyleVarInternal(Runnable pushAction) {
        currentFrame(); // ensures a frame exists
        pushAction.run();
        stack.peek().styleVarCount++;
    }

    public void pushStyleVar(int styleVar, float value) {
        pushStyleVarInternal(() -> ImGui.pushStyleVar(styleVar, value));
    }

    public void pushStyleVar(int styleVar, float x, float y) {
        pushStyleVarInternal(() -> ImGui.pushStyleVar(styleVar, x, y));
    }

    public void pushStyleVar(int styleVar, ImVec2 value) {
        pushStyleVarInternal(() -> ImGui.pushStyleVar(styleVar, value));
    }

    private void pushStyleColorInternal(Runnable pushAction) {
        currentFrame();
        pushAction.run();
        stack.peek().styleColorCount++;
    }

    public void pushStyleColor(int styleColor, int color) {
        pushStyleColorInternal(() -> ImGui.pushStyleColor(styleColor, color));
    }

    public void pushStyleColor(int styleColor, float r, float g, float b, float a) {
        pushStyleColorInternal(() -> ImGui.pushStyleColor(styleColor, r, g, b, a));
    }

    public void pushStyleColor(int styleColor, int r, int g, int b, int a) {
        pushStyleColorInternal(() -> ImGui.pushStyleColor(styleColor, r, g, b, a));
    }

    public void pushStyleColor(int styleColor, ImVec4 color) {
        pushStyleColorInternal(() -> ImGui.pushStyleColor(styleColor, color));
    }

    private void pushFontInternal(Runnable action) {
        currentFrame();
        action.run();
        stack.peek().fontCount++;
    }

    public void pushFont(ImFont font) {
        pushFontInternal(() -> ImGui.pushFont(font));
    }

    private void pushFontScaleInternal(Runnable action) {
        currentFrame();
        action.run();
        stack.peek().fontScale++;
    }

    public void push() {
        stack.push(new StackFrame());
    }

    public void pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Cannot pop from empty ImGuiGraphicsStack");
        }

        StackFrame frame = stack.pop();
        for (int i = 0; i < frame.fontCount; i++) {
            ImGui.popFont();
        }
        if (frame.styleVarCount > 0) {
            ImGui.popStyleVar(frame.styleVarCount);
        }
        if (frame.styleColorCount > 0) {
            ImGui.popStyleColor(frame.styleColorCount);
        }
    }

    public void clear() {
        while (!stack.isEmpty()) {
            pop();
        }
    }

    public void destroy() {
        clear();
    }

    public int getDepth() {
        return stack.size();
    }

    private static class StackFrame {
        int styleVarCount = 0;
        int styleColorCount = 0;
        int fontCount = 0;
        float fontScale = 20F;
    }
}
