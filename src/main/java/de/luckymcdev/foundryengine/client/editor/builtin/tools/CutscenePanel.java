package de.luckymcdev.foundryengine.client.editor.builtin.tools;

import de.luckymcdev.foundryengine.client.cutscene.ClientCutsceneManager;
import de.luckymcdev.foundryengine.client.cutscene.CutsceneEditor;
import de.luckymcdev.foundryengine.client.cutscene.CutsceneRenderer;
import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.CutsceneItems;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.cutscene.network.CutscenePacket;
import de.luckymcdev.foundryengine.common.cutscene.util.LerpType;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiSelectableFlags;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.Arrays;
import java.util.List;

public class CutscenePanel extends EditorPanel {
    public static final CutscenePanel INSTANCE = new CutscenePanel();
    private static final String[] EASING_NAMES =
            Arrays.stream(LerpType.values()).map(Enum::name).toArray(String[]::new);
    private final ImString newName = new ImString(64);
    private final ImInt playLength = new ImInt(60);
    private final ImInt holdStart = new ImInt(0);
    private final ImInt holdEnd = new ImInt(0);
    private final ImInt easingImInt = new ImInt(0);
    private int selectedIndex = -1;
    private boolean showNewForm = false;
    private String statusMessage = "";
    private long statusExpiry = 0L;

    private CutscenePanel() {
        super(Common.id("cutscene_panel"), "Cutscenes", ImIcons.FA.FA_FILM, Shortcut.ctrl(ImGuiKey.F3));
        this.category = PanelCategory.EDITOR_TOOLS;
        this.menuBar = true;
    }

    @Override
    public void content() {
        renderMenuBar();
        renderEditorItemRow();
        ImGui.separator();

        List<Cutscene> cutscenes = CutsceneRenderer.cutscenes;

        renderLeftPanel(cutscenes);
        ImGui.sameLine();
        renderRightPanel(cutscenes);

        renderStatus();
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

    private void renderEditorItemRow() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || CutsceneItems.EDITOR_ITEM == null) return;

        boolean hasItem = mc.player.getMainHandItem().getItem() == CutsceneItems.EDITOR_ITEM
                || mc.player.getOffhandItem().getItem() == CutsceneItems.EDITOR_ITEM;

        if (hasItem) {
            ImGui.pushStyleColor(ImGuiCol.Button, 0.20f, 0.60f, 0.20f, 1.0f);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.25f, 0.70f, 0.25f, 1.0f);
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.15f, 0.50f, 0.15f, 1.0f);
            if (ImGui.button(ImIcons.FA.FA_PENCIL + " Editor Item (active) — click to remove")) {
                removeEditorItem(mc);
            }
            ImGui.popStyleColor(3);
            ImGui.sameLine();
            ImGui.textDisabled("Hold RMB to drag points, scroll to push/pull.");
        } else {
            if (ImGui.button(ImIcons.FA.FA_PENCIL + " Give Editor Item")) {
                giveEditorItem(mc);
            }
        }
    }

    private void renderLeftPanel(List<Cutscene> cutscenes) {
        float listWidth = 180f;
        ImGui.beginChild("##cutscene_list", listWidth, 0, true);

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
                if (ImGui.selectable(c.getName() + "##cs" + i, sel, ImGuiSelectableFlags.None)) {
                    selectedIndex = (sel ? -1 : i);
                }
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
                sendCommand("engine cutscene add " + name);
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
            ImGui.endChild();
            return;
        }

        Cutscene c = cutscenes.get(selectedIndex);
        Minecraft mc = Minecraft.getInstance();

        ImGui.text(c.getName());
        ImGui.separator();

        int nodeCount = (c.path.getPoints().size() + 1) / 2;
        int splineCount = c.path.splines.size();
        ImGui.textDisabled("Nodes: " + nodeCount + "  |  Splines: " + splineCount);

        ImGui.spacing();

        ImGui.text("Nodes");
        ImGui.spacing();

        if (ImGui.button(ImIcons.FA.FA_PLUS + " Add Node at End")) {
            CutsceneEditor.addNodeAtEnd(c);
            setStatus("Node added at end.");
        }
        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_PLUS + " Add Node at Start")) {
            CutsceneEditor.addNodeAtStart(c);
            setStatus("Node added at start.");
        }

        ImGui.spacing();

        ImGui.pushStyleColor(ImGuiCol.Button, 0.55f, 0.25f, 0.10f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.70f, 0.35f, 0.15f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.45f, 0.20f, 0.08f, 1.0f);
        if (ImGui.button(ImIcons.FA.FA_MINUS + " Remove Last Node")) {
            boolean deleted = CutsceneEditor.removeLastNode(c);
            if (deleted) {
                selectedIndex = -1;
                setStatus("Cutscene deleted (was single-point).");
            } else {
                setStatus("Last node removed.");
            }
        }
        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_MINUS + " Remove First Node")) {
            boolean deleted = CutsceneEditor.removeFirstNode(c);
            if (deleted) {
                selectedIndex = -1;
                setStatus("Cutscene deleted (was single-point).");
            } else {
                setStatus("First node removed.");
            }
        }
        ImGui.popStyleColor(3);

        ImGui.spacing();

        if (nodeCount == 2) {
            if (ImGui.button(ImIcons.FA.FA_BEZIER_CURVE + " Linearize")) {
                sendCommand("engine cutscene linearize " + c.getName());
                setStatus("Linearized: " + c.getName());
            }
            if (ImGui.isItemHovered()) {
                ImGui.setTooltip("Straightens the tangent handles so the camera travels in a straight line.");
            }
        }

        ImGui.separator();
        if (mc.player != null && CutsceneItems.EDITOR_ITEM != null) {
            boolean hasItem = mc.player.getMainHandItem().getItem() == CutsceneItems.EDITOR_ITEM
                    || mc.player.getOffhandItem().getItem() == CutsceneItems.EDITOR_ITEM;
            if (!hasItem) {
                if (ImGui.button(ImIcons.FA.FA_PENCIL + " Give Editor Item (drag points)")) {
                    giveEditorItem(mc);
                    setStatus("Editor item given. Hold RMB to drag points.");
                }
            } else {
                ImGui.textDisabled(ImIcons.FA.FA_PENCIL + " Hold RMB to drag, scroll to push/pull.");
            }
        }

        ImGui.separator();
        ImGui.text("Playback");
        ImGui.spacing();

        ImGui.setNextItemWidth(120);
        ImGui.inputInt("Length (ticks)##len", playLength);
        if (playLength.get() < 1) playLength.set(1);

        ImGui.setNextItemWidth(120);
        ImGui.inputInt("Hold Start (ticks)##hs", holdStart);
        if (holdStart.get() < 0) holdStart.set(0);

        ImGui.setNextItemWidth(120);
        ImGui.inputInt("Hold End (ticks)##he", holdEnd);
        if (holdEnd.get() < 0) holdEnd.set(0);

        ImGui.setNextItemWidth(150);
        ImGui.combo("Easing##ease", easingImInt, EASING_NAMES);

        int totalTicks = playLength.get() + holdStart.get() + holdEnd.get();
        ImGui.textDisabled("Total: " + totalTicks + " ticks (" + String.format("%.1f", totalTicks / 20f) + "s)");

        ImGui.spacing();

        if (mc.player != null) {
            String playerName = mc.player.getName().getString();

            if (ImGui.button(ImIcons.FA.FA_PLAY + " Play")) {
                sendCommand(buildPlayCommand(c.getName(), playerName, false));
                setStatus("Playing: " + c.getName());
            }

            ImGui.sameLine();

            if (ImGui.button(ImIcons.FA.FA_ANCHOR + " Play Anchored")) {
                sendCommand(buildPlayCommand(c.getName(), playerName, true));
                setStatus("Playing anchored: " + c.getName());
            }

            if (ClientCutsceneManager.inCutscene()) {
                ImGui.sameLine();
                ImGui.pushStyleColor(ImGuiCol.Button, 0.70f, 0.20f, 0.20f, 1.0f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.80f, 0.25f, 0.25f, 1.0f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.60f, 0.15f, 0.15f, 1.0f);
                if (ImGui.button(ImIcons.FA.FA_STOP + " Cancel")) {
                    sendCommand("engine cutscene cancel " + playerName);
                    setStatus("Cutscene cancelled.");
                }
                ImGui.popStyleColor(3);
            }
        }

        ImGui.separator();
        ImGui.spacing();
        ImGui.pushStyleColor(ImGuiCol.Button, 0.55f, 0.10f, 0.10f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.70f, 0.15f, 0.15f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.45f, 0.08f, 0.08f, 1.0f);
        if (ImGui.button(ImIcons.FA.FA_TRASH + " Delete " + c.getName())) {
            sendCommand("engine cutscene remove " + c.getName());
            selectedIndex = -1;
            setStatus("Deleted: " + c.getName());
        }
        ImGui.popStyleColor(3);

        ImGui.endChild();
    }

    private void renderStatus() {
        if (!statusMessage.isEmpty()) {
            if (System.currentTimeMillis() > statusExpiry) {
                statusMessage = "";
            } else {
                ImGui.separator();
                ImGui.textDisabled(statusMessage);
            }
        }
    }

    private void setStatus(String message) {
        statusMessage = message;
        statusExpiry = System.currentTimeMillis() + 4000L;
    }

    private String buildPlayCommand(String cutsceneName, String playerName, boolean anchored) {
        String lerpName = EASING_NAMES[easingImInt.get()];
        String base = anchored ? "engine cutscene playAnchored" : "engine cutscene play";

        if (anchored) {
            return base + " " + playerName
                    + " " + cutsceneName
                    + " " + playLength.get()
                    + " true true"
                    + " " + lerpName
                    + " " + holdStart.get()
                    + " " + holdEnd.get();
        } else {
            return base + " " + playerName
                    + " " + cutsceneName
                    + " " + playLength.get()
                    + " " + lerpName
                    + " " + holdStart.get()
                    + " " + holdEnd.get();
        }
    }

    private void requestSync() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Request", true);
        ClientPacketDistributor.sendToServer(new CutscenePacket(tag));
    }

    private void sendCommand(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().sendCommand(command);
        }
    }

    private void giveEditorItem(Minecraft mc) {
        if (mc.player == null || CutsceneItems.EDITOR_ITEM == null) return;
        sendCommand("give " + mc.player.getName().getString() + " foundryengine:editor 1");
    }

    private void removeEditorItem(Minecraft mc) {
        if (mc.player == null || CutsceneItems.EDITOR_ITEM == null) return;
        sendCommand("clear " + mc.player.getName().getString() + " foundryengine:editor");
    }
}