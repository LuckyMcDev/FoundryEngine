package de.luckymcdev.foundryengine.common.blueprint.graph;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.Nullable;

/**
 * The pure data model of a blueprint graph: nodes and pins
 */
public class BlueprintGraph {
    public final Int2ObjectMap<BlueprintNode> nodes = new Int2ObjectLinkedOpenHashMap<>();
    public final Int2ObjectMap<NodePinInfo> pins = new Int2ObjectLinkedOpenHashMap<>();
    public int lastId = 0;

    public int nextId() {
        return ++lastId;
    }

    public void addNode(BlueprintNode node) {
        addNode(node, false);
    }

    /**
     * @param positionAtCursor hint for client editor to place node under cursor;
     *                         ignored server-side.
     */
    public void addNode(BlueprintNode node, boolean positionAtCursor) {
        if (node.id == 0) {
            node.id = nextId();
        } else if (node.id > lastId) {
            lastId = node.id;
        }
        nodes.put(node.id, node);

        for (var pin : node.inputPins) {
            if (pin.id == 0) pin.id = nextId();
            pins.put(pin.id, pin);
        }
        for (var pin : node.outputPins) {
            if (pin.id == 0) pin.id = nextId();
            pins.put(pin.id, pin);
        }
    }

    public void removeNode(BlueprintNode node) {
        for (var pin : pins.values()) {
            for (var oPin : node.outputPins) {
                if (pin.inputLink == oPin) {
                    pin.inputLink = null;
                    pin.inputLinkSelected = false;
                    break;
                }
            }
        }
        nodes.remove(node.id);
        for (var pin : node.inputPins) pins.remove(pin.id);
        for (var pin : node.outputPins) pins.remove(pin.id);
    }

    public void clear() {
        nodes.clear();
        pins.clear();
    }

    /**
     * Returns the input pin whose {@code inputLink} points to {@code outputPin}.
     */
    public @Nullable NodePinInfo getConnectedInputPin(NodePinInfo outputPin) {
        for (var pin : pins.values()) {
            if (pin.inputLink == outputPin) return pin;
        }
        return null;
    }

    /**
     * Recalculates {@link #lastId} after deserialization.
     */
    public void resetLastId() {
        int max = 0;
        for (int id : nodes.keySet()) if (id > max) max = id;
        for (int id : pins.keySet()) if (id > max) max = id;
        lastId = max;
    }
}