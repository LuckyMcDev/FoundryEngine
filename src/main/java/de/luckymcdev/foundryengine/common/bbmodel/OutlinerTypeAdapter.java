package de.luckymcdev.foundryengine.common.bbmodel;

import com.google.gson.*;
import de.luckymcdev.foundryengine.common.bbmodel.data.Outliner;
import de.luckymcdev.foundryengine.common.bbmodel.data.OutlinerChild;
import de.luckymcdev.foundryengine.common.bbmodel.data.OutlinerElementRef;
import de.luckymcdev.foundryengine.common.bbmodel.data.OutlinerGroupRef;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

final class OutlinerTypeAdapter implements JsonDeserializer<Outliner> {

    private static String getAsString(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }

    private static Boolean getAsBoolean(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsBoolean() : null;
    }

    private static Float getAsFloat(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsFloat() : null;
    }

    @Override
    public Outliner deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();

        Outliner outliner = new Outliner();
        outliner.setName(getAsString(object, "name"));
        outliner.setOrigin(context.deserialize(object.get("origin"), float[].class));
        outliner.setColor(getAsFloat(object, "color"));
        outliner.setUuid(getAsString(object, "uuid"));
        outliner.setExport(getAsBoolean(object, "export"));
        outliner.setMirrorUv(getAsBoolean(object, "mirror_uv"));
        outliner.setOpen(getAsBoolean(object, "isOpen"));
        outliner.setLocked(getAsBoolean(object, "locked"));
        outliner.setVisibility(getAsBoolean(object, "visibility"));
        outliner.setAutouv(getAsFloat(object, "autouv"));

        List<OutlinerChild> children = new ArrayList<>();
        JsonElement childrenElement = object.get("children");
        if (childrenElement != null && childrenElement.isJsonArray()) {
            for (JsonElement child : childrenElement.getAsJsonArray()) {
                if (child == null || child.isJsonNull()) continue;
                if (child.isJsonPrimitive() && child.getAsJsonPrimitive().isString()) {
                    children.add(new OutlinerElementRef(child.getAsString()));
                } else if (child.isJsonObject()) {
                    Outliner nested = context.deserialize(child, Outliner.class);
                    children.add(new OutlinerGroupRef(nested));
                } else {
                    throw new JsonParseException("Unsupported outliner child type: " + child);
                }
            }
        }
        outliner.setChildren(children);

        return outliner;
    }
}

