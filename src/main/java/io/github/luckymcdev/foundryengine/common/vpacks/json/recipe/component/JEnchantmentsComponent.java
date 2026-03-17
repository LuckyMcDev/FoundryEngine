package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;

public class JEnchantmentsComponent extends AbstractJComponent {
    private final HashMap<String, Integer> enchantments = new HashMap<>();

    public JEnchantmentsComponent enchantment(String name, int level) {
        this.enchantments.put(name, level);
        return this;
    }

    public static class Serializer implements JsonSerializer<JEnchantmentsComponent> {
        @Override
        public JsonElement serialize(JEnchantmentsComponent src, Type type, JsonSerializationContext context) {
            return context.serialize(src.enchantments);
        }
    }
}
