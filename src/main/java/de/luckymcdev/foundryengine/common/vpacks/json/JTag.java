package de.luckymcdev.foundryengine.common.vpacks.json;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class JTag {
    private final List<String> values = new ArrayList<>();
    private Boolean replace = false;

    /**
     * Sets "replace: true" in the generated tag
     *
     * @return The current {@link JTag} instance
     */
    public JTag replace() {
        this.replace = true;
        return this;
    }

    /**
     * Adds an entry into the tag.
     *
     * @param entry The entry added to the tag.
     * @return The current {@link JTag} instance
     */
    public JTag add(Identifier entry) {
        this.values.add(entry.toString());
        return this;
    }

    /**
     * Adds an entry into the tag. The added entry is a tag.
     *
     * @param tag The tag added to the tag
     * @return The current {@link JTag} instance
     */
    public JTag addTag(Identifier tag) {
        this.values.add("#" + tag.getNamespace() + ":" + tag.getPath());
        return this;
    }
}
