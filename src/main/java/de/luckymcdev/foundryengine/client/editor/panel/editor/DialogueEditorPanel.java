package de.luckymcdev.foundryengine.client.editor.panel.editor;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.dialogue.DialogueNode;
import de.luckymcdev.foundryengine.common.dialogue.DialogueOption;
import de.luckymcdev.foundryengine.common.dialogue.DialogueTree;
import de.luckymcdev.foundryengine.common.network.packets.dialogue.DialogueSavePacket;
import imgui.ImGui;
import imgui.flag.*;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;

public class DialogueEditorPanel extends EditorPanel {
    public static final DialogueEditorPanel INSTANCE = new DialogueEditorPanel();

    // Layout constants -- tweak these in one place instead of magic numbers everywhere.
    private static final float TREE_PANEL_WIDTH = 220f;
    private static final float SECTION_SPACING = 8f;
    private static final float ACCENT_R = 0.33f, ACCENT_G = 0.62f, ACCENT_B = 1.0f;
    private static final float DANGER_R = 0.62f, DANGER_G = 0.16f, DANGER_B = 0.16f;
    private static final float DANGER_HOVER_R = 0.75f, DANGER_HOVER_G = 0.20f, DANGER_HOVER_B = 0.20f;

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

    // ------------------------------------------------------------------
    // Menu bar
    // ------------------------------------------------------------------

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

    // ------------------------------------------------------------------
    // Left panel: tree list
    // ------------------------------------------------------------------

    private static boolean colorInt4(String label, int[] rgba) {
        float[] f = new float[]{
                ((rgba[0] >> 16) & 0xFF) / 255f,
                ((rgba[0] >> 8) & 0xFF) / 255f,
                (rgba[0] & 0xFF) / 255f,
                ((rgba[0] >> 24) & 0xFF) / 255f
        };
        ImGui.setNextItemWidth(180);
        if (ImGui.colorEdit4(label, f, ImGuiColorEditFlags.NoInputs)) {
            int r = (int) (f[0] * 255);
            int g = (int) (f[1] * 255);
            int b = (int) (f[2] * 255);
            int a = (int) (f[3] * 255);
            rgba[0] = (a << 24) | (r << 16) | (g << 8) | b;
            return true;
        }
        return false;
    }

    /** Dimmed, slightly spaced-out uppercase label used to head a section. */
    private static void sectionLabel(String text) {
        ImGui.textDisabled(text);
        ImGui.spacing();
    }

    // ------------------------------------------------------------------
    // Right panel: tree details
    // ------------------------------------------------------------------

    /** Centered, dimmed placeholder message for empty states. */
    private static void centeredMessage(String text) {
        ImGui.dummy(0, 24);
        float avail = ImGui.getContentRegionAvailX();
        float textW = ImGui.calcTextSize(text).x;
        ImGui.setCursorPosX(Math.max(0, (avail - textW) / 2f));
        ImGui.textDisabled(text);
    }

    /** Begins a bordered, padded "card" child region that auto-sizes to its content. */
    private static void cardBegin(String id) {
        ImGui.beginChild(id, 0, 0, true, imgui.flag.ImGuiWindowFlags.AlwaysAutoResize | imgui.flag.ImGuiWindowFlags.NoScrollbar);
    }

    // ------------------------------------------------------------------
    // Style editor -- now grouped into labeled collapsible sub-sections
    // instead of one undifferentiated list of 14 rows.
    // ------------------------------------------------------------------

    private static void cardEnd() {
        ImGui.endChild();
    }

    private static void dangerButtonBegin() {
        ImGui.pushStyleColor(ImGuiCol.Button, DANGER_R, DANGER_G, DANGER_B, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, DANGER_HOVER_R, DANGER_HOVER_G, DANGER_HOVER_B, 1.0f);
    }

    // ------------------------------------------------------------------
    // Node tab strip -- now a proper bordered, horizontally scrolling
    // strip of tab-like buttons instead of full-width squished buttons.
    // ------------------------------------------------------------------

    private static void dangerButtonEnd() {
        ImGui.popStyleColor(2);
    }

    private void renderTreePanel() {
        ImGui.beginChild("##tree_panel", TREE_PANEL_WIDTH, 0, true);

        sectionLabel("TREES");

        if (showNewTree) {
            renderNewTreeForm();
            ImGui.spacing();
        }

        if (trees.isEmpty()) {
            ImGui.spacing();
            ImGui.textDisabled("No dialogue trees yet.");
            ImGui.textDisabled("Use the menu above to add one.");
        } else {
            for (int i = 0; i < trees.size(); i++) {
                var t = trees.get(i);
                boolean sel = i == selectedTree;
                ImGui.pushID("tree" + i);

                if (sel) {
                    ImGui.pushStyleColor(ImGuiCol.Header, ACCENT_R, ACCENT_G, ACCENT_B, 0.35f);
                    ImGui.pushStyleColor(ImGuiCol.HeaderHovered, ACCENT_R, ACCENT_G, ACCENT_B, 0.45f);
                }

                String label = ImIcons.FA.FA_COMMENT + "  " + t.getId();
                if (ImGui.selectable(label, sel, ImGuiSelectableFlags.None, 0, 24)) {
                    selectedTree = sel ? -1 : i;
                    selectedNode = -1;
                    syncEditFields();
                }

                if (sel) {
                    ImGui.popStyleColor(2);
                }

                if (ImGui.isItemHovered() && !sel) {
                    ImGui.setTooltip("Click to open " + t.getId());
                }

                ImGui.popID();
            }
        }

        ImGui.endChild();
    }

    // ------------------------------------------------------------------
    // Node editor
    // ------------------------------------------------------------------

    private void renderNewTreeForm() {
        ImGui.beginChild("##new_tree_form", 0, 64, true);
        ImGui.textColored(ACCENT_R, ACCENT_G, ACCENT_B, 1f, "New Tree");
        ImGui.setNextItemWidth(-1);
        boolean done = ImGui.inputTextWithHint("##ntid", "namespace:path", newTreeId, ImGuiInputTextFlags.EnterReturnsTrue);

        float avail = ImGui.getContentRegionAvailX();
        float half = (avail - ImGui.getStyle().getItemSpacingX()) / 2f;

        boolean create = ImGui.button("Create", half, 0) || done;
        ImGui.sameLine();
        boolean cancel = ImGui.button("Cancel", half, 0);

        if (create && !newTreeId.get().trim().isEmpty()) {
            var id = Identifier.parse(newTreeId.get().trim());
            var tree = new DialogueTree(id, "start");
            tree.addNode(new DialogueNode("start", "NPC", "Hello!"));
            trees.add(tree);
            selectedTree = trees.size() - 1;
            selectedNode = 0;
            syncEditFields();
            showNewTree = false;
        }
        if (cancel) {
            showNewTree = false;
        }
        ImGui.endChild();
    }

    private void renderDetailPanel() {
        ImGui.beginChild("##detail_panel", 0, 0, false);

        if (selectedTree < 0 || selectedTree >= trees.size()) {
            centeredMessage("Select a tree on the left to start editing.");
            ImGui.endChild();
            return;
        }

        var tree = trees.get(selectedTree);

        renderTreeHeader(tree);
        ImGui.dummy(0, SECTION_SPACING);

        renderStyleEditor(tree);
        ImGui.dummy(0, SECTION_SPACING);

        cardBegin("##nodes_card");
        sectionLabel("NODES");
        renderNodeBar(tree);
        ImGui.spacing();
        renderNodeEdit(tree);
        cardEnd();

        ImGui.dummy(0, SECTION_SPACING);
        renderOptionList(tree);

        ImGui.endChild();
    }

    // ------------------------------------------------------------------
    // Options list -- each option is now its own bordered "card" row
    // with a clear label, instead of bare inline widgets.
    // ------------------------------------------------------------------

    private void renderTreeHeader(DialogueTree tree) {
        ImGui.pushStyleColor(ImGuiCol.Text, ACCENT_R, ACCENT_G, ACCENT_B, 1f);
        ImGui.text(ImIcons.FA.FA_COMMENT + "  " + tree.getId());
        ImGui.popStyleColor();

        ImGui.spacing();
        ImGui.beginGroup();

        var nodeIds = new ArrayList<>(tree.getNodes().keySet());
        int rootIdx = Math.max(0, nodeIds.indexOf(tree.getRootNodeId()));
        var ri = new ImInt(rootIdx);
        String[] items = nodeIds.toArray(String[]::new);

        ImGui.setNextItemWidth(220);
        if (ImGui.combo("Root Node", ri, items)) {
            tree.setRootNodeId(nodeIds.get(ri.get()));
        }

        ImGui.sameLine(0, 16);
        if (ImGui.button(ImIcons.FA.FA_PLUS + " Add Node")) {
            showNewNode = true;
            newNodeId.set("node_" + tree.getNodes().size());
            newNodeSpeaker.set("NPC");
            newNodeText.set("");
        }

        ImGui.endGroup();
    }

    // ------------------------------------------------------------------
    // Small layout helpers
    // ------------------------------------------------------------------

    private void renderStyleEditor(DialogueTree tree) {
        if (!ImGui.collapsingHeader(ImIcons.FA.FA_PAINT_BRUSH + " Style")) {
            return;
        }

        var s = tree.getStyle();
        ImGui.indent();

        if (ImGui.treeNodeEx("Dialogue Box", ImGuiTreeNodeFlags.DefaultOpen)) {
            int[] col = new int[]{s.getDialogueBackground()};
            if (colorInt4("Background##dialogueBg", col)) s.setDialogueBackground(col[0]);
            col[0] = s.getDialogueBorder();
            if (colorInt4("Border##dialogueBorder", col)) s.setDialogueBorder(col[0]);

            var bw = new ImInt(s.getDialogueBorderWidth());
            ImGui.setNextItemWidth(100);
            if (ImGui.inputInt("Border Width##dbw", bw)) s.setDialogueBorderWidth(Math.max(0, bw.get()));
            ImGui.treePop();
        }

        if (ImGui.treeNodeEx("Options Box", ImGuiTreeNodeFlags.DefaultOpen)) {
            int[] col = new int[]{s.getOptionsBackground()};
            if (colorInt4("Background##optionsBg", col)) s.setOptionsBackground(col[0]);
            col[0] = s.getOptionsBorder();
            if (colorInt4("Border##optionsBorder", col)) s.setOptionsBorder(col[0]);

            var bw = new ImInt(s.getOptionsBorderWidth());
            ImGui.setNextItemWidth(100);
            if (ImGui.inputInt("Border Width##obw", bw)) s.setOptionsBorderWidth(Math.max(0, bw.get()));
            ImGui.treePop();
        }

        if (ImGui.treeNodeEx("Buttons", ImGuiTreeNodeFlags.DefaultOpen)) {
            int[] col = new int[]{s.getButtonBackground()};
            if (colorInt4("Background##btnBg", col)) s.setButtonBackground(col[0]);
            col[0] = s.getButtonHover();
            if (colorInt4("Hover##btnHover", col)) s.setButtonHover(col[0]);
            col[0] = s.getButtonBorder();
            if (colorInt4("Border##btnBorder", col)) s.setButtonBorder(col[0]);
            ImGui.treePop();
        }

        if (ImGui.treeNodeEx("Navigation Buttons", ImGuiTreeNodeFlags.None)) {
            int[] col = new int[]{s.getNavButtonBackground()};
            if (colorInt4("Background##navBg", col)) s.setNavButtonBackground(col[0]);
            col[0] = s.getNavButtonHover();
            if (colorInt4("Hover##navHover", col)) s.setNavButtonHover(col[0]);
            col[0] = s.getNavButtonBorder();
            if (colorInt4("Border##navBorder", col)) s.setNavButtonBorder(col[0]);
            ImGui.treePop();
        }

        if (ImGui.treeNodeEx("Overlay & Text", ImGuiTreeNodeFlags.None)) {
            int[] col = new int[]{s.getOverlayColor()};
            if (colorInt4("Overlay##overlay", col)) s.setOverlayColor(col[0]);

            ImGui.spacing();
            var iv = new ImInt(s.getSpeakerFontSize());
            ImGui.setNextItemWidth(100);
            if (ImGui.inputInt("Speaker Font", iv)) s.setSpeakerFontSize(Math.max(1, iv.get()));
            iv.set(s.getDialogueFontSize());
            ImGui.setNextItemWidth(100);
            if (ImGui.inputInt("Dialogue Font", iv)) s.setDialogueFontSize(Math.max(1, iv.get()));
            iv.set(s.getOptionFontSize());
            ImGui.setNextItemWidth(100);
            if (ImGui.inputInt("Option Font", iv)) s.setOptionFontSize(Math.max(1, iv.get()));
            ImGui.treePop();
        }

        if (ImGui.treeNodeEx("Layout", ImGuiTreeNodeFlags.None)) {
            var iv = new ImInt(s.getMargin());
            ImGui.setNextItemWidth(100);
            if (ImGui.inputInt("Margin", iv)) s.setMargin(Math.max(0, iv.get()));
            iv.set(s.getPanelHeight());
            ImGui.setNextItemWidth(100);
            if (ImGui.inputInt("Panel Height", iv)) s.setPanelHeight(Math.max(20, iv.get()));
            iv.set(s.getButtonHeight());
            ImGui.setNextItemWidth(100);
            if (ImGui.inputInt("Button Height", iv)) s.setButtonHeight(Math.max(10, iv.get()));
            iv.set(s.getOptionGap());
            ImGui.setNextItemWidth(100);
            if (ImGui.inputInt("Option Gap", iv)) s.setOptionGap(Math.max(0, iv.get()));
            ImGui.treePop();
        }

        ImGui.unindent();
    }

    private void renderNodeBar(DialogueTree tree) {
        var nodes = new ArrayList<>(tree.getNodes().values());

        if (showNewNode) {
            renderNewNodeForm(tree);
            ImGui.spacing();
        }

        ImGui.beginChild("##node_tabs", 0, 40, true,
                imgui.flag.ImGuiWindowFlags.HorizontalScrollbar | imgui.flag.ImGuiWindowFlags.NoScrollWithMouse);

        for (int i = 0; i < nodes.size(); i++) {
            var n = nodes.get(i);
            boolean sel = i == selectedNode;
            boolean isRoot = n.getId().equals(tree.getRootNodeId());

            ImGui.pushID("nodetab" + i);
            if (sel) {
                ImGui.pushStyleColor(ImGuiCol.Button, ACCENT_R, ACCENT_G, ACCENT_B, 0.9f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, ACCENT_R, ACCENT_G, ACCENT_B, 1.0f);
            }

            String label = (isRoot ? ImIcons.FA.FA_HOME + " " : "") + n.getId();
            if (ImGui.button(label, 0, 26)) {
                selectedNode = sel ? -1 : i;
                syncEditFields();
            }

            if (sel) {
                ImGui.popStyleColor(2);
            }
            ImGui.popID();

            if (i < nodes.size() - 1) {
                ImGui.sameLine(0, 6);
            }
        }

        if (nodes.isEmpty()) {
            ImGui.textDisabled("No nodes in this tree yet.");
        }

        ImGui.endChild();
    }

    private void renderNewNodeForm(DialogueTree tree) {
        ImGui.beginChild("##new_node_form", 0, 60, true);
        ImGui.textColored(ACCENT_R, ACCENT_G, ACCENT_B, 1f, "New Node");

        ImGui.setNextItemWidth(140);
        ImGui.inputTextWithHint("##nnid", "node id", newNodeId);
        ImGui.sameLine();
        ImGui.setNextItemWidth(140);
        ImGui.inputTextWithHint("##nnspeaker", "speaker", newNodeSpeaker);
        ImGui.sameLine();

        if (ImGui.button(ImIcons.FA.FA_CHECK + " Add") && !newNodeId.get().trim().isEmpty()) {
            tree.addNode(new DialogueNode(newNodeId.get().trim(), newNodeSpeaker.get(), newNodeText.get()));
            selectedNode = new ArrayList<>(tree.getNodes().values()).size() - 1;
            syncEditFields();
            showNewNode = false;
        }
        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_TIMES + "##cancelNode")) showNewNode = false;

        ImGui.endChild();
    }

    private void renderNodeEdit(DialogueTree tree) {
        var nodes = new ArrayList<>(tree.getNodes().values());

        if (selectedNode < 0 || selectedNode >= nodes.size()) {
            centeredMessage("Select a node above to edit its contents.");
            return;
        }

        if (selectedTree != lastEditedTree || selectedNode != lastEditedNode) {
            syncEditFields();
        }

        var node = nodes.get(selectedNode);
        ImGui.pushID("ne_" + node.getId());

        ImGui.beginGroup();
        ImGui.setNextItemWidth(180);
        ImGui.inputText("Speaker", editSpeaker);
        ImGui.sameLine(0, 12);
        ImGui.colorEdit4("Color", editSpeakerColor, ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.NoAlpha);
        if (ImGui.isItemHovered()) ImGui.setTooltip("Speaker name color");

        ImGui.setNextItemWidth(220);
        ImGui.inputTextWithHint("Next Node ID", "(leave empty if none)", editNextNode);
        ImGui.endGroup();

        ImGui.spacing();
        ImGui.textDisabled("Dialogue text");
        ImGui.setNextItemWidth(-1);
        ImGui.inputTextMultiline("##text", editText, 0, 140);

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
        ImGui.separator();
        ImGui.spacing();

        ImGui.beginGroup();
        if (ImGui.button(ImIcons.FA.FA_PLUS + " Add Option")) {
            showNewOption = true;
            newOptionText.set("");
            newOptionTarget.set("");
        }

        ImGui.sameLine(0, 12);
        dangerButtonBegin();
        if (ImGui.button(ImIcons.FA.FA_TRASH + " Delete Node")) {
            tree.removeNode(node.getId());
            selectedNode = -1;
        }
        dangerButtonEnd();
        ImGui.endGroup();

        if (showNewOption) {
            ImGui.spacing();
            renderNewOptionForm(node);
        }

        ImGui.popID();
    }

    private void renderNewOptionForm(DialogueNode node) {
        ImGui.beginChild("##new_option_form", 0, 60, true);
        ImGui.textColored(ACCENT_R, ACCENT_G, ACCENT_B, 1f, "New Option");

        ImGui.setNextItemWidth(260);
        ImGui.inputTextWithHint("##not", "option text", newOptionText);
        ImGui.sameLine();
        ImGui.setNextItemWidth(120);
        ImGui.inputTextWithHint("##notg", "target node", newOptionTarget);
        ImGui.sameLine();

        if (ImGui.button(ImIcons.FA.FA_CHECK + " Add")) {
            node.getOptions().add(new DialogueOption(
                    "opt_" + (node.getOptions().size() + 1),
                    newOptionText.get().trim(),
                    newOptionTarget.get().trim()
            ));
            showNewOption = false;
        }
        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_TIMES + "##cancelOption")) showNewOption = false;

        ImGui.endChild();
    }

    private void renderOptionList(DialogueTree tree) {
        var nodes = new ArrayList<>(tree.getNodes().values());
        if (selectedNode < 0 || selectedNode >= nodes.size()) return;

        var node = nodes.get(selectedNode);
        var options = node.getOptions();

        cardBegin("##options_card");
        sectionLabel("OPTIONS (" + options.size() + ")");

        if (options.isEmpty()) {
            ImGui.textDisabled("This node has no options yet -- add one above.");
            cardEnd();
            return;
        }

        int removeIndex = -1;

        for (int i = 0; i < options.size(); i++) {
            var opt = options.get(i);
            ImGui.pushID("opt" + i);

            ImGui.beginChild("##opt_row" + i, 0, 64, true);

            ImGui.textColored(ACCENT_R, ACCENT_G, ACCENT_B, 1f, ImIcons.FA.FA_ARROW_RIGHT + " Option " + (i + 1));
            ImGui.sameLine(ImGui.getContentRegionAvailX() + ImGui.getCursorPosX() - 24);
            dangerButtonBegin();
            boolean deleteClicked = ImGui.button(ImIcons.FA.FA_TIMES + "##del" + i, 24, 0);
            dangerButtonEnd();
            if (deleteClicked) removeIndex = i;

            var t = new ImString(opt.getText(), 256);
            ImGui.setNextItemWidth(-1);
            if (ImGui.inputText("##ot" + i, t)) {
                var updated = new DialogueOption(opt.getId(), t.get(), opt.getTargetNodeId(),
                        opt.getConditionIds(), opt.getActionIds());
                options.set(i, updated);
            }

            ImGui.setNextItemWidth(180);
            var tg = new ImString(opt.getTargetNodeId(), 64);
            if (ImGui.inputText("Target Node##tg" + i, tg)) {
                opt.setTargetNodeId(tg.get());
            }

            ImGui.endChild();
            ImGui.popID();

            if (i < options.size() - 1) ImGui.spacing();
        }

        if (removeIndex >= 0) {
            options.remove(removeIndex);
        }

        cardEnd();
    }

    // ------------------------------------------------------------------
    // Data sync / persistence (unchanged behavior)
    // ------------------------------------------------------------------

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