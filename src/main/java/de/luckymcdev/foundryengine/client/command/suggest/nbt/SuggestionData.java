package de.luckymcdev.foundryengine.client.command.suggest.nbt;

import java.util.HashMap;
import java.util.Map;

public class SuggestionData {
	private static final Map<String, Entry> dataMap = new HashMap<>();
	public static boolean hasCustomSuggestions = false;

	public static void clear() {
		dataMap.clear();
		hasCustomSuggestions = false;
	}

	public static void store(String text, String subtext, int priority) {
		dataMap.put(text, new Entry(subtext, priority));
		hasCustomSuggestions = true;
	}

	public static String getSubtext(String text) {
		Entry entry = dataMap.get(text);
		return entry != null ? entry.subtext : null;
	}

	public static int getPriority(String text) {
		Entry entry = dataMap.get(text);
		return entry != null ? entry.priority : 0;
	}

	public record Entry(String subtext, int priority) {
		public static final int RECOMMENDED = 100;
		public static final int NORMAL = 0;
		public static final int IRRELEVANT = -1;
	}
}
