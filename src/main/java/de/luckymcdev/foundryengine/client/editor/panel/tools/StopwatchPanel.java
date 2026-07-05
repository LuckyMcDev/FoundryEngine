package de.luckymcdev.foundryengine.client.editor.panel.tools;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.config.PanelStyle;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Stopwatch;

public class StopwatchPanel extends EditorPanel {
    public static final StopwatchPanel INSTANCE = new StopwatchPanel("Temporary Stopwatch");
    private final String displayThing;
    private Stopwatch stopwatch;
    private boolean running = false;

    public StopwatchPanel(String id) {
        super(new Builder(Common.id("stopwatch-" + id.toLowerCase().replace(" ", "_")), Component.translatable("panel.foundryengine.stopwatch"))
                .icon(ImIcons.STOPWATCH)
                .category(PanelCategory.TOOLS)
                .temporary(true)
                .style(PanelStyle.MINIMAL));
        this.displayThing = id;
        this.stopwatch = new Stopwatch(System.currentTimeMillis(), 0L);
    }

    @Override
    protected void onPreWindow() {
        ImGui.setNextWindowSizeConstraints(260f, 0f, 600f, Float.MAX_VALUE);
    }

    @Override
    public void content(ImGraphicsExtractor g) {
        long now = System.currentTimeMillis();
        long displayTime = running ? stopwatch.elapsedMilliseconds(now) : stopwatch.accumulatedElapsedTime();

        ImGui.textDisabled(displayThing);
        ImGui.spacing();

        g.setNextItemWidth(ImGui.getContentRegionAvailX());
        ImGui.setWindowFontScale(2.5f);
        String timeStr = g.timer(displayTime);
        float textW = ImGui.calcTextSize(timeStr).x;
        float avail = ImGui.getContentRegionAvailX();
        ImGui.setCursorPosX(ImGui.getCursorPosX() + Math.max(0, (avail - textW) / 2));
        ImGui.textColored(running ? 0xFF4CAF50 : 0xFFCCCCCC, timeStr);
        ImGui.setWindowFontScale(1f);

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        float btnW = (ImGui.getContentRegionAvailX() - ImGui.getStyle().getItemSpacingX() * 2) / 3;

        if (ImGui.button(ImGraphicsExtractor.icon(ImIcons.ROTATE_RIGHT) + " Reset", btnW, 0)) {
            stopwatch = new Stopwatch(now, 0L);
            if (running) {
                stopwatch = new Stopwatch(now, 0L);
            }
        }

        ImGui.sameLine();

        if (ImGui.button(running ? ImGraphicsExtractor.icon(ImIcons.PAUSE) + " Pause" : ImGraphicsExtractor.icon(ImIcons.PLAY) + " Start", btnW, 0)) {
            if (!running) {
                stopwatch = new Stopwatch(now, stopwatch.accumulatedElapsedTime());
                running = true;
            } else {
                stopwatch = new Stopwatch(now, stopwatch.elapsedMilliseconds(now));
                running = false;
            }
        }

        ImGui.sameLine();

        if (ImGui.button(ImGraphicsExtractor.icon(ImIcons.CLOSE) + " Close", btnW, 0)) {
            close();
        }
    }
}
