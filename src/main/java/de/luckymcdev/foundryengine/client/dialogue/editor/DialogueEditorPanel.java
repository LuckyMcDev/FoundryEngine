package de.luckymcdev.foundryengine.client.dialogue.editor;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.dialogue.DialogueNode;
import de.luckymcdev.foundryengine.common.dialogue.DialogueOption;
import de.luckymcdev.foundryengine.common.dialogue.DialogueTree;
import de.luckymcdev.foundryengine.common.network.packets.dialogue.DialogueSavePacket;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiSelectableFlags;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;

public class DialogueEditorPanel extends EditorPanel {
    public static final DialogueEditorPanel INSTANCE = new DialogueEditorPanel();

    private final ArrayList<DialogueTree> trees = new ArrayList<>();
    private final ImString editSpeaker = new ImString("", 64);
    private final ImString editText = new ImString("", 4096);
    private final ImString editNextNode = new ImString("", 64);
    private final float[] editSpeakerColor = new float[]{0.33f, 1.0f, 0.33f, 1.0f};
    private final ImString newTreeId = new ImString(64);
    private final ImString newNodeId = new ImString(64);
    private final ImString newNodeSpeaker = new ImString(64);
    private final ImString newNodeText = new ImString(4096);
    private final ImString newOptionText = new ImString(256);
    private final ImString newOptionTarget = new ImString(64);
    private int selectedTree = -1;
    private int selectedNode = -1;
    private int lastEditedNode = -1;
    private int lastEditedTree = -1;
    private boolean showNewTree;
    private boolean showNewNode;
    private boolean showNewOption;

    private DialogueEditorPanel() {
        super(new Builder(Common.id("dialogue_editor"), "Dialogue Editor")
                .icon(ImIcons.FA.FA_COMMENT)
                .category(PanelCategory.EDITOR)
                .menuBar(true));
    }

    @Override
    public void content() {
        if (!requireWorld()) return;

        renderMenuBar();
        ImGui.separator();

        beginContent();
        renderTreePanel();
        ImGui.sameLine();
        renderDetailPanel();
        endContent();
    }

    @Override
    public void onOpened() {
        loadFromServer();
    }

    private void renderMenuBar() {
        menuBar(() -> {
            if (ImGui.menuItem(ImIcons.FA.FA_PLUS + " New Tree")) {
                showNewTree = true;
                newTreeId.set("");
            }
            if (ImGui.menuItem(ImIcons.FA.FA_SAVE + " Save")) {
                saveToServer();
            }
            if (ImGui.menuItem(ImIcons.FA.FA_DOWNLOAD + " Load")) {
                loadFromServer();
            }
            if (ImGui.menuItem(ImIcons.FA.FA_TRASH + " Delete Tree")) {
                if (selectedTree >= 0 && selectedTree < trees.size()) {
                    trees.remove(selectedTree);
                    selectedTree = Math.min(selectedTree, trees.size() - 1);
                    selectedNode = -1;
                }
            }
        });
    }

    private void renderTreePanel() {
        ImGui.beginChild("##tree_panel", 200f, 0, true);

        if (showNewTree) {
            ImGui.setNextItemWidth(-1);
            boolean done = ImGui.inputTextWithHint("##ntid", "namespace:path", newTreeId, ImGuiInputTextFlags.EnterReturnsTrue);
            if (ImGui.button("Create") || done) {
                var id = Identifier.parse(newTreeId.get().trim());
                var tree = new DialogueTree(id, "start");
                tree.addNode(new DialogueNode("start", "NPC", "Hello!"));
                trees.add(tree);
                selectedTree = trees.size() - 1;
                selectedNode = 0;
                syncEditFields();
                showNewTree = false;
            }
            ImGui.sameLine();
            if (ImGui.button("Cancel")) showNewTree = false;
            ImGui.separator();
        }

        if (trees.isEmpty()) {
            ImGui.textDisabled("No trees.");
        } else {
            for (int i = 0; i < trees.size(); i++) {
                var t = trees.get(i);
                boolean sel = i == selectedTree;
                ImGui.pushID("t" + i);
                if (ImGui.selectable(t.getId().toString(), sel, ImGuiSelectableFlags.None)) {
                    selectedTree = sel ? -1 : i;
                    selectedNode = -1;
                    syncEditFields();
                }
                ImGui.popID();
            }
        }

        ImGui.endChild();
    }

    private void renderDetailPanel() {
        ImGui.beginChild("##detail_panel", 0, 0, false);

        if (selectedTree < 0 || selectedTree >= trees.size()) {
            ImGui.textDisabled("Select a tree on the left.");
            ImGui.endChild();
            return;
        }

        var tree = trees.get(selectedTree);

        renderTreeHeader(tree);
        ImGui.separator();
        renderNodeBar(tree);
        ImGui.separator();
        renderNodeEdit(tree);
        ImGui.separator();
        renderOptionList(tree);

        ImGui.endChild();
    }

    private void renderTreeHeader(DialogueTree tree) {
        ImGui.text("Tree: " + tree.getId());

        var nodeIds = new ArrayList<>(tree.getNodes().keySet());
        int rootIdx = Math.max(0, nodeIds.indexOf(tree.getRootNodeId()));
        var ri = new ImInt(rootIdx);
        String[] items = nodeIds.toArray(String[]::new);
        if (ImGui.combo("Root Node", ri, items)) {
            tree.setRootNodeId(nodeIds.get(ri.get()));
        }

        ImGui.sameLine();
        if (ImGui.button("Add Node")) {
            showNewNode = true;
            newNodeId.set("node_" + tree.getNodes().size());
            newNodeSpeaker.set("NPC");
            newNodeText.set("");
        }
    }

    private void renderNodeBar(DialogueTree tree) {
        var nodes = new ArrayList<>(tree.getNodes().values());

        if (showNewNode) {
            ImGui.text("New Node:");
            ImGui.sameLine();
            ImGui.setNextItemWidth(80);
            ImGui.inputText("##nnid", newNodeId);
            ImGui.sameLine();
            ImGui.setNextItemWidth(80);
            ImGui.inputText("##nnspeaker", newNodeSpeaker);
            if (ImGui.button("Add")) {
                tree.addNode(new DialogueNode(newNodeId.get().trim(), newNodeSpeaker.get(), newNodeText.get()));
                selectedNode = new ArrayList<>(tree.getNodes().values()).size() - 1;
                syncEditFields();
                showNewNode = false;
            }
            ImGui.sameLine();
            if (ImGui.button("X")) showNewNode = false;
        }

        float avail = ImGui.getContentRegionAvailX();
        float btnW = Math.max(80f, (avail - 8f) / Math.max(nodes.size(), 1));

        for (int i = 0; i < nodes.size(); i++) {
            var n = nodes.get(i);
            boolean sel = i == selectedNode;
            var color = sel ? ImIcons.FA.FA_CIRCLE : null;
            if (ImGui.button(n.getId() + "##ns", btnW, 0)) {
                selectedNode = sel ? -1 : i;
                syncEditFields();
            }
            ImGui.sameLine();
        }
    }

    private void renderNodeEdit(DialogueTree tree) {
        var nodes = new ArrayList<>(tree.getNodes().values());

        if (selectedNode < 0 || selectedNode >= nodes.size()) {
            ImGui.textDisabled("Select a node above.");
            return;
        }

        if (selectedTree != lastEditedTree || selectedNode != lastEditedNode) {
            syncEditFields();
        }

        var node = nodes.get(selectedNode);
        ImGui.pushID("ne_" + node.getId());

        ImGui.setNextItemWidth(150);
        ImGui.inputText("Speaker", editSpeaker);
        ImGui.sameLine();
        ImGui.colorEdit4("##spkColor", editSpeakerColor, ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.NoLabel | ImGuiColorEditFlags.NoAlpha);
        if (ImGui.isItemHovered()) ImGui.setTooltip("Speaker name color");
        ImGui.setNextItemWidth(200);
        ImGui.inputText("Next Node ID", editNextNode);
        ImGui.setNextItemWidth(-1);
        ImGui.inputTextMultiline("##text", editText, 100, 300);

        int newColor = ((int) (editSpeakerColor[0] * 255) << 16) | ((int) (editSpeakerColor[1] * 255) << 8) | (int) (editSpeakerColor[2] * 255);
        if (!editSpeaker.get().equals(node.getSpeaker()) || !editText.get().equals(node.getText()) || !editNextNode.get().equals(node.getNextNodeId() != null ? node.getNextNodeId() : "") || newColor != node.getSpeakerColor()) {
            var updated = new DialogueNode(node.getId(), editSpeaker.get(), editText.get(), newColor);
            updated.setNextNodeId(editNextNode.get().trim().isEmpty() ? null : editNextNode.get().trim());
            updated.getOptions().addAll(node.getOptions());
            updated.getEnterActionIds().addAll(node.getEnterActionIds());
            updated.getConditionIds().addAll(node.getConditionIds());
            tree.getNodes().put(node.getId(), updated);
        }

        ImGui.spacing();

        if (ImGui.button("Add Option")) {
            showNewOption = true;
            newOptionText.set("");
            newOptionTarget.set("");
        }

        if (showNewOption) {
            ImGui.sameLine();
            ImGui.setNextItemWidth(200);
            ImGui.inputText("##not", newOptionText);
            ImGui.sameLine();
            ImGui.setNextItemWidth(100);
            ImGui.inputText("Target##notg", newOptionTarget);
            ImGui.sameLine();
            if (ImGui.button("Add")) {
                node.getOptions().add(new DialogueOption(
                        "opt_" + (node.getOptions().size() + 1),
                        newOptionText.get().trim(),
                        newOptionTarget.get().trim()
                ));
                showNewOption = false;
            }
            ImGui.sameLine();
            if (ImGui.button("X")) showNewOption = false;
        }

        ImGui.spacing();
        ImGui.pushStyleColor(ImGuiCol.Button, 0.55f, 0.10f, 0.10f, 1.0f);
        if (ImGui.button("Delete Node")) {
            tree.removeNode(node.getId());
            selectedNode = -1;
        }
        ImGui.popStyleColor(1);

        ImGui.popID();
    }

    private void renderOptionList(DialogueTree tree) {
        var nodes = new ArrayList<>(tree.getNodes().values());
        if (selectedNode < 0 || selectedNode >= nodes.size()) return;

        var node = nodes.get(selectedNode);
        var options = node.getOptions();

        if (options.isEmpty()) {
            ImGui.textDisabled("No options.");
            return;
        }

        for (int i = 0; i < options.size(); i++) {
            var opt = options.get(i);
            ImGui.pushID("ol" + i);

            ImGui.separator();
            ImGui.text("Option " + (i + 1));

            var t = new ImString(opt.getText(), 256);
            ImGui.setNextItemWidth(-1);
            if (ImGui.inputText("##ot" + i, t)) {
                var updated = new DialogueOption(opt.getId(), t.get(), opt.getTargetNodeId(),
                        opt.getConditionIds(), opt.getActionIds());
                options.set(i, updated);
            }

            var tg = new ImString(opt.getTargetNodeId(), 64);
            ImGui.setNextItemWidth(120);
            if (ImGui.inputText("Target Node##tg" + i, tg)) {
                opt.setTargetNodeId(tg.get());
            }
            ImGui.sameLine();
            ImGui.pushStyleColor(ImGuiCol.Button, 0.55f, 0.10f, 0.10f, 1.0f);
            if (ImGui.button("X")) {
                options.remove(i);
                ImGui.popStyleColor(1);
                ImGui.popID();
                return;
            }
            ImGui.popStyleColor(1);

            ImGui.popID();
        }
    }

    private void syncEditFields() {
        if (selectedTree < 0 || selectedTree >= trees.size()) return;
        var nodes = new ArrayList<>(trees.get(selectedTree).getNodes().values());
        if (selectedNode >= 0 && selectedNode < nodes.size()) {
            var node = nodes.get(selectedNode);
            editSpeaker.set(node.getSpeaker());
            editText.set(node.getText());
            editNextNode.set(node.getNextNodeId() != null ? node.getNextNodeId() : "");
            int c = node.getSpeakerColor();
            editSpeakerColor[0] = ((c >> 16) & 0xFF) / 255f;
            editSpeakerColor[1] = ((c >> 8) & 0xFF) / 255f;
            editSpeakerColor[2] = (c & 0xFF) / 255f;
            editSpeakerColor[3] = 1f;
        } else {
            editSpeaker.set("");
            editText.set("");
            editNextNode.set("");
        }
        lastEditedTree = selectedTree;
        lastEditedNode = selectedNode;
    }

    private void saveToServer() {
        var mgr = Common.getDialogueManager();
        mgr.replaceAll(trees);
        var nbt = mgr.toNbt();
        ClientPacketDistributor.sendToServer(new DialogueSavePacket(nbt));
        setStatus("Dialogue trees saved.");
    }

    private void loadFromServer() {
        var serverTrees = Common.getDialogueManager().getTrees();
        trees.clear();
        trees.addAll(serverTrees);
        selectedTree = trees.isEmpty() ? -1 : 0;
        selectedNode = -1;
        syncEditFields();
        setStatus("Loaded " + trees.size() + " trees from server.");
    }

    public void loadTree(DialogueTree t) {
        trees.add(t);
        selectedTree = trees.size() - 1;
        selectedNode = 0;
        syncEditFields();
    }

    public void clearTrees() {
        trees.clear();
        selectedTree = -1;
        selectedNode = -1;
    }

    @Override
    public void onClosed() {
        clearTrees();
    }
}
