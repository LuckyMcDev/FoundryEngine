package de.luckymcdev.foundryengine.common.dialogue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.List;

/**
 * A selectable option within a {@link DialogueNode}. Points to a target node,
 * with optional condition and action bindings.
 */
public class DialogueOption {
	private final String id;
	private final String text;
	private final List<String> conditionIds;
	private final List<String> actionIds;
	private String targetNodeId;

	public DialogueOption(String id, String text, String targetNodeId) {
		this.id = id;
		this.text = text;
		this.targetNodeId = targetNodeId;
		this.conditionIds = new ArrayList<>();
		this.actionIds = new ArrayList<>();
	}

	public DialogueOption(String id, String text, String targetNodeId, List<String> conditionIds, List<String> actionIds) {
		this.id = id;
		this.text = text;
		this.targetNodeId = targetNodeId;
		this.conditionIds = new ArrayList<>(conditionIds);
		this.actionIds = new ArrayList<>(actionIds);
	}

	public static DialogueOption fromNbt(CompoundTag tag) {
		var opt = new DialogueOption(
			tag.getStringOr("Id", ""),
			tag.getStringOr("Text", ""),
			tag.getStringOr("TargetNodeId", "")
		);
		var conds = tag.getListOrEmpty("Conditions");
		for (int i = 0; i < conds.size(); i++) {
			conds.getString(i).ifPresent(opt.conditionIds::add);
		}
		var acts = tag.getListOrEmpty("Actions");
		for (int i = 0; i < acts.size(); i++) {
			acts.getString(i).ifPresent(opt.actionIds::add);
		}
		return opt;
	}

	public CompoundTag toNbt() {
		var tag = new CompoundTag();
		tag.putString("Id", id);
		tag.putString("Text", text);
		tag.putString("TargetNodeId", targetNodeId);
		var conds = new ListTag();
		for (var c : conditionIds) {
			conds.add(StringTag.valueOf(c));
		}
		tag.put("Conditions", conds);
		var acts = new ListTag();
		for (var a : actionIds) {
			acts.add(StringTag.valueOf(a));
		}
		tag.put("Actions", acts);
		return tag;
	}

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public String getTargetNodeId() {
		return targetNodeId;
	}

	public void setTargetNodeId(String targetNodeId) {
		this.targetNodeId = targetNodeId;
	}

	public List<String> getConditionIds() {
		return conditionIds;
	}

	public List<String> getActionIds() {
		return actionIds;
	}
}
