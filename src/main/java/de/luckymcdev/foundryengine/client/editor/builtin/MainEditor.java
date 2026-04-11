package de.luckymcdev.foundryengine.client.editor.builtin;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import imgui.flag.ImGuiKey;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.slf4j.Logger;

import java.util.List;

public class MainEditor extends EditorPanel {
    public static final MainEditor INSTANCE = new MainEditor();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ImString inputId = new ImString();
    private final ImString inputName = new ImString();
    private final ImString inputAuthors = new ImString();
    private final ImInt inputMajorVersion = new ImInt();
    private final ImInt inputMinorVersion = new ImInt();
    private final ImInt inputPatchVersion = new ImInt();
    private boolean wantsNew = false;

    private MainEditor() {
        super(Common.id("main_editor"), "Main Editor", ImIcons.FA.FA_EDIT, Shortcut.ctrl(ImGuiKey.F9));
        this.category = PanelCategory.EDITOR;
        this.menuBar = true;
    }

    @Override
    public void content() {
        renderMenuBar();
        renderCreateMenu();
    }

    private void renderMenuBar() {
        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("Files")) {
                if (ImGui.menuItem("Create New")) {
                    wantsNew = true;
                }
                ImGui.endMenu();
            }
            ImGui.endMenuBar();
        }
    }

    private void renderCreateMenu() {
        if (wantsNew) {
            if (ImGui.begin("Create new Bundle")) {
                ImGui.inputText("Bundle Id", inputId);
                ImGui.inputText("Bundle Name", inputName);
                ImGui.inputText("Bundle Authors", inputAuthors);
                ImGui.inputInt("Bundle Version", inputMajorVersion);
                ImGui.inputInt("##MinorVersion", inputMinorVersion);
                ImGui.inputInt("##PatchVersion", inputPatchVersion);

                ImGuiUtils.centered(() -> {
                    if (ImGui.button("Create", 100, 0)) {
                        String[] authorsArray = inputAuthors.get().split(",");

                        List<String> authorsList = java.util.Arrays.stream(authorsArray)
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .toList();

                        wantsNew = false;
                    }
                }, 100, ImGui.getWindowWidth());

                ImGui.end();
            }
        }
    }
}
