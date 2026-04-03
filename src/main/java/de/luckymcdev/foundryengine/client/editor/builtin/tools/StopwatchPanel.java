package de.luckymcdev.foundryengine.client.editor.builtin.tools;

import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelStyle;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import net.minecraft.world.Stopwatch;

public class StopwatchPanel extends EditorPanel {
    public static final StopwatchPanel INSTANCE = new StopwatchPanel("");
    private final String displayThing;
    private Stopwatch stopwatch;
    private boolean running = false;

    public StopwatchPanel(String id) {
        super(Common.id("stopwatch-" + id), "Stopwatch", ImIcons.FA.FA_STOPWATCH, Shortcut.empty());
        this.displayThing = id;
        this.temporary = true;
        this.style = PanelStyle.MINIMAL;
        this.stopwatch = new Stopwatch(System.currentTimeMillis(), 0L);
    }

    @Override
    public void content() {
        long now = System.currentTimeMillis();
        long displayTime = running ? stopwatch.elapsedMilliseconds(now) : stopwatch.accumulatedElapsedTime();

        ImGui.text(displayThing);

        if (ImGui.smallButton(ImGuiUtils.icon(ImIcons.FA.FA_ARROW_ROTATE_LEFT))) {
            stopwatch = new Stopwatch(now, 0L);
        }

        ImGui.sameLine();

        String label = "";
        if (running) label = ImGuiUtils.icon(ImIcons.FA.FA_CIRCLE_STOP);
        if (!running) label = ImGuiUtils.icon(ImIcons.FA.FA_CIRCLE_PLAY);
        if (ImGui.smallButton(label)) {
            if (!running) {
                stopwatch = new Stopwatch(now, stopwatch.accumulatedElapsedTime());
                running = true;
            } else {
                stopwatch = new Stopwatch(now, stopwatch.elapsedMilliseconds(now));
                running = false;
            }
        }

        ImGui.sameLine();
        ImGui.text(ImGuiUtils.timer(displayTime));

        ImGui.sameLine();
        if (ImGui.smallButton(ImGuiUtils.icon(ImIcons.FA.FA_CLOSE))) {
            close();
        }
    }
}