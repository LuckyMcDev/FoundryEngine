package de.luckymcdev.foundryengine.client.command.suggest.nbt;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JsonComponentSuggestions {
	private static final List<String> TEXT_COMPONENT_KEYS = List.of(
		"text", "translate", "with", "score", "selector", "keybind", "nbt",
		"extra", "color", "font", "bold", "italic", "underlined", "strikethrough",
		"obfuscated", "insertion", "clickEvent", "hoverEvent"
	);

	private static final List<String> STYLE_KEYS = List.of(
		"color", "font", "bold", "italic", "underlined", "strikethrough",
		"obfuscated", "insertion", "clickEvent", "hoverEvent"
	);

	private static final List<String> COLORS = ChatFormatting.getNames(true, false).stream().toList();

	private static final List<String> FONTS = List.of(
		"minecraft:default", "minecraft:uniform", "minecraft:alt", "minecraft:illageralt"
	);

	public static CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder) {
		String remaining = builder.getRemaining().trim();

		if (remaining.isEmpty() || remaining.equals("{")) {
			for (String key : TEXT_COMPONENT_KEYS) {
				builder.suggest(key + ": ", new NbtSuggestionComponent("text component field"));
			}
			return builder.buildFuture();
		}

		if (remaining.endsWith(":")) {
			String key = remaining.substring(0, remaining.length() - 1).trim();
			suggestValueForKey(builder, key);
			return builder.buildFuture();
		}

		if (remaining.contains(":")) {
			int colonPos = remaining.lastIndexOf(':');
			String key = remaining.substring(0, colonPos).trim();
			String value = remaining.substring(colonPos + 1).trim();

			if (value.isEmpty()) {
				suggestValueForKey(builder, key);
			} else if (key.equals("color") || key.equals("\"color\"")) {
				suggestColor(builder, value);
			} else if (key.equals("font") || key.equals("\"font\"")) {
				suggestFont(builder, value);
			}

			return builder.buildFuture();
		}

		for (String key : TEXT_COMPONENT_KEYS) {
			if (key.toLowerCase().startsWith(remaining.toLowerCase())) {
				builder.suggest(key + ": ", new NbtSuggestionComponent("text component field"));
			}
		}

		return builder.buildFuture();
	}

	public static CompletableFuture<Suggestions> suggestStyle(SuggestionsBuilder builder) {
		String remaining = builder.getRemaining().trim();

		if (remaining.isEmpty() || remaining.equals("{")) {
			for (String key : STYLE_KEYS) {
				builder.suggest(key + ": ", new NbtSuggestionComponent("style field"));
			}
			return builder.buildFuture();
		}

		if (remaining.contains(":")) {
			int colonPos = remaining.lastIndexOf(':');
			String key = remaining.substring(0, colonPos).trim();
			String value = remaining.substring(colonPos + 1).trim();

			if (value.isEmpty()) {
				suggestStyleValueForKey(builder, key);
			} else if (key.equals("color") || key.equals("\"color\"")) {
				suggestColor(builder, value);
			} else if (key.equals("font") || key.equals("\"font\"")) {
				suggestFont(builder, value);
			}

			return builder.buildFuture();
		}

		for (String key : STYLE_KEYS) {
			if (key.toLowerCase().startsWith(remaining.toLowerCase())) {
				builder.suggest(key + ": ", new NbtSuggestionComponent("style field"));
			}
		}

		return builder.buildFuture();
	}

	private static void suggestValueForKey(SuggestionsBuilder builder, String key) {
		String clean = key.replace("\"", "");

		switch (clean) {
			case "color" -> suggestColor(builder, "");
			case "font" -> suggestFont(builder, "");
			case "bold", "italic", "underlined", "strikethrough", "obfuscated" -> builder.suggest("true");
			case "text", "insertion", "translate" -> builder.suggest("\"\"");
			case "selector" -> builder.suggest("\"@p\"");
			case "keybind" -> builder.suggest("\"key.forward\"");
		}
	}

	private static void suggestStyleValueForKey(SuggestionsBuilder builder, String key) {
		String clean = key.replace("\"", "");

		switch (clean) {
			case "color" -> suggestColor(builder, "");
			case "font" -> suggestFont(builder, "");
			case "bold", "italic", "underlined", "strikethrough", "obfuscated" -> builder.suggest("true");
		}
	}

	private static void suggestColor(SuggestionsBuilder builder, String prefix) {
		for (String color : COLORS) {
			if (color.startsWith(prefix)) {
				builder.suggest(color, new NbtSuggestionComponent("color"));
			}
		}
		if ("#".startsWith(prefix)) {
			builder.suggest("#rrggbb");
		}
	}

	private static void suggestFont(SuggestionsBuilder builder, String prefix) {
		for (String font : FONTS) {
			if (font.startsWith(prefix)) {
				builder.suggest(font, new NbtSuggestionComponent("font"));
			}
		}
	}
}