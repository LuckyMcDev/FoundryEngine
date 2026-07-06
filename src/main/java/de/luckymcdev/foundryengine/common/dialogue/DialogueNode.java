package de.luckymcdev.foundryengine.common.dialogue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.List;

/**
 * A single node in a dialogue tree. Contains speaker text, options, enter actions, and conditions.
 * Supports narrative-only flow via {@link #nextNodeId}.
 */
public class DialogueNode {
	private final String id;
	private final String speaker;
	private final String text;
	private final int speakerColor;
	private final List<DialogueOption> options;
	private final List<String> enterActionIds;
	private final List<String> conditionIds;
	private String nextNodeId;

	public DialogueNode(String id, String speaker, String text) {
		this(id, speaker, text, 0x55FF55);
	}

	public DialogueNode(String id, String speaker, String text, int speakerColor) {
		this.id = id;
		this.speaker = speaker;
		this.text = text;
		this.speakerColor = speakerColor;
		this.nextNodeId = null;
		this.options = new ArrayList<>();
		this.enterActionIds = new ArrayList<>();
		this.conditionIds = new ArrayList<>();
	}

	public static DialogueNode fromNbt(CompoundTag tag) {
		var node = new DialogueNode(
			tag.getStringOr("Id", ""),
			tag.getStringOr("Speaker", ""),
			tag.getStringOr("Text", ""),
			tag.getIntOr("SpeakerColor", 0x55FF55)
		);
		if (tag.contains("NextNodeId")) {
			node.nextNodeId = tag.getStringOr("NextNodeId", null);
		}
		var opts = tag.getListOrEmpty("Options");
		for (int i = 0; i < opts.size(); i++) {
			node.options.add(DialogueOption.fromNbt(opts.getCompoundOrEmpty(i)));
		}
		var acts = tag.getListOrEmpty("EnterActions");
		for (int i = 0; i < acts.size(); i++) {
			acts.getString(i).ifPresent(s -> node.enterActionIds.add(s));
		}
		var conds = tag.getListOrEmpty("Conditions");
		for (int i = 0; i < conds.size(); i++) {
			conds.getString(i).ifPresent(s -> node.conditionIds.add(s));
		}
		return node;
	}

	public CompoundTag toNbt() {
		var tag = new CompoundTag();
		tag.putString("Id", id);
		tag.putString("Speaker", speaker);
		tag.putString("Text", text);
		tag.putInt("SpeakerColor", speakerColor);
		if (nextNodeId != null) {
			tag.putString("NextNodeId", nextNodeId);
		}
		var opts = new ListTag();
		for (var o : options) {
			opts.add(o.toNbt());
		}
		tag.put("Options", opts);
		var acts = new ListTag();
		for (var a : enterActionIds) {
			acts.add(StringTag.valueOf(a));
		}
		tag.put("EnterActions", acts);
		var conds = new ListTag();
		for (var c : conditionIds) {
			conds.add(StringTag.valueOf(c));
		}
		tag.put("Conditions", conds);
		return tag;
	}

	public String getId() {
		return id;
	}

	public String getSpeaker() {
		return speaker;
	}

	public String getText() {
		return text;
	}

	public int getSpeakerColor() {
		return speakerColor;
	}

	public String getNextNodeId() {
		return nextNodeId;
	}

	public void setNextNodeId(String nextNodeId) {
		this.nextNodeId = nextNodeId;
	}

	public List<DialogueOption> getOptions() {
		return options;
	}

	public List<String> getEnterActionIds() {
		return enterActionIds;
	}

	public List<String> getConditionIds() {
		return conditionIds;
	}
}
