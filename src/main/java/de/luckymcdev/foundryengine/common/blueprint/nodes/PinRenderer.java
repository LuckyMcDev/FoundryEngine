package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinInfo;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.List;

@FunctionalInterface
public interface PinRenderer {
    static PinRenderer boolPin() {
        return (pin, undo) -> {
            if (!(pin.defaultValue instanceof Boolean b)) return false;
            var ref = new ImBoolean(b);
            ImGui.textUnformatted(pin.pin.label() + " ");
            ImGui.sameLine();
            if (ImGui.checkbox("##dv_" + pin.id, ref)) {
                undo.run();
                pin.defaultValue = ref.get();
            }
            return true;
        };
    }

    // ========== Built-in renderers ==========

    static PinRenderer intPin() {
        return (pin, undo) -> {
            if (!(pin.defaultValue instanceof Integer i)) return false;
            var ref = new ImInt(i);
            ImGui.textUnformatted(pin.pin.label());
            if (ImGui.dragInt("##dv_" + pin.id, ref.getData())) {
                undo.run();
                pin.defaultValue = ref.get();
            }
            return true;
        };
    }

    static PinRenderer floatPin(float dragSpeed) {
        return (pin, undo) -> {
            if (!(pin.defaultValue instanceof Float f)) return false;
            var ref = new ImFloat(f);
            ImGui.textUnformatted(pin.pin.label());
            if (ImGui.dragFloat("##dv_" + pin.id, ref.getData(), dragSpeed)) {
                undo.run();
                pin.defaultValue = ref.get();
            }
            return true;
        };
    }

    static PinRenderer stringPin(int maxLength) {
        return (pin, undo) -> {
            if (!(pin.defaultValue instanceof String s)) return false;
            var ref = new ImString(s, maxLength);
            ImGui.textUnformatted(pin.pin.label());
            if (ImGui.inputText("##dv_" + pin.id, ref)) {
                undo.run();
                pin.defaultValue = ref.get();
            }
            return true;
        };
    }

    static PinRenderer enumPin(List<String> values) {
        return (pin, undo) -> {
            if (!(pin.defaultValue instanceof String s) || values == null || values.isEmpty())
                return false;
            String current = values.contains(s) ? s : values.get(0);
            int idx = values.indexOf(current);
            if (idx < 0) idx = 0;
            var selected = new ImInt(idx);
            ImGui.pushID("##dv_" + pin.id);
            ImGui.textUnformatted(pin.pin.label() + " ");
            ImGui.sameLine();
            if (ImGui.combo("##combo", selected, values.toArray(new String[0]))) {
                undo.run();
                pin.defaultValue = values.get(selected.get());
            }
            ImGui.popID();
            return true;
        };
    }

    // Fallback – handles catalogue drag-drop + label display
    static PinRenderer fallback() {
        return (pin, undo) -> {
            ImGui.textUnformatted(pin.pin.label() + ": " + pin.defaultValue);
            return false;
        };
    }

    /**
     * Render the inline editor widget for a pin's default value.
     *
     * @param pin      the pin whose default value to edit
     * @param pushUndo callback to save undo state before mutation
     * @return true if the pin was handled (caller should skip default rendering)
     */
    boolean render(NodePinInfo pin, Runnable pushUndo);
}
