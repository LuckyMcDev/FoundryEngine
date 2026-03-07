package io.github.luckymcdev.foundryengine.client.opengl.preprocessing;

import net.minecraft.resources.Identifier;

import java.util.regex.Pattern;

/**
 * A Glsl pre-processor. Can be used to replace Strings in the Shader source code.
 */
public abstract class GLSLPreProcessor {
    /**
     * The unique Identifier.
     */
    protected final Identifier id;
    /**
     * The Pattern.
     */
    protected final Pattern pattern;
    /**
     * The Replacement for the Pattern.
     */
    protected final String replacement;

    /**
     * Instantiates a new Glsl pre-processor.
     *
     * @param id          the id
     * @param pattern     the pattern
     * @param replacement the replacement
     */
    protected GLSLPreProcessor(Identifier id, Pattern pattern, String replacement) {
        this.id = id;
        this.pattern = pattern;
        this.replacement = replacement;
    }

    /**
     * Instantiates a new Glsl pre-processor.
     * without a replacement, You HAVE to Overwrite apply()
     *
     * @param id      the id
     * @param pattern the pattern
     */
    protected GLSLPreProcessor(Identifier id, Pattern pattern) {
        this.id = id;
        this.pattern = pattern;
        this.replacement = "YOU DIDNT OVERRIDE THE apply(String source) METHOD. CURSE YOU!!";
    }

    /**
     * Gets the unique {@link Identifier}
     *
     * @return the id
     */
    public Identifier getId() {
        return id;
    }

    /**
     * What happens when you replace a String.
     *
     * @param source the source
     * @return the string
     */
    public String apply(String source) {
        return pattern.matcher(source).replaceAll(replacement);
    }
}
