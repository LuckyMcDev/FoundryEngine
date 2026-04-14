package de.luckymcdev.foundryengine.client.editor.builtin.scene;

import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.scene.SelectionManager;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.scene.EngineSceneNode;
import de.luckymcdev.foundryengine.common.scene.EntitySceneNode;
import de.luckymcdev.foundryengine.common.scene.PointNode;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Set;

public class PropertiesPanel extends EditorPanel {
    public static final PropertiesPanel INSTANCE = new PropertiesPanel();
    private EngineSceneNode currentTarget;

    public PropertiesPanel() {
        super(Common.id("properties"), "Properties", ImIcons.FA.FA_SLIDERS, Shortcut.empty());
    }

    @Override
    public void content() {
        currentTarget = SelectionManager.getSelected();
        if (currentTarget == null) {
            ImGui.textDisabled("No object selected.");
            return;
        }

        ImGui.text(ImIcons.FA.FA_CUBE + " " + currentTarget.getDisplayName());
        ImGui.sameLine();
        ImGui.textDisabled("(" + currentTarget.getTypeName() + ")");
        ImGui.separator();

        if (ImGui.beginTable("##props_table", 2)) {
            renderStaticProperty("UUID", currentTarget.getUUID());
            renderStaticProperty("Type", currentTarget.getTypeName());
            renderReadOnlyDisplayName();
            renderPositionSliders();
            renderRotationSliders();

            Map<String, Object> props = currentTarget.getProperties();
            if (!props.isEmpty()) {
                ImGui.tableNextColumn();
                ImGui.separator();
                ImGui.tableNextColumn();
                ImGui.separator();

                for (Map.Entry<String, Object> entry : props.entrySet()) {
                    renderCustomProperty(entry.getKey(), entry.getValue());
                }
            }

            ImGui.endTable();
        }
    }

    private void renderStaticProperty(String label, String value) {
        ImGui.tableNextColumn();
        ImGui.textDisabled(label);
        ImGui.tableNextColumn();
        ImGui.text(value);
    }

    private void renderReadOnlyDisplayName() {
        ImGui.tableNextColumn();
        ImGui.textDisabled("Display Name");
        ImGui.tableNextColumn();
        ImGui.text(currentTarget.getDisplayName());
    }

    private void renderPositionSliders() {
        ImGui.tableNextColumn();
        ImGui.text("Position");
        ImGui.tableNextColumn();

        Vector3f pos = currentTarget.getPosition();
        boolean editable = currentTarget.editable();

        if (editable) {
            float[] x = {pos.x};
            float[] y = {pos.y};
            float[] z = {pos.z};

            float min = -500.0f;
            float max = 500.0f;

            ImGui.setNextItemWidth(-1);
            boolean changed = ImGui.sliderFloat("X", x, min, max, "%.2f");
            if (ImGui.sliderFloat("Y", y, min, max, "%.2f")) changed = true;
            if (ImGui.sliderFloat("Z", z, min, max, "%.2f")) changed = true;

            if (changed) {
                Vector3f newPos = new Vector3f(x[0], y[0], z[0]);
                if (currentTarget instanceof PointNode point) {
                    point.setPosition(newPos);
                } else if (currentTarget instanceof EntitySceneNode entityNode) {
                    var entity = entityNode.asEntity();
                    if (entity != null && entity.level() instanceof ServerLevel serverLevel) {
                        Vector2f rot = currentTarget.getRotation();
                        entity.teleportTo(serverLevel, newPos.x, newPos.y, newPos.z,
                                Set.of(), rot.x, rot.y, false);
                    }
                }
            }
        } else {
            ImGui.textDisabled(String.format("%.2f, %.2f, %.2f", pos.x, pos.y, pos.z));
        }
    }

    private void renderRotationSliders() {
        ImGui.tableNextColumn();
        ImGui.text("Rotation");
        ImGui.tableNextColumn();

        Vector2f rot = currentTarget.getRotation();
        boolean editable = (currentTarget instanceof PointNode) || (currentTarget instanceof EntitySceneNode);

        if (editable) {
            float[] yaw = {rot.x};
            float[] pitch = {rot.y};
            boolean changed = false;

            ImGui.setNextItemWidth(-1);
            if (ImGui.sliderFloat("Yaw", yaw, -180.0f, 180.0f, "%.2f")) changed = true;
            if (ImGui.sliderFloat("Pitch", pitch, -180.0f, 180.0f, "%.2f")) changed = true;

            if (changed) {
                Vector2f newRot = new Vector2f(yaw[0], pitch[0]);
                if (currentTarget instanceof PointNode point) {
                    point.setRotation(newRot);
                } else if (currentTarget instanceof EntitySceneNode entityNode) {
                    var entity = entityNode.asEntity();
                    if (entity != null && entity.level() instanceof ServerLevel serverLevel) {
                        Vector3f pos = currentTarget.getPosition();
                        entity.teleportTo(serverLevel, pos.x, pos.y, pos.z,
                                Set.of(), newRot.x, newRot.y, false);
                    }
                }
            }
        } else {
            ImGui.textDisabled(String.format("%.2f, %.2f", rot.x, rot.y));
        }
    }

    private void renderCustomProperty(String key, Object value) {
        ImGui.tableNextColumn();
        ImGui.text(key);
        ImGui.tableNextColumn();
        ImGui.setNextItemWidth(-1);

        if (value instanceof Float) {
            ImFloat val = new ImFloat((Float) value);
            if (ImGui.inputFloat("##" + key, val)) {
                currentTarget.setProperty(key, val.get());
            }
        } else if (value instanceof Integer) {
            ImInt val = new ImInt((Integer) value);
            if (ImGui.inputInt("##" + key, val)) {
                currentTarget.setProperty(key, val.get());
            }
        } else if (value instanceof Boolean) {
            ImBoolean val = new ImBoolean((Boolean) value);
            if (ImGui.checkbox("##" + key, val)) {
                currentTarget.setProperty(key, val.get());
            }
        } else if (value instanceof String) {
            ImString val = new ImString((String) value, 256);
            if (ImGui.inputText("##" + key, val)) {
                currentTarget.setProperty(key, val.get());
            }
        } else {
            ImGui.textDisabled(value.toString());
        }
    }
}