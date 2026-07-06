package de.luckymcdev.foundryengine.common.dialogue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks an in-progress dialogue for a single player on the server.
 * Serialized to NBT for network sync to the client.
 */
public class DialogueSession {
	private final Identifier treeId;
	private final Deque<String> history;
	private final Map<String, String> variables;
	private String currentNodeId;
	private boolean ended;
	private DialogueDisplayMode displayMode;
	private DialogueStyle style = new DialogueStyle();

	public DialogueSession(Identifier treeId, String currentNodeId) {
		this.treeId = treeId;
		this.currentNodeId = currentNodeId;
		this.ended = false;
		this.displayMode = DialogueDisplayMode.SCREEN;
		this.history = new ArrayDeque<>();
		this.variables = new HashMap<>();
	}

	public static DialogueSession fromNbt(CompoundTag tag) {
		var treeId = Identifier.parse(tag.getStringOr("TreeId", "foundryengine:empty"));
		var session = new DialogueSession(treeId, tag.getStringOr("CurrentNodeId", ""));
		if (tag.getBooleanOr("Ended", false)) {
			session.end();
		}
		session.setDisplayMode(DialogueDisplayMode.fromOrdinal(tag.getIntOr("DisplayMode", 0)));
		if (tag.contains("Style")) {
			session.style = DialogueStyle.fromNbt(tag.getCompoundOrEmpty("Style"));
		}

		var histTag = tag.getListOrEmpty("History");
		for (int i = 0; i < histTag.size(); i++) {
			histTag.getString(i).ifPresent(s -> session.history.addLast(s));
		}

		var varTag = tag.getCompoundOrEmpty("Variables");
		for (var key : varTag.keySet()) {
			varTag.getString(key).ifPresent(v -> session.variables.put(key, v));
		}

		return session;
	}

	public Identifier getTreeId() {
		return treeId;
	}

	public String getCurrentNodeId() {
		return currentNodeId;
	}

	public void setCurrentNodeId(String currentNodeId) {
		this.currentNodeId = currentNodeId;
	}

	public boolean isEnded() {
		return ended;
	}

	public void end() {
		this.ended = true;
	}

	public DialogueDisplayMode getDisplayMode() {
		return displayMode;
	}

	public void setDisplayMode(DialogueDisplayMode displayMode) {
		this.displayMode = displayMode;
	}

	public DialogueStyle getStyle() {
		return style;
	}

	public void setStyle(DialogueStyle style) {
		this.style = style;
	}

	public Deque<String> getHistory() {
		return history;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public String getVariable(String key) {
		return variables.get(key);
	}

	public void setVariable(String key, String value) {
		variables.put(key, value);
	}

	public CompoundTag toNbt() {
		var tag = new CompoundTag();
		tag.putString("TreeId", treeId.toString());
		tag.putString("CurrentNodeId", currentNodeId);
		tag.putBoolean("Ended", ended);
		tag.putInt("DisplayMode", displayMode.ordinal());
		tag.put("Style", style.toNbt());

		var histTag = new ListTag();
		for (var h : history) {
			histTag.add(StringTag.valueOf(h));
		}
		tag.put("History", histTag);

		var varTag = new CompoundTag();
		for (var e : variables.entrySet()) {
			varTag.putString(e.getKey(), e.getValue());
		}
		tag.put("Variables", varTag);

		return tag;
	}
}
