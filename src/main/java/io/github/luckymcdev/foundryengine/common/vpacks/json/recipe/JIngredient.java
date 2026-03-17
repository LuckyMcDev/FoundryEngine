package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JIngredient {
    private final List<String> entries = new ArrayList<>();
    private String tag;

    public JIngredient entry(String entry) {
        if (this.tag != null) {
            throw new IllegalStateException("You cannot have both tags and items in an ingredient");
        }
        this.entries.add(entry);
        return this;
    }

    /**
     * Sets the ingredient to uses the provided tag to match items. Prepends the
     * "#" onto the provided string.
     *
     * @param tag The tag used for matching items.
     * @return The current {@link JIngredient} instance
     */
    public JIngredient tag(String tag) {
        if (!this.entries.isEmpty()) {
            throw new IllegalStateException("You cannot have both tags and items in an ingredient.");
        }
        this.tag = "#" + tag;
        return this;
    }

    public JIngredient tag(Identifier tag) {
        return this.tag(tag.toString());
    }

    public static class Serializer implements JsonSerializer<JIngredient> {
        @Override
        public JsonElement serialize(JIngredient src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(Objects.requireNonNullElseGet(src.tag, () -> src.entries));
        }
    }
}
