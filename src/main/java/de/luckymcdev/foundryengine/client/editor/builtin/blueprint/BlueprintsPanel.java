package de.luckymcdev.foundryengine.client.editor.builtin.blueprint;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.imgui.imnodes.NodeEditorInstance;
import de.luckymcdev.foundryengine.client.imgui.imnodes.blueprint.BlueprintEngine;
import de.luckymcdev.foundryengine.client.imgui.imnodes.blueprint.BlueprintEngine.NodeTemplate;
import de.luckymcdev.foundryengine.client.imgui.imnodes.blueprint.BlueprintSerializer;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Editor panel hosting the Blueprint node editor canvas.
 */
public class BlueprintsPanel extends EditorPanel {
    public static final BlueprintsPanel INSTANCE = new BlueprintsPanel();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final BlueprintEngine engine;
    private final NodeEditorInstance<Void> editor;
    private final BlueprintSerializer serializer;
    private final imgui.type.ImString searchFilter = new imgui.type.ImString(128);
    private final imgui.type.ImString fileNameInput = new imgui.type.ImString(256);
    private final Path blueprintsDirectory;

    protected BlueprintsPanel() {
        super(Common.id("blueprints"), "Blueprints", ImIcons.FA.FA_MAP, Shortcut.empty());
        this.category = PanelCategory.EDITOR_BLUEPRINTS;

        this.engine = new BlueprintEngine();
        this.editor = new NodeEditorInstance<>(engine.execType, engine);
        this.serializer = new BlueprintSerializer(engine);

        engine.registerBuiltins();

        this.blueprintsDirectory = Common.CACHE.resolve("blueprints");

        try {
            Files.createDirectories(blueprintsDirectory);
        } catch (IOException e) {
            LOGGER.error("Failed to create blueprints directory", e);
        }

        // engine.node("My Mod", "Spawn Entity")
        //     .in(engine.execType,  "In")
        //     .in(engine.stringType,"Entity ID").defaultValue("Entity ID", "minecraft:zombie")
        //     .in(engine.vectorType,"Location")
        //     .out(engine.execType, "Out")
        //     .behavior((n, e, ed, ctx) -> {
        //         String id = ctx.resolvePinAs(n.inputPin("Entity ID"), String.class, "");
        //         LOGGER.info("Spawning entity: {}", id);
        //     })
        //     .register();
    }

    @Override
    public void content() {
        if (ImGui.button(ImIcons.FA.FA_PLAY + " Run")) {
            engine.executeGraph(editor);
        }
        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_TRASH + " Clear")) {
            editor.clear();
        }
        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_SAVE + " Save")) {
            ImGui.openPopup("###save-blueprint");
        }
        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_FOLDER_OPEN + " Load")) {
            ImGui.openPopup("###load-blueprint");
        }
        ImGui.sameLine();
        ImGui.textDisabled("Nodes: " + editor.nodes.size());
        ImGui.separator();

        renderSaveDialog();
        renderLoadDialog();

        editor.render(
                ignored -> renderNodePalette(),
                node -> {
                }
        );
    }

    private void renderSaveDialog() {
        if (ImGui.beginPopupModal("###save-blueprint", new ImBoolean(true), imgui.flag.ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.text("Blueprint name:");
            ImGui.setNextItemWidth(250f);
            ImGui.inputText("##bp-name", fileNameInput);

            if (ImGui.button("Save##btn", 80, 0)) {
                String fileName = fileNameInput.get().trim();
                if (!fileName.isEmpty()) {
                    saveBlueprint(fileName);
                    fileNameInput.set("");
                    ImGui.closeCurrentPopup();
                } else {
                    LOGGER.warn("Blueprint name cannot be empty");
                }
            }

            ImGui.sameLine();
            if (ImGui.button("Cancel##btn", 80, 0)) {
                fileNameInput.set("");
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }

    private void renderLoadDialog() {
        if (ImGui.beginPopupModal("###load-blueprint", new ImBoolean(true), imgui.flag.ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.text("Available blueprints:");
            ImGui.separator();

            try {
                var blueprintFiles = Files.list(blueprintsDirectory)
                        .filter(p -> p.toString().endsWith(".json"))
                        .toList();

                if (blueprintFiles.isEmpty()) {
                    ImGui.textDisabled("No blueprints found");
                } else {
                    for (var file : blueprintFiles) {
                        String name = file.getFileName().toString();
                        if (ImGui.menuItem(name)) {
                            loadBlueprint(file);
                            ImGui.closeCurrentPopup();
                        }
                    }
                }
            } catch (IOException e) {
                ImGui.textColored(1.0f, 0.0f, 0.0f, 1.0f, "Error reading blueprints: " + e.getMessage());
            }

            ImGui.separator();
            if (ImGui.button("Close##btn", 80, 0)) {
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }

    private void saveBlueprint(String fileName) {
        try {
            if (!fileName.endsWith(".json")) {
                fileName += ".json";
            }

            Path filePath = blueprintsDirectory.resolve(fileName);
            String json = serializer.serialize(editor);
            Files.writeString(filePath, json);
            LOGGER.info("Blueprint saved to {}", filePath);
        } catch (IOException e) {
            LOGGER.error("Failed to save blueprint", e);
        }
    }

    private void loadBlueprint(Path filePath) {
        try {
            String json = Files.readString(filePath);
            serializer.deserialize(json, editor);
            LOGGER.info("Blueprint loaded from {}", filePath);
        } catch (IOException e) {
            LOGGER.error("Failed to load blueprint", e);
        }
    }

    private void renderNodePalette() {
        ImGui.setNextItemWidth(200f);
        ImGui.inputTextWithHint("##bp-search", "Search nodes...", searchFilter);
        ImGui.separator();

        String filter = searchFilter.get().toLowerCase().trim();

        Map<String, List<NodeTemplate>> grouped = new LinkedHashMap<>();
        for (var template : engine.getRegistry()) {
            if (!filter.isEmpty() && !template.name().toLowerCase().contains(filter)
                    && !template.category().toLowerCase().contains(filter)) {
                continue;
            }
            grouped.computeIfAbsent(template.category(), k -> new java.util.ArrayList<>())
                    .add(template);
        }

        if (grouped.isEmpty()) {
            ImGui.textDisabled("No matching nodes.");
        }

        for (var entry : grouped.entrySet()) {
            String cat = entry.getKey();
            if (ImGui.treeNodeEx(cat, ImGuiTreeNodeFlags.DefaultOpen)) {
                for (var template : entry.getValue()) {
                    if (ImGui.menuItem(template.name() + "##" + cat)) {
                        editor.addNode(template);
                        searchFilter.set("");
                    }
                }
                ImGui.treePop();
            }
        }

        ImGui.separator();
        if (ImGui.menuItem("Clear All")) {
            editor.clear();
        }
    }
}