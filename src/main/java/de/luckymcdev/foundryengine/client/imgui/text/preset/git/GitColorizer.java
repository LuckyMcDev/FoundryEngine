package de.luckymcdev.foundryengine.client.imgui.text.preset.git;

import de.luckymcdev.foundryengine.client.imgui.text.color.AbstractBaseColorizer;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorGlyph;
import de.luckymcdev.foundryengine.common.util.color.Color;

import java.util.List;
import java.util.regex.Pattern;

public final class GitColorizer extends AbstractBaseColorizer {

	public static final Color COLOR_DEFAULT = Color.ofABGR(0xFFD4D4D4);
	public static final Color COLOR_COMMENT = Color.ofABGR(0xFF6A9955);   // dim green
	public static final Color COLOR_HEADER = Color.ofABGR(0xFF569CD6);   // blue
	public static final Color COLOR_BRANCH = Color.ofABGR(0xFF4EC9B0);   // cyan
	public static final Color COLOR_REMOTE = Color.ofABGR(0xFFCE9178);   // orange
	public static final Color COLOR_COMMIT_HASH = Color.ofABGR(0xFFDCDCAA);   // yellow
	public static final Color COLOR_TAG = Color.ofABGR(0xFFC586C0);   // purple
	public static final Color COLOR_STASH = Color.ofABGR(0xFFD7BA7D);   // gold
	public static final Color COLOR_STATUS_ADDED = Color.ofABGR(0xFF6A9955);   // green
	public static final Color COLOR_STATUS_MOD = Color.ofABGR(0xFFDCDCAA);   // yellow
	public static final Color COLOR_STATUS_DEL = Color.ofABGR(0xFFF44747);   // red
	public static final Color COLOR_STATUS_REN = Color.ofABGR(0xFFC586C0);   // purple
	public static final Color COLOR_STATUS_CP = Color.ofABGR(0xFFC586C0);   // purple
	public static final Color COLOR_STATUS_UNM = Color.ofABGR(0xFF9CDCFE);   // light blue
	public static final Color COLOR_STATUS_IGN = Color.ofABGR(0xFF808080);   // grey
	public static final Color COLOR_NUMBER = Color.ofABGR(0xFFB5CEA8);   // light green
	public static final Color COLOR_PUNCTUATION = Color.ofABGR(0xFF808080);   // grey

	private static final Pattern STATUS_ADDED = Pattern.compile("^\\s*[ADMRCU]\\s+.*");
	private static final Pattern STATUS_MOD = Pattern.compile("^\\s*M\\s+.*");
	private static final Pattern STATUS_DEL = Pattern.compile("^\\s*D\\s+.*");
	private static final Pattern STATUS_REN = Pattern.compile("^\\s*R\\s+.*");
	private static final Pattern STATUS_CP = Pattern.compile("^\\s*C\\s+.*");
	private static final Pattern STATUS_UNM = Pattern.compile("^\\s*U\\s+.*");
	private static final Pattern STATUS_IGN = Pattern.compile("^\\s*!\\s+.*");

	private static final Pattern COMMIT_HASH = Pattern.compile("^\\s*[0-9a-f]{7,40}\\s+.*");
	private static final Pattern CURRENT_BRANCH = Pattern.compile("^\\*\\s+(.*)$");
	private static final Pattern REMOTE_BRANCH = Pattern.compile("^\\s*remotes?/.*");
	private static final Pattern LOCAL_BRANCH = Pattern.compile("^\\s{2}\\S+.*");
	private static final Pattern REMOTE_LIST = Pattern.compile("^\\S+\\s+\\S+\\s+\\((fetch|push)\\)$");
	private static final Pattern STASH_ENTRY = Pattern.compile("^stash@\\{[0-9]+\\}.*");
	private static final Pattern TAG_NAME = Pattern.compile("^[a-zA-Z0-9_.-]+$");
	private static final Pattern LOG_ONELINE = Pattern.compile("^[0-9a-f]{7,40}\\s+.*");
	private static final Pattern LOG_GRAPH = Pattern.compile("^[\\s*|/\\\\-]+.*");
	private static final Pattern LOG_SUMMARY = Pattern.compile(".*\\d+\\s+file[s]?\\s+changed.*");
	private static final Pattern MERGE_IN_PROGRESS = Pattern.compile("^(Merging|Rebasing|Cherry-picking).*");
	private static final Pattern DIFF_HEADER = Pattern.compile("^(diff --git|--- a/|\\+\\+\\+ b/|@@)");

	private static Color pickColor(String line) {
		if (line.isEmpty()) {
			return COLOR_DEFAULT;
		}

		if (line.startsWith("#")) {
			return COLOR_COMMENT;
		}

		if (MERGE_IN_PROGRESS.matcher(line).matches()) {
			return COLOR_HEADER;
		}

		if (DIFF_HEADER.matcher(line).matches()) {
			return COLOR_COMMENT;
		}

		if (STASH_ENTRY.matcher(line).matches()) {
			return COLOR_STASH;
		}

		if (REMOTE_LIST.matcher(line).matches()) {
			return COLOR_REMOTE;
		}

		if (CURRENT_BRANCH.matcher(line).matches()) {
			return COLOR_BRANCH;
		}
		if (REMOTE_BRANCH.matcher(line).matches()) {
			return COLOR_REMOTE;
		}
		if (LOCAL_BRANCH.matcher(line).matches()) {
			return COLOR_BRANCH;
		}

		if (TAG_NAME.matcher(line).matches()) {
			return COLOR_TAG;
		}

		if (line.length() >= 3) {
			char x = line.charAt(0);
			char y = line.charAt(1);
			if (x == '?' && y == '?') {
				return COLOR_STATUS_IGN;
			}
			if (x == 'A' || y == 'A') {
				return COLOR_STATUS_ADDED;
			}
			if (x == 'M' || y == 'M') {
				return COLOR_STATUS_MOD;
			}
			if (x == 'D' || y == 'D') {
				return COLOR_STATUS_DEL;
			}
			if (x == 'R' || y == 'R') {
				return COLOR_STATUS_REN;
			}
			if (x == 'C' || y == 'C') {
				return COLOR_STATUS_CP;
			}
			if (x == 'U' || y == 'U') {
				return COLOR_STATUS_UNM;
			}
		}

		if (COMMIT_HASH.matcher(line).matches()) {
			return COLOR_COMMIT_HASH;
		}

		if (LOG_GRAPH.matcher(line).matches()) {
			return COLOR_COMMIT_HASH;
		}

		if (LOG_SUMMARY.matcher(line).matches()) {
			return COLOR_NUMBER;
		}

		if (line.startsWith("On branch") || line.startsWith("Your branch") ||
			line.startsWith("Changes") || line.startsWith("Untracked files") ||
			line.startsWith("nothing to commit") || line.startsWith("no changes added") ||
			line.startsWith("not currently on any branch") ||
			line.startsWith("HEAD detached at") ||
			line.startsWith("You are in '") ||
			line.startsWith("All conflicts fixed") ||
			line.startsWith("Resolved")) {
			return COLOR_HEADER;
		}

		return COLOR_DEFAULT;
	}

	@Override
	public Color getDefaultColor() {
		return COLOR_DEFAULT;
	}

	@Override
	protected void analyzeDocument(List<List<EditorGlyph>> lines) {
		// No document-wide analysis needed
	}

	@Override
	protected void colorizeLineImpl(List<EditorGlyph> line, int lineIdx, String text) {
		Color color = pickColor(text);
		for (EditorGlyph g : line) {
			g.color = color;
		}
	}
}