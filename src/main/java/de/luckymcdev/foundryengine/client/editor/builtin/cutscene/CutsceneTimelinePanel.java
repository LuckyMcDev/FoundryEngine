package de.luckymcdev.foundryengine.client.editor.builtin.cutscene;

import de.luckymcdev.foundryengine.client.cutscene.ClientCutsceneManager;
import de.luckymcdev.foundryengine.client.cutscene.CutsceneEditor;
import de.luckymcdev.foundryengine.client.cutscene.CutsceneRenderer;
import de.luckymcdev.foundryengine.client.cutscene.CutsceneUiState;
import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.CutsceneItems;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.cutscene.network.CutscenePacket;
import de.luckymcdev.foundryengine.common.cutscene.util.LerpType;
import de.luckymcdev.foundryengine.common.cutscene.util.ScreenEffectType;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiSelectableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.Arrays;
import java.util.List;

/**
 * Dedicated cutscene timeline / preview / effects panel.
 */
public class CutsceneTimelinePanel extends EditorPanel {
    public static final CutsceneTimelinePanel INSTANCE = new CutsceneTimelinePanel();

    private static final String[] EASING_NAMES =
            Arrays.stream(LerpType.values()).map(Enum::name).toArray(String[]::new);
    private static final String[] EFFECT_TYPES =
            Arrays.stream(ScreenEffectType.values()).map(Enum::name).toArray(String[]::new);

    private final ImBoolean previewEnabled = new ImBoolean(false);
    private final ImInt previewTick = new ImInt(0);
    private final ImInt selectedEffectIndex = new ImInt(-1);
    private final ImFloat selectedEffectAt = new ImFloat(0f);
    private final ImInt effectTypeIndex = new ImInt(0);
    private final ImFloat effectIntroDuration = new ImFloat(0.1f);
    private final ImFloat effectHoldDuration = new ImFloat(0.2f);
    private final ImFloat effectOutroDuration = new ImFloat(0.1f);
    private final ImInt effectEasingIndex = new ImInt(0);
    private final ImString effectCommand = new ImString(256);
    private float zoomPxPerTick = 6.0f;

    private CutsceneTimelinePanel() {
        super(Common.id("cutscene_timeline"), "Cutscene Timeline", ImIcons.FA.FA_SLIDERS, Shortcut.empty());
        this.category = PanelCategory.EDITOR_CUTSCENES;
        this.menuBar = true;
    }

    @Override
    public void onClosed() {
        ClientCutsceneManager.clearPreview();
        previewEnabled.set(false);
    }

    @Override
    public void content() {
        renderMenuBar();

        List<Cutscene> cutscenes = CutsceneRenderer.cutscenes;

        ImGui.beginChild("##cs_tl_list", 220f, 0, true);
        try {
            if (cutscenes.isEmpty()) {
                ImGui.textDisabled("No cutscenes.");
            } else {
                String selectedName = CutsceneUiState.getSelectedName();
                for (int i = 0; i < cutscenes.size(); i++) {
                    Cutscene c = cutscenes.get(i);
                    boolean sel = selectedName != null && selectedName.equals(c.getName());

                    ImGui.pushID(i);
                    int argb = c.getColorArgb();
                    ImGui.colorButton("##cs_color",
                            ((argb >> 16) & 0xFF) / 255f,
                            ((argb >> 8) & 0xFF) / 255f,
                            (argb & 0xFF) / 255f,
                            ((argb >> 24) & 0xFF) / 255f,
                            0, 12, 12);
                    ImGui.sameLine();

                    if (ImGui.selectable(c.getName() + "##cs", sel, ImGuiSelectableFlags.None)) {
                        CutsceneUiState.setSelected(c);
                        selectedEffectIndex.set(-1);
                    }
                    ImGui.popID();
                }
            }
        } finally {
            ImGui.endChild();
        }

        ImGui.sameLine();

        // Right: timeline + effects for selected cutscene
        ImGui.beginChild("##cs_tl_right", 0, 0, false);
        try {
            Cutscene selected = CutsceneUiState.getSelectedCutscene();
            if (selected == null) {
                ImGui.textDisabled("Select a cutscene on the left (or in the Cutscenes panel).");
                ClientCutsceneManager.clearPreview();
                return;
            }

            renderPreviewSettings(selected);

            int totalTicks = getTotalTicks(selected);
            ImGui.separator();
            renderNodeControls(selected);

            ImGui.separator();
            renderTimelineStrip(selected, totalTicks);

            ImGui.separator();
            renderEffectsEditor(selected, totalTicks);
        } finally {
            ImGui.endChild();
        }
    }

    private void renderMenuBar() {
        if (!ImGui.beginMenuBar()) return;
        if (ImGui.menuItem(ImIcons.FA.FA_ROTATE_RIGHT + " Sync")) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("Request", true);
            ClientPacketDistributor.sendToServer(new CutscenePacket(tag));
        }
        ImGui.endMenuBar();
    }

    private void renderPreviewSettings(Cutscene c) {
        ImGui.text(c.getName());
        ImGui.spacing();
        ImGui.text("Preview Settings");

        // Bind to the cutscene's values to avoid duplication across panels
        int lenTicks = Math.max(1, c.getDefaultLength());
        int hs = Math.max(0, c.getDefaultHoldStart());
        int he = Math.max(0, c.getDefaultHoldEnd());
        int easeIdx = 0;
        for (int i = 0; i < EASING_NAMES.length; i++) {
            if (EASING_NAMES[i].equalsIgnoreCase(c.getDefaultEasing())) {
                easeIdx = i;
                break;
            }
        }

        boolean changed = false;

        // Length
        ImGui.setNextItemWidth(120);
        ImInt lenInput = new ImInt(lenTicks);
        if (ImGui.inputInt("Length (ticks)##cs_tl_len", lenInput)) {
            c.setDefaultLength(Math.max(1, lenInput.get()));
            changed = true;
        }

        // Hold Start
        ImGui.setNextItemWidth(120);
        ImInt hsInput = new ImInt(hs);
        if (ImGui.inputInt("Hold Start##cs_tl_hs", hsInput)) {
            c.setDefaultHoldStart(Math.max(0, hsInput.get()));
            changed = true;
        }

        // Hold End
        ImGui.setNextItemWidth(120);
        ImInt heInput = new ImInt(he);
        if (ImGui.inputInt("Hold End##cs_tl_he", heInput)) {
            c.setDefaultHoldEnd(Math.max(0, heInput.get()));
            changed = true;
        }

        // Easing
        ImGui.setNextItemWidth(150);
        ImInt easeInput = new ImInt(easeIdx);
        if (ImGui.combo("Easing##cs_tl_ease", easeInput, EASING_NAMES)) {
            c.setDefaultEasing(EASING_NAMES[easeInput.get()]);
            changed = true;
        }

        if (changed) {
            markDirty();
        }

        // Preview toggle: reintroduce direct control to allow live scrubbing on the timeline
        boolean inPlayback = ClientCutsceneManager.inCutscene();
        if (inPlayback) {
            ImGui.textDisabled("Disabled while a cutscene is playing.");
            if (previewEnabled.get()) {
                previewEnabled.set(false);
                ClientCutsceneManager.clearPreview();
            }
        } else {
            ImGui.checkbox("Enable Preview##cs_tl_preview", previewEnabled);
        }

        int total = getTotalTicks(c);
        int currentTick = Math.max(0, Math.min(previewTick.get(), total));
        previewTick.set(currentTick);
        ImGui.setNextItemWidth(-1);
        int[] scrub = {previewTick.get()};
        if (ImGui.sliderInt("##cs_tl_scrub", scrub, 0, total)) {
            previewTick.set(scrub[0]);
            // Enable live preview while scrubbing
            previewEnabled.set(true);
        }

        applyPreviewCamera(c, total);
        // If not currently playing, allow user to toggle preview and scrub live in the timeline
        // (Preview toggle is provided below in the UI for clarity)
    }

    private void renderNodeControls(Cutscene c) {
        Minecraft mc = Minecraft.getInstance();
        int nodes = c.getAnchorPointCount();
        ImGui.textDisabled("Nodes: " + nodes + "  |  Splines: " + c.path.splines.size());
        ImGui.spacing();

        if (ImGui.button(ImIcons.FA.FA_PLUS + " Start##cs_node_add_start")) {
            CutsceneEditor.addNodeAtStart(c);
            setStatus("Node added at start.");
        }
        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_PLUS + " End##cs_node_add_end")) {
            CutsceneEditor.addNodeAtEnd(c);
            setStatus("Node added at end.");
        }

        ImGui.spacing();

        ImGui.pushStyleColor(ImGuiCol.Button, 0.55f, 0.25f, 0.10f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.70f, 0.35f, 0.15f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.45f, 0.20f, 0.08f, 1.0f);
        if (ImGui.button(ImIcons.FA.FA_MINUS + " Start##cs_node_rm_start")) {
            boolean deleted = CutsceneEditor.removeFirstNode(c);
            if (deleted) {
                CutsceneUiState.setSelected(null);
                setStatus("Cutscene deleted (was single-point).");
            } else {
                setStatus("First node removed.");
            }
        }
        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_MINUS + " End##cs_node_rm_end")) {
            boolean deleted = CutsceneEditor.removeLastNode(c);
            if (deleted) {
                CutsceneUiState.setSelected(null);
                setStatus("Cutscene deleted (was single-point).");
            } else {
                setStatus("Last node removed.");
            }
        }
        ImGui.popStyleColor(3);

        ImGui.spacing();

        if (nodes == 2) {
            if (ImGui.button(ImIcons.FA.FA_BEZIER_CURVE + " Linearize##cs_node_linearize")) {
                sendCommand("engine cutscene linearize " + c.getName());
                setStatus("Linearized: " + c.getName());
            }
            if (ImGui.isItemHovered())
                ImGui.setTooltip("Straightens the tangent handles so the camera travels in a straight line.");
        }

        ImGui.separator();
        if (mc.player != null && CutsceneItems.EDITOR_ITEM != null) {
            boolean hasItem = mc.player.getMainHandItem().getItem() == CutsceneItems.EDITOR_ITEM
                    || mc.player.getOffhandItem().getItem() == CutsceneItems.EDITOR_ITEM;
            if (!hasItem) {
                if (ImGui.button(ImIcons.FA.FA_PENCIL + " Give Editor Item")) {
                    sendCommand("give " + mc.player.getName().getString() + " foundryengine:editor 1");
                    setStatus("Editor item given. Hold RMB to drag points.");
                }
            } else {
                if (ImGui.button(ImIcons.FA.FA_PENCIL + " Editor Item (active) — click to remove")) {
                    sendCommand("clear " + mc.player.getName().getString() + " foundryengine:editor");
                }
                ImGui.sameLine();
                ImGui.textDisabled("Hold RMB to drag points, scroll to push/pull.");
            }
        }
    }

    private void renderTimelineStrip(Cutscene c, int totalTicks) {
        if (totalTicks < 1) totalTicks = 1;

        float stripHeight = 90f;
        ImGui.beginChild("##cs_tl_strip", 0, stripHeight, true, ImGuiWindowFlags.HorizontalScrollbar);
        try {
            float wheel = ImGui.getIO().getMouseWheel();
            if (ImGui.isWindowHovered() && wheel != 0f) {
                if (ImGui.getIO().getKeyCtrl()) {
                    zoomPxPerTick = Math.clamp(zoomPxPerTick + (wheel > 0 ? 1.0f : -1.0f), 1.0f, 20.0f);
                } else {
                    ImGui.setScrollX(Math.max(0f, ImGui.getScrollX() - (wheel * 40f)));
                }
            }

            float padding = 12f;
            float contentW = Math.max(ImGui.getContentRegionAvailX(), padding * 2 + (totalTicks * zoomPxPerTick));
            float canvasH = stripHeight - ImGui.getStyle().getWindowPaddingY() * 2;

            ImGui.invisibleButton("##cs_tl_canvas", contentW, canvasH);
            var draw = ImGui.getWindowDrawList();

            float minX = ImGui.getItemRectMin().x;
            float minY = ImGui.getItemRectMin().y;
            float maxY = ImGui.getItemRectMax().y;
            float baseY = maxY - 18f;
            float barTopY = minY + 8f;
            float barHeight = 8;
            float barBottomY = barTopY + barHeight;

            draw.addLine(minX, baseY, minX + contentW, baseY, 0x40FFFFFF, 1f);

            int majorEvery = 20;
            int minorEvery = 5;
            for (int t = 0; t <= totalTicks; t += minorEvery) {
                float x = minX + padding + (t * zoomPxPerTick);
                boolean major = (t % majorEvery) == 0;
                float h = major ? 18f : 10f;
                int col = major ? 0xA0FFFFFF : 0x60FFFFFF;
                draw.addLine(x, baseY, x, baseY - h, col, 1f);
                if (major) draw.addText(x + 2, baseY - h - 14f, 0xA0FFFFFF, String.valueOf(t));
            }

            int anchors = c.getAnchorPointCount();
            int len = Math.max(1, c.getDefaultLength());
            int hs = Math.max(0, c.getDefaultHoldStart());
            int nodeCol = 0xB0000000 | (c.getColorArgb() & 0x00FFFFFF);
            for (int i = 0; i < anchors; i++) {
                double distKey = getAnchorDistanceKey(c, i, anchors);
                int tick = Mth.clamp(hs + (int) Math.round(distKey * len), 0, totalTicks);
                float x = minX + padding + (tick * zoomPxPerTick);
                draw.addLine(x, minY + 18f, x, baseY, nodeCol, 2f);
            }

            var effects = c.getScreenEffects();
            for (int i = 0; i < effects.size(); i++) {
                var ev = effects.get(i);
                int startTick = hs + Math.round(ev.at * len);
                int durationTicks = Math.max(1, Math.round((ev.introDuration + ev.holdDuration + ev.outroDuration) * len));
                float x = minX + padding + (startTick * zoomPxPerTick);
                float w = durationTicks * zoomPxPerTick;
                int color = (i == selectedEffectIndex.get()) ? 0xFFFFFFFF : 0x66FFFFFF;
                draw.addRectFilled(x, barTopY, x + w, barBottomY, color);
                draw.addText(x + w * 0.5f, barTopY - 6f, 0xFFFFFFFF, ev.name);
                if (ImGui.isMouseHoveringRect(x, barTopY, x + w, barBottomY)) {
                    ImGui.setTooltip("Effect: " + ev.name + ", at=" + ev.at + ", duration=" + (ev.introDuration + ev.holdDuration + ev.outroDuration));
                }
            }

            float playheadX = minX + padding + (previewTick.get() * zoomPxPerTick);
            draw.addLine(playheadX, minY + 2f, playheadX, maxY - 2f, 0xFFFF6666, 2.5f);

            if (ImGui.isItemHovered() && (ImGui.isMouseDown(0) || ImGui.isMouseClicked(0))) {
                float localX = ImGui.getMousePosX() - minX - padding;
                int tick = Mth.clamp(Math.round(localX / zoomPxPerTick), 0, totalTicks);
                previewTick.set(tick);

                float px = minX + padding + (tick * zoomPxPerTick);
                int picked = -1;
                for (int i = 0; i < effects.size(); i++) {
                    var ev = effects.get(i);
                    int et = hs + Math.round(Mth.clamp(ev.at, 0f, 1f) * len);
                    float ex = minX + padding + (Mth.clamp(et, 0, totalTicks) * zoomPxPerTick);
                    if (Math.abs(ex - px) <= 4f) {
                        picked = i;
                        break;
                    }
                }
                if (picked != -1) {
                    selectedEffectIndex.set(picked);
                    loadEffectEditorFrom(c);
                }
            }
        } finally {
            ImGui.endChild();
        }

        ImGui.textDisabled("Playhead: " + previewTick.get() + " ticks");
    }

    private void renderEffectsEditor(Cutscene c, int totalTicks) {
        var effects = c.getScreenEffects();

        ImGui.text("Screen Effects");
        ImGui.spacing();

        if (ImGui.button(ImIcons.FA.FA_PLUS + " Add at Playhead##cs_eff_add")) {
            float at = computeAtFromPlayhead(c, totalTicks);
            var created = new Cutscene.ScreenEffectEvent(
                    at, ScreenEffectType.cinematic.name(), 0.1f, 0.2f, 0.1f, LerpType.LINEAR.name(), "");
            c.addScreenEffect(created);
            selectedEffectIndex.set(c.getScreenEffects().indexOf(created));
            loadEffectEditorFrom(c);
            ClientPacketDistributor.sendToServer(new CutscenePacket(CutsceneEditor.toNbt()));
        }

        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_EYE + " Preview Type##cs_eff_preview")) {
            int idx = selectedEffectIndex.get();
            if (idx >= 0 && idx < effects.size()) {
                var ev = effects.get(idx);
                int len = Math.max(1, c.getDefaultLength());
                de.luckymcdev.foundryengine.client.cutscene.ClientScreenEffectManager.startEffect(
                        ev.name, ev.getIntroTicks(len), ev.getHoldTicks(len), ev.getOutroTicks(len), ev.lerpType, ev.command);
            }
        }

        if (effects.isEmpty()) {
            ImGui.spacing();
            ImGui.textDisabled("No screen effects.");
            return;
        }

        ImGui.beginChild("##cs_eff_list", 0, 120, true);
        try {
            for (int i = 0; i < effects.size(); i++) {
                var ev = effects.get(i);
                boolean sel = (i == selectedEffectIndex.get());
                String label = String.format("%d) t=%.2f  %s", i, ev.at, ev.name);
                if (ImGui.selectable(label + "##eff" + i, sel, ImGuiSelectableFlags.None)) {
                    selectedEffectIndex.set(sel ? -1 : i);
                    if (selectedEffectIndex.get() >= 0) loadEffectEditorFrom(c);
                }
            }
        } finally {
            ImGui.endChild();
        }

        if (selectedEffectIndex.get() < 0 || selectedEffectIndex.get() >= effects.size()) return;

        ImGui.separator();
        ImGui.text("Selected Effect");

        ImGui.setNextItemWidth(120);
        float[] at = selectedEffectAt.getData();
        ImGui.sliderFloat("At##cs_eff_at", at, 0f, 1f);
        selectedEffectAt.set(at[0]);

        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_LOCATION_DOT + " To Playhead##cs_eff_to_ph")) {
            selectedEffectAt.set(computeAtFromPlayhead(c, totalTicks));
        }

        ImGui.setNextItemWidth(180);
        ImGui.combo("Type##cs_eff_type", effectTypeIndex, EFFECT_TYPES);

        int len = Math.max(1, c.getDefaultLength());
        var itemWidth = 200;
        ImGui.setNextItemWidth(itemWidth);
        int idxSel = selectedEffectIndex.get();
        if (idxSel >= 0 && idxSel < effects.size()) {
            var ev = effects.get(idxSel);
            int atTicks = Math.max(0, Math.min(Math.round(ev.at * len), len));
            ImInt atTicksInput = new ImInt(atTicks);
            if (ImGui.inputInt("At (ticks)##cs_eff_at_ticks", atTicksInput)) {
                ev.at = Math.clamp(atTicksInput.get() / (float) len, 0f, 1f);
            }

            int introTicks = Math.max(0, Math.round(ev.introDuration * len));
            ImInt introTicksInput = new ImInt(introTicks);
            ImGui.setNextItemWidth(itemWidth);
            if (ImGui.inputInt("Intro (ticks)##cs_eff_intro_ticks", introTicksInput)) {
                ev.introDuration = Math.max(0f, introTicksInput.get()) / (float) len;
            }

            int holdTicks = Math.max(0, Math.round(ev.holdDuration * len));
            ImInt holdTicksInput = new ImInt(holdTicks);
            ImGui.setNextItemWidth(itemWidth);
            if (ImGui.inputInt("Hold (ticks)##cs_eff_hold_ticks", holdTicksInput)) {
                ev.holdDuration = Math.max(0f, holdTicksInput.get()) / (float) len;
            }

            int outroTicks = Math.max(0, Math.round(ev.outroDuration * len));
            ImInt outroTicksInput = new ImInt(outroTicks);
            ImGui.setNextItemWidth(itemWidth);
            if (ImGui.inputInt("Outro (ticks)##cs_eff_outro_ticks", outroTicksInput)) {
                ev.outroDuration = Math.max(0f, outroTicksInput.get()) / (float) len;
            }
        }

        if (ImGui.collapsingHeader("Relative Settings")) {
            ImGui.setNextItemWidth(itemWidth);
            ImGui.sliderFloat("Intro##cs_eff_intro", effectIntroDuration.getData(), 0f, 1f, "%.2f");
            if (effectIntroDuration.get() < 0) effectIntroDuration.set(0f);

            ImGui.setNextItemWidth(itemWidth);
            ImGui.sliderFloat("Hold##cs_eff_hold", effectHoldDuration.getData(), 0f, 1f, "%.2f");
            if (effectHoldDuration.get() < 0) effectHoldDuration.set(0f);

            ImGui.setNextItemWidth(itemWidth);
            ImGui.sliderFloat("Outro##cs_eff_outro", effectOutroDuration.getData(), 0f, 1f, "%.2f");
            if (effectOutroDuration.get() < 0) effectOutroDuration.set(0f);
        }

        ImGui.setNextItemWidth(itemWidth);
        ImGui.combo("Easing##cs_eff_ease", effectEasingIndex, EASING_NAMES);

        ImGui.setNextItemWidth(-1);
        ImGui.inputTextWithHint("Command##cs_eff_cmd", "optional server command", effectCommand);

        if (ImGui.button(ImIcons.FA.FA_CHECK + " Apply##cs_eff_apply")) {
            int idx = selectedEffectIndex.get();
            var ev = effects.get(idx);
            // Persist all fields updated via the UI. In particular, do not override tick-based edits
            // with 0..1 sliders unless the UI explicitly changed them. Here, we rely on whatever is
            // currently stored in ev.* values.
            ev.at = Math.max(0f, Math.min(1f, selectedEffectAt.get()));
            ev.name = EFFECT_TYPES[Math.clamp(effectTypeIndex.get(), 0, EFFECT_TYPES.length - 1)];
            ev.lerpType = EASING_NAMES[Math.clamp(effectEasingIndex.get(), 0, EASING_NAMES.length - 1)];
            ev.command = effectCommand.get();
            ClientPacketDistributor.sendToServer(new CutscenePacket(CutsceneEditor.toNbt()));
        }
        ImGui.sameLine();
        ImGui.pushStyleColor(ImGuiCol.Button, 0.55f, 0.10f, 0.10f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.70f, 0.15f, 0.15f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.45f, 0.08f, 0.08f, 1.0f);
        if (ImGui.button(ImIcons.FA.FA_TRASH + " Remove##cs_eff_rm")) {
            c.removeScreenEffect(selectedEffectIndex.get());
            selectedEffectIndex.set(-1);
            ClientPacketDistributor.sendToServer(new CutscenePacket(CutsceneEditor.toNbt()));
        }
        ImGui.popStyleColor(3);
    }

    private void loadEffectEditorFrom(Cutscene c) {
        int idx = selectedEffectIndex.get();
        if (idx < 0 || idx >= c.getScreenEffects().size()) return;
        var ev = c.getScreenEffects().get(idx);

        selectedEffectAt.set(ev.at);

        int tIdx = 0;
        for (int i = 0; i < EFFECT_TYPES.length; i++) {
            if (EFFECT_TYPES[i].equalsIgnoreCase(ev.name)) {
                tIdx = i;
                break;
            }
        }
        effectTypeIndex.set(tIdx);

        effectIntroDuration.set(Math.max(0f, ev.introDuration));
        effectHoldDuration.set(Math.max(0f, ev.holdDuration));
        effectOutroDuration.set(Math.max(0f, ev.outroDuration));

        int eIdx = 0;
        for (int i = 0; i < EASING_NAMES.length; i++) {
            if (EASING_NAMES[i].equalsIgnoreCase(ev.lerpType)) {
                eIdx = i;
                break;
            }
        }
        effectEasingIndex.set(eIdx);
        effectCommand.set(ev.command == null ? "" : ev.command);
    }

    private int getTotalTicks(Cutscene c) {
        int len = Math.max(1, c.getDefaultLength());
        int hs = Math.max(0, c.getDefaultHoldStart());
        int he = Math.max(0, c.getDefaultHoldEnd());
        return len + hs + he;
    }

    private void applyPreviewCamera(Cutscene c, int totalTicks) {
        if (!previewEnabled.get() || ClientCutsceneManager.inCutscene()) {
            ClientCutsceneManager.clearPreview();
            return;
        }

        int len = Math.max(1, c.getDefaultLength());
        int hs = Math.max(0, c.getDefaultHoldStart());
        int tick = Math.min(previewTick.get(), totalTicks);

        float t;
        if (tick <= hs) t = 0f;
        else if (tick >= hs + len) t = 1f;
        else t = (tick - hs) / (float) len;

        ClientCutsceneManager.setPreview(c, t);
    }

    private float computeAtFromPlayhead(Cutscene c, int totalTicks) {
        int len = Math.max(1, c.getDefaultLength());
        int hs = Math.max(0, c.getDefaultHoldStart());
        int tick = Math.max(0, Math.min(previewTick.get(), totalTicks));
        if (tick <= hs) return 0f;
        if (tick >= hs + len) return 1f;
        return (tick - hs) / (float) len;
    }

    private double getAnchorDistanceKey(Cutscene c, int anchorIndex, int anchors) {
        if (anchors <= 1 || anchorIndex <= 0) return 0.0;
        if (anchorIndex >= anchors - 1) return 1.0;

        int splineCount = c.path.splines.size();
        if (splineCount <= 0) return (double) anchorIndex / (double) (anchors - 1);

        double time = (double) anchorIndex / (double) splineCount;
        return c.path.getNormalizedDistanceAtTime(time);
    }

    private void sendCommand(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().sendCommand(command);
        }
    }

    private void setStatus(String message) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(message));
        }
    }

    private void markDirty() {
        ClientPacketDistributor.sendToServer(new CutscenePacket(CutsceneEditor.toNbt()));
    }
}
