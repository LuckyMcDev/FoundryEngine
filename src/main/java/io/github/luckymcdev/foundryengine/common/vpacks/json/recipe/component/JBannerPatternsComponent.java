package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.banner.JBannerPattern;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JBannerPatternsComponent extends AbstractJComponent {
    private final List<JBannerPattern> patterns = new ArrayList<>();

    public JBannerPatternsComponent pattern(JBannerPattern bannerPattern) {
        this.patterns.add(bannerPattern);
        return this;
    }

    public static class Serializer implements JsonSerializer<JBannerPatternsComponent> {
        @Override
        public JsonElement serialize(JBannerPatternsComponent src, Type type, JsonSerializationContext context) {
            return context.serialize(src.patterns);
        }
    }
}
