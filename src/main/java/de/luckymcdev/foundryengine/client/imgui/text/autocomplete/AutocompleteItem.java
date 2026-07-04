package de.luckymcdev.foundryengine.client.imgui.text.autocomplete;

import de.luckymcdev.foundryengine.common.util.color.Color;

/**
 * @param type  "keyword", "type", "function", "variable", "constant"
 */
public record AutocompleteItem(String text, String type, String signature, Color color) {
}