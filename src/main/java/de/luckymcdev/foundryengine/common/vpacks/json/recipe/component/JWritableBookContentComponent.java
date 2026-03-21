package de.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.component.WritableBookContent;

import java.lang.reflect.Type;

public class JWritableBookContentComponent extends JListComponent<Filterable<String>> {
    public static class Serializer implements JsonSerializer<JWritableBookContentComponent> {
        @Override
        public JsonElement serialize(JWritableBookContentComponent src, Type type, JsonSerializationContext context) {
            return context.serialize(new JCodecComponent<>(new WritableBookContent(src.list), WritableBookContent.CODEC));
        }
    }
}
