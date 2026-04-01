package de.luckymcdev.foundryengine.client.editor.builtin.console;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.log.EngineLogAppender;
import de.luckymcdev.foundryengine.common.log.LogEntry;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiKey;
import imgui.type.ImString;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * A Panel to display the Console output in game. Can be used to see any errors without having to look
 * at an outside program.
 * Starts capturing the Log at {@link FMLConstructModEvent}
 */
public class ConsolePanel extends EditorPanel {
    public static final ConsolePanel INSTANCE = new ConsolePanel();

    private final ImString filter = new ImString(256);
    private final List<LogEntry> filteredCache = new ArrayList<>();
    private final ImString commandInput = new ImString(256);
    private String lastFilterText = "";
    private int lastLogSize = -1;
    private boolean autoScroll = true;

    private ConsolePanel() {
        super(Common.id("console"), "Console", ImIcons.FA.FA_ENVELOPE, Shortcut.ctrl(ImGuiKey.F1));
        menuBar = true;
        this.category = PanelCategory.EDITOR_CONSOLE;
    }

    private List<LogEntry> getFilteredLogs() {
        String currentFilter = filter.get().toLowerCase();
        int currentSize = EngineLogAppender.Holder.get().getHistory().size();

        // Only re-filter if something actually changed
        if (!currentFilter.equals(lastFilterText) || currentSize != lastLogSize) {
            filteredCache.clear();
            for (LogEntry entry : EngineLogAppender.Holder.get().getHistory()) {
                if (currentFilter.isEmpty() ||
                        entry.message().toLowerCase().contains(currentFilter) ||
                        entry.logger().toLowerCase().contains(currentFilter)) {
                    filteredCache.add(entry);
                }
            }
            lastFilterText = currentFilter;
            lastLogSize = currentSize;
        }
        return filteredCache;
    }

    @Override
    public void content() {
        //if(EngineImGuiUtils.requirePermissions()) return; Console doesnt require permissons? It only shows client logs anyway.
        renderControls();
        ImGui.separator();

        List<LogEntry> logsToRender = getFilteredLogs();

        ImGui.beginChild("##scrollingRegion", 0, -ImGui.getFrameHeightWithSpacing(), false);
        for (LogEntry entry : logsToRender) {
            boolean hasColor = pushLevelColor(entry.level());
            ImGui.textUnformatted(entry.format());
            if (hasColor) ImGui.popStyleColor();
        }

        if (autoScroll && ImGui.getScrollY() >= ImGui.getScrollMaxY()) {
            ImGui.setScrollHereY(1.0f);
        }

        ImGui.endChild();

        ImGui.separator();
        renderCommandInput();
    }

    private void renderControls() {
        if (ImGui.beginMenuBar()) {
            ImGui.setNextItemWidth(200);
            ImGui.inputTextWithHint("##Filter", "Filter...", filter);

            ImGui.separator();

            if (ImGui.menuItem("Clear")) {
                EngineLogAppender.Holder.get().clearHistory();
            }

            ImGui.separator();

            if (ImGui.menuItem("Auto-scroll", "", autoScroll)) {
                autoScroll = !autoScroll;
            }

            ImGui.separator();

            if (ImGui.button("Copy To Clipboard")) {
                StringBuilder sb = new StringBuilder();
                for (LogEntry entry : filteredCache) {
                    sb.append(entry.format()).append("\n");
                }
                ImGui.setClipboardText(sb.toString());
            }

            ImGui.endMenuBar();
        }
    }

    private void renderCommandInput() {
        ImGui.setNextItemWidth(-1);

        if (ImGui.inputTextWithHint("##CommandInput", "Enter command...", commandInput,
                ImGuiInputTextFlags.EnterReturnsTrue)) {
            executeCommand(commandInput.get());
            commandInput.set("");
        }

        if (ImGui.isWindowFocused() && ImGui.isKeyPressed(ImGuiKey.Enter)) {
            ImGui.setKeyboardFocusHere(-1);
        }
    }

    private void executeCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }

        String trimmedCommand = command.trim();

        if (trimmedCommand.startsWith("/")) {
            trimmedCommand = trimmedCommand.substring(1);
        }

        if (Client.getMinecraft().getConnection() != null) {
            Client.sendCommand(trimmedCommand);
        }
    }

    private boolean pushLevelColor(Level level) {
        int levelInt = level.intLevel();

        if (levelInt <= Level.ERROR.intLevel()) {
            ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 0.33f, 0.33f, 1.0f);
            return true;
        } else if (levelInt == Level.WARN.intLevel()) {
            ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 0.82f, 0.0f, 1.0f);
            return true;
        } else if (levelInt == Level.INFO.intLevel()) {
            return false;
        } else if (levelInt == Level.DEBUG.intLevel()) {
            ImGui.pushStyleColor(ImGuiCol.Text, 0.44f, 0.85f, 1.0f, 1.0f);
            return true;
        } else {
            ImGui.pushStyleColor(ImGuiCol.Text, 0.55f, 0.55f, 0.55f, 1.0f);
            return true;
        }
    }
}