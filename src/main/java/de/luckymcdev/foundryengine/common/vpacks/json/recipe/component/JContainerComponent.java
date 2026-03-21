package de.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.JResult;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JContainerComponent extends AbstractJComponent {
    private final List<JSlottedItem> items = new ArrayList<>();

    public static class JSlottedItem {
        private int slot;
        // JResult has the necessary fields to fit this piece, so we'll go ahead and use it.
        private JResult item;

        public JSlottedItem slot(int slot) {
            this.slot = slot;
            return this;
        }

        public JSlottedItem item(JResult item) {
            this.item = item;
            return this;
        }
    }

    public static class Serializer implements JsonSerializer<JContainerComponent> {
        @Override
        public JsonElement serialize(JContainerComponent src, Type type, JsonSerializationContext context) {
            return context.serialize(src.items);
        }
    }
}
