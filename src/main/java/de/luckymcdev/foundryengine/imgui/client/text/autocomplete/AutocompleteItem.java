package de.luckymcdev.foundryengine.imgui.client.text.autocomplete;

import de.luckymcdev.foundryengine.util.common.color.Color;
import org.jspecify.annotations.Nullable;

/**
 * @param type "keyword", "type", "function", "variable", "constant"
 */
public record AutocompleteItem(String text, String type, @Nullable String signature, Color color) {
}