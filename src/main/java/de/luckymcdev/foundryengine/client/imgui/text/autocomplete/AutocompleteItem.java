package de.luckymcdev.foundryengine.client.imgui.text.autocomplete;

import de.luckymcdev.foundryengine.common.util.color.Color;
import org.jspecify.annotations.Nullable;

/**
 * @param type "keyword", "type", "function", "variable", "constant"
 */
public record AutocompleteItem(String text, String type, @Nullable String signature, Color color) {
}