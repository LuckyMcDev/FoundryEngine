package de.luckymcdev.foundryengine.common.vpacks.json.state;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JState {
    private final List<JVariant> variants = new ArrayList<>();
    private final List<JMultipart> multiparts = new ArrayList<>();

    public JState() {
    }

    public JState(JVariant... variants) {
        JState state = new JState();
        state.addAll(variants);
    }

    public JState(JMultipart... parts) {
        JState state = new JState();
        state.addAll(parts);
    }

    /**
     * Adds a variant to the blockstate.
     *
     * @param variant The variant added to the blockstate
     * @return The current {@link JState} instance
     * @throws IllegalStateException If multiparts have already been added to the blockstate.
     */
    public JState add(JVariant variant) {
        if (!this.multiparts.isEmpty()) {
            throw new IllegalStateException("BlockStates are not allowed to have both multiparts and variants.");
        }
        this.variants.add(variant);
        return this;
    }

    /**
     * Adds a multipart to the blockstate.
     *
     * @param part The multipart added to the blockstate.
     * @return The current {@link JState} instance
     * @throws IllegalStateException If variants have already been added to the
     *                               blockstate.
     */
    public JState add(JMultipart part) {
        if (!this.variants.isEmpty()) {
            throw new IllegalStateException("BlockStates are not allowed to have both multiparts and variants.");
        }
        this.multiparts.add(part);
        return this;
    }

    /**
     * Adds all variants to the blockstate
     *
     * @param variants The variants added to the blockstate
     * @return The current {@link JState} instance
     * @throws IllegalStateException If multiparts have already been added to
     *                               the blockstate
     */
    public JState addAll(JVariant... variants) {
        if (!this.multiparts.isEmpty()) {
            throw new IllegalStateException("BlockStates are not allowed to have both multiparts and variants.");
        }
        this.variants.addAll(List.of(variants));
        return this;
    }

    /**
     * Adds all multiparts to the blockstate.
     *
     * @param parts The parts added to the blockstate
     * @return The current {@link JState} instance
     * @throws IllegalStateException If variants have already been added to the
     *                               blockstate
     */
    public JState addAll(JMultipart... parts) {
        if (!this.variants.isEmpty()) {
            throw new IllegalStateException("BlockStates are not allowed to have both multiparts and variants.");
        }
        this.multiparts.addAll(List.of(parts));
        return this;
    }

    public JState clone() {
        try {
            return (JState) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    public static class Serializer implements JsonSerializer<JState> {
        @Override
        public JsonElement serialize(JState src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            if (!src.variants.isEmpty()) {
                JsonObject variants = new JsonObject();
                src.variants.forEach(variant -> variants.add(
                        String.join(",", variant.conditions),
                        context.serialize(variant.model)
                ));
                jsonObject.add("variants", variants);

            }
            if (!src.multiparts.isEmpty()) {
                jsonObject.add("multipart", context.serialize(src.multiparts));
            }
            return jsonObject;
        }
    }
}
