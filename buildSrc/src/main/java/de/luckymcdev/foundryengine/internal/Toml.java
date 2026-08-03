package de.luckymcdev.foundryengine.internal;

/**
 * Minimal TOML escaping helpers for generated metadata files.
 */
public final class Toml {

	private Toml() {
	}

	/**
	 * Escapes a value as a TOML basic string, or keeps the single-quoted literal
	 * form when the value spans multiple lines.
	 */
	public static String string(String value) {
		if (value == null) {
			return "\"\"";
		}
		if (value.contains("\n")) {
			// Literal '' '' strings do not allow newlines either; use """..."""
			return "\"\"\"\n" + value + "\"\"\"";
		}
		StringBuilder sb = new StringBuilder("\"");
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '"' -> sb.append("\\\"");
				case '\\' -> sb.append("\\\\");
				case '\b' -> sb.append("\\b");
				case '\t' -> sb.append("\\t");
				case '\n' -> sb.append("\\n");
				case '\f' -> sb.append("\\f");
				case '\r' -> sb.append("\\r");
				default -> {
					if (c < 0x20 || c == 0x7f) {
						sb.append("\\u").append(String.format("%04x", (int) c));
					} else {
						sb.append(c);
					}
				}
			}
		}
		return sb.append('"').toString();
	}
}