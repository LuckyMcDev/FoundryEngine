package de.luckymcdev.foundryengine.common.graph.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

import java.util.*;

public final class NodeModel {
    private final UUID id;
    private final Identifier typeRef;
    private final float x;
    private final float y;
    private final List<PinModel> pins;
    private final List<PinModel> inputPins;
    private final List<PinModel> outputPins;
    private final Map<UUID, PinModel> pinById;
    private final JsonObject data;

    public NodeModel(UUID id, Identifier typeRef, float x, float y,
                     List<PinModel> pins, JsonObject data) {
        this.id = id;
        this.typeRef = typeRef;
        this.x = x;
        this.y = y;
        var inputList = new ArrayList<PinModel>(pins.size());
        var outputList = new ArrayList<PinModel>(pins.size());
        var byId = new LinkedHashMap<UUID, PinModel>(pins.size());
        for (var pin : pins) {
            byId.put(pin.id(), pin);
            if (pin.direction() == PinDirection.INPUT) {
                inputList.add(pin);
            } else {
                outputList.add(pin);
            }
        }
        this.pins = List.copyOf(pins);
        this.inputPins = List.copyOf(inputList);
        this.outputPins = List.copyOf(outputList);
        this.pinById = Collections.unmodifiableMap(byId);
        this.data = data != null ? data.deepCopy() : new JsonObject();
    }

    public NodeModel(UUID id, Identifier typeRef, float x, float y, List<PinModel> pins) {
        this(id, typeRef, x, y, pins, new JsonObject());
    }

    public UUID id() { return id; }
    public Identifier typeRef() { return typeRef; }
    public float x() { return x; }
    public float y() { return y; }
    public List<PinModel> pins() { return pins; }
    public List<PinModel> inputPins() { return inputPins; }
    public List<PinModel> outputPins() { return outputPins; }
    public PinModel pin(UUID pinId) { return pinById.get(pinId); }
    public JsonObject data() { return data.deepCopy(); }

    public NodeModel withPosition(float x, float y) {
        return new NodeModel(id, typeRef, x, y, pins, data);
    }

    public NodeModel withData(JsonObject newData) {
        return new NodeModel(id, typeRef, x, y, pins, newData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeModel that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "NodeModel[" + id + " type=" + typeRef + "]";
    }
}
