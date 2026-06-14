package de.luckymcdev.foundryengine.client.editor.panel.editor;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.api.event.ClientEvents;
import de.luckymcdev.foundryengine.client.blueprint.editor.NodeEditorInstance;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;
import de.luckymcdev.foundryengine.common.blueprint.serial.BlueprintSerializer;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BlueprintsPanel extends EditorPanel {
    public static final BlueprintsPanel INSTANCE = new BlueprintsPanel();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final BlueprintEngine engine;
    private final NodeEditorInstance editor;
    private final BlueprintSerializer serializer;
    private final ImString searchFilter = new ImString(128);
    private final ImString fileNameInput = new ImString(256);
    private final Path blueprintsDirectory;

    protected BlueprintsPanel() {
        super(Common.id("blueprints"), "Blueprints", ImIcons.FA.FA_MAP, PanelCategory.EDITOR);
        this.engine = new BlueprintEngine();
        this.editor = new NodeEditorInstance(engine);
        this.serializer = new BlueprintSerializer(engine);

        engine.registerBuiltins();

        this.blueprintsDirectory = Common.CACHE.resolve("blueprints");

        try {
            Files.createDirectories(blueprintsDirectory);
        } catch (IOException e) {
            LOGGER.error("Failed to create blueprints directory", e);
        }

        ClientEvents.tick(event -> {
            engine.executeEvent("event.client_tick", editor.graph);
        });
    }

    private static void renderCategoryTree(CategoryNode node, String path, int flags,
                                           java.util.function.Consumer<BuiltinNode> onPick) {
        for (var builtin : node.builtins) {
            if (ImGui.menuItem(builtin.name + "##" + builtin.identifier)) {
                onPick.accept(builtin);
            }
        }

        for (var entry : node.children.entrySet()) {
            String name = entry.getKey();
            CategoryNode child = entry.getValue();
            String childPath = path.isEmpty() ? name : path + "/" + name;
            if (ImGui.treeNodeEx(name + "##cat-" + childPath, flags)) {
                renderCategoryTree(child, childPath, flags, onPick);
                ImGui.treePop();
            }
        }
    }

    @Override
    public void content() {
        if (ImGui.button(ImIcons.FA.FA_PLAY + " Run")) {
            engine.executeGraph(editor.graph);
        }
        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_TRASH + " Clear")) {
            editor.graph.clear();
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
        ImGui.textDisabled("Nodes: " + editor.graph.nodes.size());
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
                        .filter(p -> p.toString().endsWith(BlueprintSerializer.EXTENSION)).toList();

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
        if (!fileName.endsWith(BlueprintSerializer.EXTENSION)) {
            fileName += BlueprintSerializer.EXTENSION;
        }

        Path filePath = blueprintsDirectory.resolve(fileName);
        try {
            serializer.saveToFile(editor.graph, editor::getNodeGridPos, filePath);
        } catch (IOException e) {
            LOGGER.error("Failed to save blueprint to {}", filePath, e);
        }
    }

    private void loadBlueprint(Path filePath) {
        try {
            Map<Integer, float[]> positions = serializer.loadFromFile(filePath, editor.graph);
            positions.forEach((nodeId, pos) -> editor.setNodeGridPos(nodeId, pos[0], pos[1]));
        } catch (IOException e) {
            LOGGER.error("Failed to load blueprint from {}", filePath, e);
        }
    }

    private void renderNodePalette() {
        if (ImGui.isWindowAppearing()) {
            ImGui.setKeyboardFocusHere();
        }

        ImGui.setNextItemWidth(200f);
        ImGui.inputTextWithHint("##bp-search", "Search nodes...", searchFilter);
        ImGui.separator();

        String filter = searchFilter.get().toLowerCase().trim();

        CategoryNode root = new CategoryNode();
        for (var builtin : engine.getBuiltinNodes()) {
            String displayName = builtin.name;
            String category = builtin.category;
            if (!filter.isEmpty()
                    && !displayName.toLowerCase().contains(filter)
                    && !builtin.identifier.toLowerCase().contains(filter)
                    && !category.toLowerCase().contains(filter)) {
                continue;
            }

            CategoryNode cur = root;
            for (String part : category.split("/")) {
                if (part.isBlank()) continue;
                cur = cur.children.computeIfAbsent(part, k -> new CategoryNode());
            }
            cur.builtins.add(builtin);
        }

        if (root.isEmpty()) {
            ImGui.textDisabled("No matching nodes.");
        }

        int flags = filter.isEmpty() ? ImGuiTreeNodeFlags.None : ImGuiTreeNodeFlags.DefaultOpen;
        renderCategoryTree(root, "", flags, builtin -> {
            editor.addNodeAtOrigin(builtin);
            searchFilter.set("");
        });

        ImGui.separator();
        if (ImGui.menuItem("Clear All")) {
            editor.graph.clear();
        }
    }

    private static final class CategoryNode {
        final Map<String, CategoryNode> children = new LinkedHashMap<>();
        final List<BuiltinNode> builtins = new java.util.ArrayList<>();

        boolean isEmpty() {
            if (!builtins.isEmpty()) return false;
            for (var c : children.values()) if (!c.isEmpty()) return false;
            return true;
        }
    }
}
