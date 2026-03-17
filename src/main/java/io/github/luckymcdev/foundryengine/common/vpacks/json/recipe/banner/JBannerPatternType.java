package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.banner;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class JBannerPatternType {
    private String string;
    private JBannerPatternObject object;

    public JBannerPatternType string(String string) {
        if (this.object == null) {
            this.string = string;
        } else {
            throw new IllegalStateException("Cannot have both object and string defined for a pattern type.");
        }
        return this;
    }

    public JBannerPatternType object(JBannerPatternObject object) {
        if (this.string == null) {
            this.object = object;
        } else {
            throw new IllegalStateException("Cannot have both object and string defined for a pattern type.");
        }
        return this;
    }

    public static class Serializer implements JsonSerializer<JBannerPatternType> {
        @Override
        public JsonElement serialize(JBannerPatternType src, Type type, JsonSerializationContext context) {
            if (src.string != null) {
                return context.serialize(src.string);
            } else {
                return context.serialize(src.object);
            }
        }
    }
}
