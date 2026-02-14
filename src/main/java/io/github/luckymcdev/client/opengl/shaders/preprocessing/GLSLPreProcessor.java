package io.github.luckymcdev.client.opengl.shaders.preprocessing;

import net.minecraft.resources.ResourceLocation;

import java.util.regex.Pattern;

public abstract class GLSLPreProcessor {
    protected final ResourceLocation id;
    protected final Pattern pattern;
    protected final String replacement;

    protected GLSLPreProcessor(ResourceLocation id, Pattern pattern, String replacement) {
        this.id = id;
        this.pattern = pattern;
        this.replacement = replacement;
    }

    protected GLSLPreProcessor(ResourceLocation id, Pattern pattern) {
        this.id = id;
        this.pattern = pattern;
        this.replacement = "YOU DIDNT OVERRIDE THE apply(String source) METHOD. CURSE YOU!!";
    }

    public ResourceLocation getId() {
        return id;
    }

    public String apply(String source) {
        return pattern.matcher(source).replaceAll(replacement);
    }
}
