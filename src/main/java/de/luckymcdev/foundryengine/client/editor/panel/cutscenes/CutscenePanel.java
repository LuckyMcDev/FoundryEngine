package de.luckymcdev.foundryengine.client.editor.panel.cutscenes;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.cutscene.CutsceneRenderer;
import de.luckymcdev.foundryengine.client.cutscene.CutsceneUiState;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.PanelRequirements;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.cutscene.util.LerpType;
import de.luckymcdev.foundryengine.common.network.packets.editor.CutscenePacket;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiSelectableFlags;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.Arrays;
import java.util.List;

public class CutscenePanel extends EditorPanel {
    public static final CutscenePanel INSTANCE = new CutscenePanel();

    private static final String[] EASING_NAMES =
            Arrays.stream(LerpType.values()).map(Enum::name).toArray(String[]::new);

    private final ImString newName = new ImString(64);

    private final ImInt selectedNodeIndex = new ImInt(0);
    private final ImFloat selectedNodePitch = new ImFloat(0f);
    private final ImFloat selectedNodeYaw = new ImFloat(0f);

    private int lastSelectedCutsceneIndex = Integer.MIN_VALUE;
    private int selectedIndex = -1;
    private boolean showNewForm = false;

    private CutscenePanel() {
        super(Common.id("cutscene_panel"), "Cutscenes", ImIcons.FA.FA_FILM);
        this.category = PanelCategory.EDITOR_CUTSCENES;
        this.menuBar = true;
    }

    @Override
    public void content() {
        if (!PanelRequirements.requireWorld()) {
            return;
        }

        renderMenuBar();
        ImGui.separator();

        beginContent();
        List<Cutscene> cutscenes = CutsceneRenderer.getCutscenes();
        renderLeftPanel(cutscenes);
        ImGui.sameLine();
        renderRightPanel(cutscenes);
        endContent();
    }

    @Override
    public void onClosed() {
        Client.getCutsceneManager().clearPreview();
    }

    private void renderMenuBar() {
        if (!ImGui.beginMenuBar()) return;

        if (ImGui.menuItem(ImIcons.FA.FA_PLUS + " New")) {
            showNewForm = !showNewForm;
            newName.set("");
        }

        if (ImGui.menuItem(ImIcons.FA.FA_ROTATE_RIGHT + " Sync")) {
            requestSync();
            setStatus("Sync requested.");
        }

        ImGui.endMenuBar();
    }

    private void renderLeftPanel(List<Cutscene> cutscenes) {
        ImGui.beginChild("##cutscene_list", 180f, 0, true);

        if (showNewForm) {
            renderNewCutsceneForm();
            ImGui.separator();
        }

        if (cutscenes.isEmpty()) {
            ImGui.textDisabled("No cutscenes.");
        } else {
            for (int i = 0; i < cutscenes.size(); i++) {
                Cutscene c = cutscenes.get(i);
                boolean sel = (i == selectedIndex);

                ImGui.pushID(i);
                Color color = new Color(c.getColorArgb());
                ImGui.colorButton("##cs_color",
                        color.r(), color.g(), color.b(), color.a(),
                        0, 12, 12);
                ImGui.sameLine();

                if (ImGui.selectable(c.getName() + "##cs", sel, ImGuiSelectableFlags.None)) {
                    selectedIndex = sel ? -1 : i;
                    CutsceneUiState.setSelected(selectedIndex >= 0 ? cutscenes.get(selectedIndex) : null);
                }
                ImGui.popID();
            }
        }

        ImGui.endChild();
    }

    private void renderNewCutsceneForm() {
        ImGui.setNextItemWidth(-1);
        boolean confirm = ImGui.inputTextWithHint("##newname", "cutscene_name", newName,
                imgui.flag.ImGuiInputTextFlags.EnterReturnsTrue);
        if (ImGui.button("Create") || confirm) {
            String name = newName.get().trim();
            if (!name.isBlank()) {
                ClientPacketDistributor.sendToServer(CutscenePacket.addAction(name));
                setStatus("Created: " + name);
                showNewForm = false;
                newName.set("");
            }
        }
        ImGui.sameLine();
        if (ImGui.button("Cancel")) {
            showNewForm = false;
            newName.set("");
        }
    }

    private void renderRightPanel(List<Cutscene> cutscenes) {
        ImGui.beginChild("##cutscene_detail", 0, 0, false);

        if (selectedIndex < 0 || selectedIndex >= cutscenes.size()) {
            ImGui.textDisabled("Select a cutscene on the left.");
            CutsceneUiState.setSelected(null);
            Client.getCutsceneManager().clearPreview();
            ImGui.endChild();
            return;
        }

        Cutscene c = cutscenes.get(selectedIndex);
        CutsceneUiState.setSelected(c);

        ImGui.text(c.getName());
        renderCutsceneColorPicker(c);
        ImGui.separator();

        renderNodeRotationEditor(c);
        ImGui.separator();

        renderPlaybackControls(c);
        ImGui.separator();

        renderDeleteButton(c);

        ImGui.endChild();
    }

    private void renderCutsceneColorPicker(Cutscene c) {
        Color color = new Color(c.getColorArgb());
        float[] col = {color.r(), color.g(), color.b(), color.a()};

        ImGui.sameLine();
        ImGui.setNextItemWidth(120);
        if (ImGui.colorEdit4("##cs_track_color", col, ImGuiColorEditFlags.NoInputs)) {
            c.setColorArgb(new Color(col[0], col[1], col[2], col[3]).argb());
            ClientPacketDistributor.sendToServer(new CutscenePacket(Client.getCutsceneEditor().toNbt()));
        }
        if (ImGui.isItemHovered()) ImGui.setTooltip("Track color");
    }

    private void renderNodeRotationEditor(Cutscene c) {
        if (selectedIndex != lastSelectedCutsceneIndex) {
            lastSelectedCutsceneIndex = selectedIndex;
            selectedNodeIndex.set(0);
            var rot = c.getInitialRot();
            selectedNodePitch.set(rot.x);
            selectedNodeYaw.set(rot.y);
        }

        int anchors = c.getAnchorPointCount();
        if (anchors <= 0) {
            ImGui.textDisabled("No nodes.");
            return;
        }

        if (selectedNodeIndex.get() < 0) selectedNodeIndex.set(0);
        if (selectedNodeIndex.get() > anchors - 1) selectedNodeIndex.set(anchors - 1);

        ImGui.text("Orientation");
        ImGui.spacing();

        ImGui.setNextItemWidth(120);
        int before = selectedNodeIndex.get();
        ImGui.inputInt("Node Index##cs_node", selectedNodeIndex);
        if (selectedNodeIndex.get() < 0) selectedNodeIndex.set(0);
        if (selectedNodeIndex.get() > anchors - 1) selectedNodeIndex.set(anchors - 1);
        if (selectedNodeIndex.get() != before) {
            var r = c.getAnchorRotations().get(selectedNodeIndex.get());
            selectedNodePitch.set(r.x);
            selectedNodeYaw.set(r.y);
        }

        ImGui.setNextItemWidth(120);
        ImGui.inputFloat("Pitch##cs_pitch", selectedNodePitch);
        ImGui.setNextItemWidth(120);
        ImGui.inputFloat("Yaw##cs_yaw", selectedNodeYaw);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ImGui.sameLine();
            if (ImGui.button(ImIcons.FA.FA_EYE + " Capture")) {
                selectedNodePitch.set(mc.player.getXRot());
                selectedNodeYaw.set(mc.player.getYRot());
            }
        }

        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_CHECK + " Apply")) {
            c.setAnchorRotation(selectedNodeIndex.get(),
                    new net.minecraft.world.phys.Vec2(selectedNodePitch.get(), selectedNodeYaw.get()));
            ClientPacketDistributor.sendToServer(new CutscenePacket(Client.getCutsceneEditor().toNbt()));
            setStatus("Rotation updated for node " + selectedNodeIndex.get() + ".");
        }

        ImGui.spacing();
        ImGui.separator();
        ImGui.text("Hold at Node");
        int holdAtNode = c.getAnchorHoldTicks(selectedNodeIndex.get());
        ImInt holdInput = new ImInt(holdAtNode);
        ImGui.setNextItemWidth(100);
        if (ImGui.inputInt("Hold (ticks)##cs_node_hold", holdInput)) {
            c.setAnchorHoldTicks(selectedNodeIndex.get(), Math.max(0, holdInput.get()));
            ClientPacketDistributor.sendToServer(new CutscenePacket(Client.getCutsceneEditor().toNbt()));
            setStatus("Hold set to " + holdInput.get() + " ticks for node " + selectedNodeIndex.get() + ".");
        }
    }

    private void renderPlaybackControls(Cutscene c) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (selectedIndex != lastSelectedCutsceneIndex) {
            CutsceneUiState.syncFromCutscene(c);
        }

        String playerName = mc.player.getName().getString();
        String lerpName = EASING_NAMES[CutsceneUiState.getPlaybackEasingIndex()];

        ImGui.setNextItemWidth(120);
        int len = CutsceneUiState.getPlaybackLength();
        if (ImGui.inputInt("Length (ticks)##len", new ImInt(len))) {
            CutsceneUiState.setPlaybackLength(len);
            CutsceneUiState.syncToCutscene(c);
        }

        ImGui.setNextItemWidth(120);
        int hs = CutsceneUiState.getPlaybackHoldStart();
        if (ImGui.inputInt("Hold Start##hs", new ImInt(hs))) {
            CutsceneUiState.setPlaybackHoldStart(hs);
            CutsceneUiState.syncToCutscene(c);
        }

        ImGui.setNextItemWidth(120);
        int he = CutsceneUiState.getPlaybackHoldEnd();
        if (ImGui.inputInt("Hold End##he", new ImInt(he))) {
            CutsceneUiState.setPlaybackHoldEnd(he);
            CutsceneUiState.syncToCutscene(c);
        }

        ImGui.setNextItemWidth(150);
        int easeIdx = CutsceneUiState.getPlaybackEasingIndex();
        if (ImGui.combo("Easing##ease", new ImInt(easeIdx), EASING_NAMES)) {
            CutsceneUiState.setPlaybackEasingIndex(easeIdx);
            CutsceneUiState.syncToCutscene(c);
        }

        ImGui.spacing();

        if (ImGui.button(ImIcons.FA.FA_PLAY + " Play")) {
            ClientPacketDistributor.sendToServer(CutscenePacket.playAction(
                    playerName, c.getName(),
                    CutsceneUiState.getPlaybackLength(), lerpName,
                    CutsceneUiState.getPlaybackHoldStart(), CutsceneUiState.getPlaybackHoldEnd()));
            setStatus("Playing: " + c.getName());
        }
        ImGui.sameLine();

        if (Client.getCutsceneManager().inCutscene()) {
            ImGui.sameLine();
            ImGui.pushStyleColor(ImGuiCol.Button, 0.70f, 0.20f, 0.20f, 1.0f);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.80f, 0.25f, 0.25f, 1.0f);
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.60f, 0.15f, 0.15f, 1.0f);
            if (ImGui.button(ImIcons.FA.FA_STOP + " Cancel")) {
                ClientPacketDistributor.sendToServer(CutscenePacket.cancelAction(playerName));
                setStatus("Cutscene cancelled.");
            }
            ImGui.popStyleColor(3);
        }
    }

    private void renderDeleteButton(Cutscene c) {
        ImGui.spacing();
        ImGui.pushStyleColor(ImGuiCol.Button, 0.55f, 0.10f, 0.10f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.70f, 0.15f, 0.15f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.45f, 0.08f, 0.08f, 1.0f);
        if (ImGui.button(ImIcons.FA.FA_TRASH + " Delete " + c.getName())) {
            ClientPacketDistributor.sendToServer(CutscenePacket.removeAction(c.getName()));
            selectedIndex = -1;
            CutsceneUiState.setSelected(null);
            setStatus("Deleted: " + c.getName());
        }
        ImGui.popStyleColor(3);
    }

    private void requestSync() {
        ClientPacketDistributor.sendToServer(CutscenePacket.requestSync());
    }
}
