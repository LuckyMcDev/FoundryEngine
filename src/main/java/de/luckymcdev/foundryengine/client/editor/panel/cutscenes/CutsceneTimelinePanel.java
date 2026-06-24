package de.luckymcdev.foundryengine.client.editor.panel.cutscenes;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.cutscene.CutsceneRenderer;
import de.luckymcdev.foundryengine.client.cutscene.CutsceneUiState;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.model.CommandAttachment;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.cutscene.model.CutsceneAttachment;
import de.luckymcdev.foundryengine.common.cutscene.model.EffectAttachment;
import de.luckymcdev.foundryengine.common.cutscene.util.LerpType;

import de.luckymcdev.foundryengine.common.network.packets.editor.CutscenePacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.LinearizeCutscenePacket;
import de.luckymcdev.foundryengine.common.util.color.Color;
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
import net.minecraft.network.chat.Component;
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
    private static final String[] EFFECT_TYPES = {"none", "black", "circle", "star", "cinematic", "grayscale", "sepia", "depth_vis"};
    private static final String[] ATTACHMENT_TYPES = new String[]{"Effect", "Command"};

    private final ImBoolean previewEnabled = new ImBoolean(false);
    private final ImInt previewTick = new ImInt(0);
    private final ImInt selectedAttachmentIndex = new ImInt(-1);
    private final ImInt selectedAttachmentTypeIndex = new ImInt(0);
    private final ImFloat effectAt = new ImFloat(0f);
    private final ImInt effectTypeIndex = new ImInt(0);
    private final ImFloat effectIntroDuration = new ImFloat(0.1f);
    private final ImFloat effectHoldDuration = new ImFloat(0.2f);
    private final ImFloat effectOutroDuration = new ImFloat(0.1f);
    private final ImInt effectEasingIndex = new ImInt(0);
    private final ImString commandText = new ImString(256);
    private final ImFloat commandDelay = new ImFloat(0f);
    private float zoomPxPerTick = 6.0f;

    private CutsceneTimelinePanel() {
        super(new Builder(Common.id("cutscene_timeline"), "Cutscene Timeline")
                .icon(ImIcons.FA.FA_SLIDERS)
                .category(PanelCategory.EDITOR_CUTSCENES)
                .menuBar(true));
    }

    @Override
    public void onClosed() {
        Client.getCutsceneManager().clearPreview();
        previewEnabled.set(false);
    }

    @Override
    public void content() {
        if (!requireWorld()) {
            return;
        }

        renderMenuBar();

        List<Cutscene> cutscenes = CutsceneRenderer.getCutscenes();

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
                    Color color = new Color(c.getColorArgb());
                    ImGui.colorButton("##cs_color",
                            color.r(), color.g(), color.b(), color.a(),
                            0, 12, 12);
                    ImGui.sameLine();

                    if (ImGui.selectable(c.getName() + "##cs", sel, ImGuiSelectableFlags.None)) {
                        CutsceneUiState.setSelected(c);
                        selectedAttachmentIndex.set(-1);
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
                Client.getCutsceneManager().clearPreview();
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
        menuBar(() -> {
            if (ImGui.menuItem(ImIcons.FA.FA_ROTATE_RIGHT + " Sync")) {
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("Request", true);
                ClientPacketDistributor.sendToServer(new CutscenePacket(tag));
            }
        });
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
        boolean inPlayback = Client.getCutsceneManager().inCutscene();
        if (inPlayback) {
            ImGui.textDisabled("Disabled while a cutscene is playing.");
            if (previewEnabled.get()) {
                previewEnabled.set(false);
                Client.getCutsceneManager().clearPreview();
            }
        } else {
            ImGui.checkbox("Enable Preview##cs_tl_preview", previewEnabled);
        }

        int total = getTotalTicks(c);
        int currentTick = Math.clamp(previewTick.get(), 0, total);
        previewTick.set(currentTick);
        ImGui.setNextItemWidth(-1);
        int[] scrub = {previewTick.get()};
        if (ImGui.sliderInt("##cs_tl_scrub", scrub, 0, total)) {
            previewTick.set(scrub[0]);
            previewEnabled.set(true);
        }

        applyPreviewCamera(c, total);
    }

    private void renderNodeControls(Cutscene c) {
        Minecraft mc = Minecraft.getInstance();
        int nodes = c.getAnchorPointCount();
        ImGui.textDisabled("Nodes: " + nodes + "  |  Splines: " + c.path.splines.size());
        ImGui.spacing();

        if (ImGui.button(ImIcons.FA.FA_PLUS + " Start##cs_node_add_start")) {
            Client.getCutsceneEditor().addNodeAtStart(c);
            sendChatStatus("Node added at start.");
        }
        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_PLUS + " End##cs_node_add_end")) {
            Client.getCutsceneEditor().addNodeAtEnd(c);
            sendChatStatus("Node added at end.");
        }

        ImGui.spacing();

        ImGui.pushStyleColor(ImGuiCol.Button, 0.55f, 0.25f, 0.10f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.70f, 0.35f, 0.15f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.45f, 0.20f, 0.08f, 1.0f);
        if (ImGui.button(ImIcons.FA.FA_MINUS + " Start##cs_node_rm_start")) {
            boolean deleted = Client.getCutsceneEditor().removeFirstNode(c);
            if (deleted) {
                CutsceneUiState.setSelected(null);
                sendChatStatus("Cutscene deleted (was single-point).");
            } else {
                sendChatStatus("First node removed.");
            }
        }
        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_MINUS + " End##cs_node_rm_end")) {
            boolean deleted = Client.getCutsceneEditor().removeLastNode(c);
            if (deleted) {
                CutsceneUiState.setSelected(null);
                sendChatStatus("Cutscene deleted (was single-point).");
            } else {
                sendChatStatus("Last node removed.");
            }
        }
        ImGui.popStyleColor(3);

        ImGui.spacing();

        if (nodes == 2) {
            if (ImGui.button(ImIcons.FA.FA_BEZIER_CURVE + " Linearize##cs_node_linearize")) {
                ClientPacketDistributor.sendToServer(new LinearizeCutscenePacket(c.getName()));
                sendChatStatus("Linearized: " + c.getName());
            }
            if (ImGui.isItemHovered())
                ImGui.setTooltip("Straightens the tangent handles so the camera travels in a straight line.");
        }

        ImGui.separator();
        ImGui.text("Per-Node Hold Ticks");
        var holdTicks = c.getAnchorHoldTicks();
        if (holdTicks.size() != nodes) {
            // ensureAnchorRotations was called; sync
        }
        for (int ni = 0; ni < nodes && ni < holdTicks.size(); ni++) {
            ImGui.pushID("hold_" + ni);
            ImGui.setNextItemWidth(80);
            int ht = holdTicks.get(ni);
            ImInt htInput = new ImInt(ht);
            String label = "Node " + ni + " Hold##hold";
            if (ImGui.inputInt(label, htInput)) {
                c.setAnchorHoldTicks(ni, Math.max(0, htInput.get()));
                markDirty();
            }
            ImGui.popID();
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
            var holdTicks = c.getAnchorHoldTicks();
            int nodeCol = (new Color(c.getColorArgb()).argb() & 0x00FFFFFF) | 0xB0000000;
            int holdCol = (new Color(c.getColorArgb()).argb() & 0x00FFFFFF) | 0x30000000;

            // Compute anchor tick positions accounting for prior holds and segment durations
            double[] keys = c.getAnchorDistanceKeys();
            int[] anchorTicks = new int[anchors];
            anchorTicks[0] = 0;
            if (anchors > 1) {
                double totalWeight = 0;
                for (int i = 0; i < anchors - 1; i++) {
                    totalWeight += keys[i + 1] - keys[i];
                }
                if (totalWeight <= 0) totalWeight = 1;

                int cursor = hs;
                for (int seg = 0; seg < anchors - 1; seg++) {
                    double segWeight = keys[seg + 1] - keys[seg];
                    cursor += Math.round((segWeight / totalWeight) * len);
                    anchorTicks[seg + 1] = cursor;
                    cursor += holdTicks.get(seg + 1);
                }
            }

            for (int i = 0; i < anchors; i++) {
                int tick = Mth.clamp(anchorTicks[i], 0, totalTicks);
                float x = minX + padding + (tick * zoomPxPerTick);
                draw.addLine(x, minY + 18f, x, baseY, nodeCol, 2f);

                // Draw hold zone after interior anchors
                if (i > 0 && i < anchors - 1) {
                    int holdEndTick = Mth.clamp(anchorTicks[i] + holdTicks.get(i), 0, totalTicks);
                    float hx1 = x;
                    float hx2 = minX + padding + (holdEndTick * zoomPxPerTick);
                    draw.addRectFilled(hx1, minY + 18f, hx2, baseY, holdCol);
                }
            }

            var attachments = c.getAttachments();
            for (int i = 0; i < attachments.size(); i++) {
                var att = attachments.get(i);
                int startTick = computeTickForT(c, Math.clamp(att.getAt(), 0f, 1f), len, hs);
                float duration = att.getDuration();
                int durationTicks = Math.max(1, Math.round(duration * len));
                float x = minX + padding + (startTick * zoomPxPerTick);
                float w = Math.max(zoomPxPerTick, durationTicks * zoomPxPerTick);

                int color;
                int textColor = 0xFFFFFFFF;
                String label;

                if (att instanceof EffectAttachment eff) {
                    color = (i == selectedAttachmentIndex.get()) ? 0xFFFFFFFF : 0x66FFCC00;
                    label = eff.getEffectName();
                } else if (att instanceof CommandAttachment cmd) {
                    color = (i == selectedAttachmentIndex.get()) ? 0xFFFFFFFF : 0x6600CCFF;
                    String cmdPreview = cmd.getCommand().length() > 15 ? cmd.getCommand().substring(0, 15) + "..." : cmd.getCommand();
                    label = "Cmd: " + cmdPreview;
                } else {
                    color = (i == selectedAttachmentIndex.get()) ? 0xFFFFFFFF : 0x66666666;
                    label = "Unknown";
                }

                draw.addRectFilled(x, barTopY, x + w, barBottomY, color);
                draw.addText(x + 2, barTopY - 6f, textColor, label);
                if (ImGui.isMouseHoveringRect(x, barTopY, x + w, barBottomY)) {
                    ImGui.setTooltip(att.getDisplayName() + " at=" + att.getAt() + ", duration=" + duration);
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
                for (int i = 0; i < attachments.size(); i++) {
                    var att = attachments.get(i);
                    int et = computeTickForT(c, Mth.clamp(att.getAt(), 0f, 1f), len, hs);
                    float ex = minX + padding + (Mth.clamp(et, 0, totalTicks) * zoomPxPerTick);
                    float dur = att.getDuration();
                    float exEnd = ex + Math.max(zoomPxPerTick, (dur * len * zoomPxPerTick));
                    if (px >= ex - 4f && px <= exEnd + 4f) {
                        picked = i;
                        break;
                    }
                }
                if (picked != -1) {
                    selectedAttachmentIndex.set(picked);
                    loadAttachmentEditorFrom(c);
                }
            }
        } finally {
            ImGui.endChild();
        }

        int ah = c.getTotalAnchorHoldTicks();
        String holdInfo = ah > 0 ? "  |  Anchor holds: " + ah + "t" : "";
        ImGui.textDisabled("Playhead: " + previewTick.get() + " ticks / " + totalTicks + " total" + holdInfo);
    }

    private void renderEffectsEditor(Cutscene c, int totalTicks) {
        var attachments = c.getAttachments();

        ImGui.text("Attachments");
        ImGui.spacing();

        // Add new attachment dropdown
        ImGui.setNextItemWidth(100);
        if (ImGui.combo("##cs_att_type_select", selectedAttachmentTypeIndex, ATTACHMENT_TYPES)) {
            // Selection changed
        }
        ImGui.sameLine();

        if (ImGui.button(ImIcons.FA.FA_PLUS + " Add at Playhead##cs_att_add")) {
            float at = computeAtFromPlayhead(c, totalTicks);
            int typeIdx = selectedAttachmentTypeIndex.get();
            CutsceneAttachment newAtt;
            if (typeIdx == 1) { // Command
                newAtt = new CommandAttachment(at, "", 0f);
            } else { // Effect (default)
                newAtt = new EffectAttachment(at, "cinematic", 0.1f, 0.2f, 0.1f, LerpType.LINEAR.name());
            }
            c.addAttachment(newAtt);
            selectedAttachmentIndex.set(attachments.indexOf(newAtt));
            loadAttachmentEditorFrom(c);
            ClientPacketDistributor.sendToServer(new CutscenePacket(Client.getCutsceneEditor().toNbt()));
        }

        // Preview button for effects
        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_EYE + " Preview##cs_att_preview")) {
            int idx = selectedAttachmentIndex.get();
            if (idx >= 0 && idx < attachments.size()) {
                var att = attachments.get(idx);
                if (att instanceof EffectAttachment eff) {
                    int len = Math.max(1, c.getDefaultLength());
                    Client.getPostEffectManager().startScreenEffect(
                            eff.getEffectName(), eff.getIntroTicks(len), eff.getHoldTicks(len), eff.getOutroTicks(len), eff.getLerpType());
                }
            }
        }

        if (attachments.isEmpty()) {
            ImGui.spacing();
            ImGui.textDisabled("No attachments. Click '+' to add one.");
            return;
        }

        // Attachment list
        ImGui.beginChild("##cs_att_list", 0, 120, true);
        try {
            for (int i = 0; i < attachments.size(); i++) {
                var att = attachments.get(i);
                boolean sel = (i == selectedAttachmentIndex.get());
                String label = String.format("%d) t=%.2f  %s", i, att.getAt(), att.getDisplayName());
                if (ImGui.selectable(label + "##att" + i, sel, ImGuiSelectableFlags.None)) {
                    selectedAttachmentIndex.set(sel ? -1 : i);
                    if (selectedAttachmentIndex.get() >= 0) loadAttachmentEditorFrom(c);
                }
            }
        } finally {
            ImGui.endChild();
        }

        if (selectedAttachmentIndex.get() < 0 || selectedAttachmentIndex.get() >= attachments.size()) return;

        ImGui.separator();
        ImGui.text("Selected Attachment");

        var selectedAtt = attachments.get(selectedAttachmentIndex.get());

        // Common: At position
        ImGui.setNextItemWidth(120);
        float[] at = new float[]{selectedAtt.getAt()};
        ImGui.sliderFloat("At##cs_att_at", at, 0f, 1f);
        selectedAtt.setAt(at[0]);

        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_LOCATION_DOT + " To Playhead##cs_att_to_ph")) {
            selectedAtt.setAt(computeAtFromPlayhead(c, totalTicks));
        }

        // Type-specific editor
        if (selectedAtt instanceof EffectAttachment eff) {
            renderEffectAttachmentEditor(c, eff, totalTicks);
        } else if (selectedAtt instanceof CommandAttachment cmd) {
            renderCommandAttachmentEditor(c, cmd, totalTicks);
        }

        // Apply/Remove buttons
        if (ImGui.button(ImIcons.FA.FA_CHECK + " Apply##cs_att_apply")) {
            ClientPacketDistributor.sendToServer(new CutscenePacket(Client.getCutsceneEditor().toNbt()));
        }
        ImGui.sameLine();
        ImGui.pushStyleColor(ImGuiCol.Button, 0.55f, 0.10f, 0.10f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.70f, 0.15f, 0.15f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.45f, 0.08f, 0.08f, 1.0f);
        if (ImGui.button(ImIcons.FA.FA_TRASH + " Remove##cs_att_rm")) {
            c.removeAttachment(selectedAtt);
            selectedAttachmentIndex.set(-1);
            ClientPacketDistributor.sendToServer(new CutscenePacket(Client.getCutsceneEditor().toNbt()));
        }
        ImGui.popStyleColor(3);
    }

    private void renderEffectAttachmentEditor(Cutscene c, EffectAttachment eff, int totalTicks) {
        ImGui.setNextItemWidth(180);
        int typeIdx = 0;
        for (int i = 0; i < EFFECT_TYPES.length; i++) {
            if (EFFECT_TYPES[i].equalsIgnoreCase(eff.getEffectName())) {
                typeIdx = i;
                break;
            }
        }
        ImInt typeIdxInput = new ImInt(typeIdx);
        if (ImGui.combo("Effect Type##cs_eff_type", typeIdxInput, EFFECT_TYPES)) {
            eff.setEffectName(EFFECT_TYPES[typeIdxInput.get()]);
        }

        int len = Math.max(1, c.getDefaultLength());
        int itemWidth = 200;

        int introTicks = Math.max(0, Math.round(eff.getIntroDuration() * len));
        ImInt introTicksInput = new ImInt(introTicks);
        ImGui.setNextItemWidth(itemWidth);
        if (ImGui.inputInt("Intro (ticks)##cs_eff_intro", introTicksInput)) {
            eff.setIntroDuration(Math.max(0f, introTicksInput.get()) / (float) len);
        }

        int holdTicks = Math.max(0, Math.round(eff.getHoldDuration() * len));
        ImInt holdTicksInput = new ImInt(holdTicks);
        ImGui.setNextItemWidth(itemWidth);
        if (ImGui.inputInt("Hold (ticks)##cs_eff_hold", holdTicksInput)) {
            eff.setHoldDuration(Math.max(0f, holdTicksInput.get()) / (float) len);
        }

        int outroTicks = Math.max(0, Math.round(eff.getOutroDuration() * len));
        ImInt outroTicksInput = new ImInt(outroTicks);
        ImGui.setNextItemWidth(itemWidth);
        if (ImGui.inputInt("Outro (ticks)##cs_eff_outro", outroTicksInput)) {
            eff.setOutroDuration(Math.max(0f, outroTicksInput.get()) / (float) len);
        }

        if (ImGui.collapsingHeader("Relative Settings##eff")) {
            ImGui.setNextItemWidth(itemWidth);
            float introRel = eff.getIntroDuration();
            ImFloat introRelInput = new ImFloat(introRel);
            if (ImGui.sliderFloat("Intro##cs_eff_intro_rel", introRelInput.getData(), 0f, 1f, "%.2f")) {
                eff.setIntroDuration(Math.max(0f, introRelInput.get()));
            }

            ImGui.setNextItemWidth(itemWidth);
            float holdRel = eff.getHoldDuration();
            ImFloat holdRelInput = new ImFloat(holdRel);
            if (ImGui.sliderFloat("Hold##cs_eff_hold_rel", holdRelInput.getData(), 0f, 1f, "%.2f")) {
                eff.setHoldDuration(Math.max(0f, holdRelInput.get()));
            }

            ImGui.setNextItemWidth(itemWidth);
            float outroRel = eff.getOutroDuration();
            ImFloat outroRelInput = new ImFloat(outroRel);
            if (ImGui.sliderFloat("Outro##cs_eff_outro_rel", outroRelInput.getData(), 0f, 1f, "%.2f")) {
                eff.setOutroDuration(Math.max(0f, outroRelInput.get()));
            }
        }

        ImGui.setNextItemWidth(itemWidth);
        int easeIdx = 0;
        for (int i = 0; i < EASING_NAMES.length; i++) {
            if (EASING_NAMES[i].equalsIgnoreCase(eff.getLerpType())) {
                easeIdx = i;
                break;
            }
        }
        ImInt easeInput = new ImInt(easeIdx);
        if (ImGui.combo("Easing##cs_eff_ease", easeInput, EASING_NAMES)) {
            eff.setLerpType(EASING_NAMES[easeInput.get()]);
        }
    }

    private void renderCommandAttachmentEditor(Cutscene c, CommandAttachment cmd, int totalTicks) {
        ImGui.setNextItemWidth(-1);
        ImString cmdInput = new ImString(cmd.getCommand(), 256);
        if (ImGui.inputTextWithHint("Command##cs_cmd_text", "server command (e.g., /say Hello)", cmdInput)) {
            cmd.setCommand(cmdInput.get());
        }

        int len = Math.max(1, c.getDefaultLength());
        int itemWidth = 200;

        int delayTicks = Math.max(0, Math.round(cmd.getDelay() * len));
        ImInt delayTicksInput = new ImInt(delayTicks);
        ImGui.setNextItemWidth(itemWidth);
        if (ImGui.inputInt("Delay (ticks)##cs_cmd_delay", delayTicksInput)) {
            cmd.setDelay(Math.max(0f, delayTicksInput.get()) / (float) len);
        }

        if (ImGui.collapsingHeader("Relative Settings##cmd")) {
            ImGui.setNextItemWidth(itemWidth);
            float delayRel = cmd.getDelay();
            ImFloat delayRelInput = new ImFloat(delayRel);
            if (ImGui.sliderFloat("Delay##cs_cmd_delay_rel", delayRelInput.getData(), 0f, 1f, "%.2f")) {
                cmd.setDelay(Math.max(0f, delayRelInput.get()));
            }
        }

        ImGui.textDisabled(String.format("Trigger at: %.2f (tick %d)", cmd.getEffectiveAt(), cmd.getTriggerTick(len)));
    }

    private void loadAttachmentEditorFrom(Cutscene c) {
        int idx = selectedAttachmentIndex.get();
        if (idx < 0 || idx >= c.getAttachments().size()) return;
        var att = c.getAttachments().get(idx);

        if (att instanceof EffectAttachment eff) {
            effectAt.set(eff.getAt());

            int tIdx = 0;
            for (int i = 0; i < EFFECT_TYPES.length; i++) {
                if (EFFECT_TYPES[i].equalsIgnoreCase(eff.getEffectName())) {
                    tIdx = i;
                    break;
                }
            }
            effectTypeIndex.set(tIdx);

            effectIntroDuration.set(Math.max(0f, eff.getIntroDuration()));
            effectHoldDuration.set(Math.max(0f, eff.getHoldDuration()));
            effectOutroDuration.set(Math.max(0f, eff.getOutroDuration()));

            int eIdx = 0;
            for (int i = 0; i < EASING_NAMES.length; i++) {
                if (EASING_NAMES[i].equalsIgnoreCase(eff.getLerpType())) {
                    eIdx = i;
                    break;
                }
            }
            effectEasingIndex.set(eIdx);
        } else if (att instanceof CommandAttachment cmd) {
            commandText.set(cmd.getCommand() == null ? "" : cmd.getCommand());
            commandDelay.set(Math.max(0f, cmd.getDelay()));
        }
    }

    private int getTotalTicks(Cutscene c) {
        int len = Math.max(1, c.getDefaultLength());
        int hs = Math.max(0, c.getDefaultHoldStart());
        int he = Math.max(0, c.getDefaultHoldEnd());
        return len + hs + he + c.getTotalAnchorHoldTicks();
    }

    private void applyPreviewCamera(Cutscene c, int totalTicks) {
        if (!previewEnabled.get() || Client.getCutsceneManager().inCutscene()) {
            Client.getCutsceneManager().clearPreview();
            return;
        }

        int len = Math.max(1, c.getDefaultLength());
        int hs = Math.max(0, c.getDefaultHoldStart());
        int he = Math.max(0, c.getDefaultHoldEnd());
        int tick = Math.min(previewTick.get(), totalTicks);

        float t = c.computeProgress(tick, len, hs, he);
        Client.getCutsceneManager().setPreview(c, t);
    }

    private float computeAtFromPlayhead(Cutscene c, int totalTicks) {
        int len = Math.max(1, c.getDefaultLength());
        int hs = Math.max(0, c.getDefaultHoldStart());
        int he = Math.max(0, c.getDefaultHoldEnd());
        int tick = Math.max(0, Math.min(previewTick.get(), totalTicks));
        return c.computeProgress(tick, len, hs, he);
    }

    /**
     * Converts a path progress t (0..1) to a timeline tick position, accounting for per-anchor holds.
     */
    private int computeTickForT(Cutscene c, float t, int len, int hs) {
        if (t <= 0) return 0;
        int totalTicks = getTotalTicks(c);
        if (t >= 1) return totalTicks;

        double[] keys = c.getAnchorDistanceKeys();
        var holdTicks = c.getAnchorHoldTicks();
        int anchorCount = holdTicks.size();
        int segmentCount = anchorCount - 1;

        if (segmentCount <= 0) return hs;

        // Find which segment t falls into
        int seg = segmentCount - 1;
        for (int i = 0; i < segmentCount; i++) {
            if (t >= keys[i] && t <= keys[i + 1]) {
                seg = i;
                break;
            }
        }

        double totalWeight = 0;
        for (int i = 0; i < segmentCount; i++) {
            totalWeight += keys[i + 1] - keys[i];
        }
        if (totalWeight <= 0) totalWeight = 1;

        int tick = hs;
        for (int s = 0; s < seg; s++) {
            double sw = keys[s + 1] - keys[s];
            tick += Math.round((sw / totalWeight) * len);
            tick += holdTicks.get(s + 1);
        }

        double sw = keys[seg + 1] - keys[seg];
        int segDuration = Math.round((float) (sw / totalWeight) * len);
        if (segDuration > 0 && keys[seg + 1] > keys[seg]) {
            double localT = (t - keys[seg]) / (keys[seg + 1] - keys[seg]);
            tick += Math.round(localT * segDuration);
        }

        return Math.min(tick, totalTicks);
    }

    private double getAnchorDistanceKey(Cutscene c, int anchorIndex, int anchors) {
        if (anchors <= 1 || anchorIndex <= 0) return 0.0;
        if (anchorIndex >= anchors - 1) return 1.0;

        int splineCount = c.path.splines.size();
        if (splineCount <= 0) return (double) anchorIndex / (double) (anchors - 1);

        double time = (double) anchorIndex / (double) splineCount;
        return c.path.getNormalizedDistanceAtTime(time);
    }

    private void sendChatStatus(String message) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal(message));
        }
    }

    private void markDirty() {
        ClientPacketDistributor.sendToServer(new CutscenePacket(Client.getCutsceneEditor().toNbt()));
    }
}
