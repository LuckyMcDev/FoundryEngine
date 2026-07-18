package de.luckymcdev.foundryengine.client.command.suggest.nbt;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class NbtSuggestionEngine {
	public static CompletableFuture<Suggestions> suggest(String rootType, SuggestionsBuilder builder) {
		SuggestionData.clear();
		String remaining = builder.getRemaining();
		if (remaining.isEmpty()) {
			suggestCompoundStart(builder, rootType);
			return captureData(builder.buildFuture());
		}

		Context ctx = parseContext(remaining);
		if (ctx == null) {
			return Suggestions.empty();
		}

		int absoluteOffset = builder.getStart() + ctx.replaceOffset;
		if (absoluteOffset != builder.getStart()) {
			builder = builder.createOffset(absoluteOffset);
		}

		switch (ctx.phase) {
			case FIELD_NAME -> suggestFieldNames(ctx, builder, rootType);
			case FIELD_VALUE -> suggestFieldValues(ctx, builder, rootType);
			case LIST_ENTRY -> suggestListEntries(ctx, builder, rootType);
			case LIST_END -> builder.suggest("]");
		}

		return captureData(builder.buildFuture());
	}

	private static CompletableFuture<Suggestions> captureData(CompletableFuture<Suggestions> future) {
		Suggestions suggestions = future.join();
		for (Suggestion s : suggestions.getList()) {
			if (s.getTooltip() instanceof NbtSuggestionComponent(String text, int priority)) {
				SuggestionData.store(s.getText(), text, priority);
			}
		}
		return future;
	}

	private static @Nullable Context parseContext(String input) {
		int length = input.length();
		int lastColon = -1;
		int lastCommaOrBrace = -1;
		boolean inString = false;
		char stringChar = '"';
		int braceDepth = 0;
		int bracketDepth = 0;
		List<String> compoundPath = new ArrayList<>();
		ArrayDeque<String> listFieldStack = new ArrayDeque<>();

		for (int i = 0; i < length; i++) {
			char c = input.charAt(i);

			if (inString) {
				if (c == '\\') {
					i++;
					continue;
				}
				if (c == stringChar) {
					inString = false;
				}
				continue;
			}

			if (c == '"' || c == '\'') {
				inString = true;
				stringChar = c;
				continue;
			}

			if (c == '{') {
				braceDepth++;
				if (braceDepth > 1) {
					String fn = findFieldBeforeBrace(input, i);
					if (fn != null) {
						compoundPath.add(fn);
					}
				}
			} else if (c == '}') {
				braceDepth--;
				lastCommaOrBrace = i;
				if (!compoundPath.isEmpty()) {
					compoundPath.remove(compoundPath.size() - 1);
				}
			} else if (c == '[') {
				bracketDepth++;
				if (lastColon > lastCommaOrBrace && bracketDepth == 1) {
					String fn = extractFieldName(input, lastColon);
					if (fn != null) {
						listFieldStack.push(fn);
					}
				}
			} else if (c == ']') {
				bracketDepth--;
				lastCommaOrBrace = i;
				if (!listFieldStack.isEmpty()) {
					listFieldStack.pop();
				}
			} else if (c == ':') {
				lastColon = i;
			} else if (c == ',') {
				lastCommaOrBrace = i;
			}
		}

		if (braceDepth < 0 || bracketDepth < 0) {
			return null;
		}

		if (bracketDepth > 0) {
			int lastNonWs = length - 1;
			while (lastNonWs >= 0 && input.charAt(lastNonWs) == ' ') {
				lastNonWs--;
			}
			if (lastNonWs >= 0 && (input.charAt(lastNonWs) == ',' || input.charAt(lastNonWs) == '[')) {
				String listField = listFieldStack.peek();
				return new Context(Phase.LIST_ENTRY, "", listField, length, compoundPath);
			}
		}

		if (input.endsWith(",")) {
			return new Context(Phase.FIELD_NAME, "", null, length, compoundPath);
		}

		if (input.endsWith("}") || input.endsWith("]")) {
			return new Context(Phase.FIELD_NAME, "", null, length, compoundPath);
		}

		if (lastColon > lastCommaOrBrace) {
			String afterColon = input.substring(lastColon + 1);
			String trimmedAfter = afterColon.trim();
			int whitespaceAfterColon = afterColon.length() - trimmedAfter.length();
			int valueOffset = lastColon + 1 + whitespaceAfterColon;
			String fieldName = extractFieldName(input, lastColon);

			if (trimmedAfter.isEmpty()) {
				return new Context(Phase.FIELD_VALUE, "", fieldName, valueOffset, compoundPath);
			}

			if (trimmedAfter.startsWith("{")) {
				String inner = trimmedAfter.substring(1);
				int innerBrace = 0;
				for (int i = 0; i < inner.length(); i++) {
					char c = inner.charAt(i);
					if (c == '{') {
						innerBrace++;
					} else if (c == '}') {
						if (innerBrace == 0) {
							String after = inner.substring(i + 1);
							int closingPos = valueOffset + 1 + i + 1;
							if (after.isEmpty() || after.equals(",")) {
								return new Context(Phase.FIELD_NAME, "", null, closingPos, compoundPath);
							}
							int trailingWsLen = after.length() - after.trim().length();
							int afterTrimmedOffset = closingPos + trailingWsLen;
							return new Context(Phase.FIELD_VALUE, after.trim(), fieldName, afterTrimmedOffset, compoundPath);
						}
						innerBrace--;
					}
				}
				int openPos = valueOffset + 1;
				List<String> nestedPath = new ArrayList<>(compoundPath);
				if (fieldName != null) {
					nestedPath.add(fieldName);
				}
				return new Context(Phase.FIELD_NAME, "", null, openPos, nestedPath);
			}

			if (trimmedAfter.startsWith("[")) {
				String inner = trimmedAfter.substring(1);
				int innerBracket = 0;
				for (int i = 0; i < inner.length(); i++) {
					char c = inner.charAt(i);
					if (c == '[') {
						innerBracket++;
					} else if (c == ']') {
						if (innerBracket == 0) {
							String after = inner.substring(i + 1);
							int closingPos = valueOffset + 1 + i + 1;
							if (after.isEmpty() || after.equals(",")) {
								return new Context(Phase.FIELD_NAME, "", null, closingPos, compoundPath);
							}
							int trailingWsLen = after.length() - after.trim().length();
							int afterTrimmedOffset = closingPos + trailingWsLen;
							return new Context(Phase.FIELD_VALUE, after.trim(), fieldName, afterTrimmedOffset, compoundPath);
						}
						innerBracket--;
					}
				}
				int listOffset = valueOffset + 1;
				return new Context(Phase.LIST_ENTRY, "", fieldName, listOffset, compoundPath);
			}

			return new Context(Phase.FIELD_VALUE, trimmedAfter, fieldName, valueOffset, compoundPath);
		}

		String afterLast = input.substring(lastCommaOrBrace + 1);
		String trimmed = afterLast.trim();
		int leadingWs = afterLast.length() - trimmed.length();
		int fieldOffset = lastCommaOrBrace + 1 + leadingWs;
		if (trimmed.startsWith("{")) {
			trimmed = trimmed.substring(1);
			fieldOffset++;
		} else if (trimmed.startsWith("[")) {
			trimmed = trimmed.substring(1);
			fieldOffset++;
		}

		return new Context(Phase.FIELD_NAME, trimmed, null, fieldOffset, compoundPath);
	}

	@Nullable
	private static String findFieldBeforeBrace(String input, int bracePos) {
		int pos = bracePos - 1;
		while (pos >= 0 && input.charAt(pos) == ' ') {
			pos--;
		}
		if (pos < 1 || input.charAt(pos) != ':') {
			return null;
		}
		return extractFieldName(input, pos);
	}

	private static @Nullable String extractFieldName(String input, int colonPos) {
		int start = colonPos - 1;
		while (start >= 0 && Character.isWhitespace(input.charAt(start))) {
			start--;
		}
		if (start < 0) {
			return null;
		}

		if (input.charAt(start) == '"' || input.charAt(start) == '\'') {
			char quote = input.charAt(start);
			int open = start - 1;
			while (open >= 0 && input.charAt(open) != quote) {
				open--;
			}
			if (open >= 0) {
				return input.substring(open + 1, start);
			}
			return null;
		}

		int end = start + 1;
		while (start >= 0 && (Character.isLetterOrDigit(input.charAt(start)) || input.charAt(start) == '_' || input.charAt(start) == '.')) {
			start--;
		}
		return input.substring(start + 1, end);
	}

	private static void suggestCompoundStart(SuggestionsBuilder builder, String rootType) {
		List<NbtSuggestions.FieldDef> fields = NbtSuggestions.getFieldsForInherited(rootType);
		if (!fields.isEmpty()) {
			builder.suggest("{");
		}
	}

	private static void suggestFieldNames(Context ctx, SuggestionsBuilder builder, String rootType) {
		List<NbtSuggestions.FieldDef> fields = resolveFields(rootType, ctx.compoundPath);
		String prefix = ctx.currentPrefix.trim();

		for (NbtSuggestions.FieldDef field : fields) {
			if (!prefix.isEmpty() && !field.name().toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) {
				continue;
			}

			String text = field.name() + ": ";
			if (field.type() == NbtSuggestions.NbtType.COMPOUND || field.type() == NbtSuggestions.NbtType.LIST) {
				text += field.type() == NbtSuggestions.NbtType.COMPOUND ? "{" : "[";
			}

			int priority = SuggestionData.Entry.NORMAL;
			if (NbtSuggestions.isIrrelevant(field.name())) {
				priority = SuggestionData.Entry.IRRELEVANT;
			} else if (NbtSuggestions.isRecommended(field.name())) {
				priority = SuggestionData.Entry.RECOMMENDED;
			}

			String subtext = field.type().name().toLowerCase(Locale.ROOT);
			if (!field.subtext().isEmpty()) {
				subtext += " " + field.subtext();
			}

			builder.suggest(text, new NbtSuggestionComponent(subtext, priority));
		}
	}

	private static void suggestFieldValues(Context ctx, SuggestionsBuilder builder, String rootType) {
		String fieldName = ctx.fieldName;
		if (fieldName == null) {
			return;
		}

		List<NbtSuggestions.FieldDef> fields = resolveFields(rootType, ctx.compoundPath);
		for (NbtSuggestions.FieldDef field : fields) {
			if (field.name().equals(fieldName)) {
				suggestValueForType(builder, field.type(), field, ctx.currentPrefix.trim());
				return;
			}
		}
	}

	private static void suggestListEntries(Context ctx, SuggestionsBuilder builder, String rootType) {
		String fieldName = ctx.fieldName;
		if (fieldName == null) {
			builder.suggest("{");
			return;
		}

		List<NbtSuggestions.FieldDef> fields = resolveFields(rootType, ctx.compoundPath);
		for (NbtSuggestions.FieldDef field : fields) {
			if (field.name().equals(fieldName)) {
				NbtSuggestions.NbtType elementType = field.elementType();
				if (elementType != null) {
					suggestValueForType(builder, elementType, field, "");
				} else {
					builder.suggest("{");
				}
				return;
			}
		}

		builder.suggest("{");
	}

	private static List<NbtSuggestions.FieldDef> resolveFields(String rootType, List<String> compoundPath) {
		if (compoundPath == null || compoundPath.isEmpty()) {
			return NbtSuggestions.getFieldsForInherited(rootType);
		}

		List<NbtSuggestions.FieldDef> current = NbtSuggestions.getFieldsForInherited(rootType);
		for (String pathField : compoundPath) {
			NbtSuggestions.FieldDef matched = null;
			for (NbtSuggestions.FieldDef f : current) {
				if (f.name().equals(pathField)) {
					matched = f;
					break;
				}
			}
			if (matched == null || matched.children() == null || matched.children().isEmpty()) {
				return NbtSuggestions.getFieldsForInherited(rootType);
			}
			current = matched.children();
		}
		return current;
	}

	private static void suggestValueForType(SuggestionsBuilder builder, NbtSuggestions.NbtType type, @Nullable NbtSuggestions.FieldDef field, String prefix) {
		if (field != null && field.subtype() != NbtSuggestions.Subtype.NONE
			&& (type == NbtSuggestions.NbtType.ENUM || type == NbtSuggestions.NbtType.STRING || type == NbtSuggestions.NbtType.RESOURCE_LOCATION)) {
			List<String> values = field.subtype().getValues();
			if (!values.isEmpty()) {
				String subtext = field.subtype().name().toLowerCase(Locale.ROOT);
				for (String v : values) {
					if (v.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) {
						builder.suggest(v, new NbtSuggestionComponent(subtext, SuggestionData.Entry.NORMAL));
					}
				}
				return;
			}
		}

		switch (type) {
			case BOOLEAN:
				if ("true".startsWith(prefix) || prefix.isEmpty()) {
					builder.suggest("true");
				}
				if ("false".startsWith(prefix) || prefix.isEmpty()) {
					builder.suggest("false");
				}
				break;
			case BYTE:
				if (prefix.isEmpty()) {
					builder.suggest("0b");
					builder.suggest("1b");
				} else if (prefix.matches("-?\\d*")) {
					builder.suggest(prefix + "b");
				}
				break;
			case SHORT:
				if (prefix.isEmpty()) {
					builder.suggest("0s");
					builder.suggest("1s");
				} else if (prefix.matches("-?\\d*")) {
					builder.suggest(prefix + "s");
				}
				break;
			case INT:
				if (prefix.isEmpty()) {
					builder.suggest("0");
					builder.suggest("1");
				} else if (prefix.matches("-?\\d+")) {
					builder.suggest(prefix);
				}
				break;
			case LONG:
				if (prefix.isEmpty()) {
					builder.suggest("0L");
					builder.suggest("1L");
				} else if (prefix.matches("-?\\d*")) {
					builder.suggest(prefix + "L");
				}
				break;
			case FLOAT:
				if (prefix.isEmpty()) {
					builder.suggest("0.0f");
					builder.suggest("1.0f");
				} else if (prefix.matches("-?\\d*\\.?\\d*")) {
					builder.suggest(prefix + "f");
				}
				break;
			case DOUBLE:
				if (prefix.isEmpty()) {
					builder.suggest("0.0");
					builder.suggest("0.0d");
				} else if (prefix.matches("-?\\d*\\.?\\d*")) {
					builder.suggest(prefix + "d");
				}
				break;
			case STRING:
				if (!prefix.startsWith("\"")) {
					builder.suggest("\"\"");
				}
				break;
			case UUID:
				if (prefix.isEmpty()) {
					builder.suggest("[I;0,0,0,0]");
				}
				break;
			case COMPOUND:
				builder.suggest("{\n}");
				break;
			case LIST:
				builder.suggest("[\n]");
				break;
			case RESOURCE_LOCATION:
				break;
			case ENUM:
				if (field != null) {
					String enumKey = field.subtext().isBlank() ? field.name() : field.subtext();
					List<String> values = NbtSuggestions.getEnumValues(enumKey);
					if (values != null) {
						for (String v : values) {
							if (v.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) {
								builder.suggest(v, new NbtSuggestionComponent("enum", SuggestionData.Entry.NORMAL));
							}
						}
					}
				}
				break;
		}
	}

	private enum Phase {FIELD_NAME, FIELD_VALUE, LIST_ENTRY, LIST_END}

	private record Context(Phase phase, String currentPrefix, @Nullable String fieldName, int replaceOffset, List<String> compoundPath) {
	}
}
