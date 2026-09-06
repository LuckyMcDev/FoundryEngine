package de.luckymcdev.foundryengine.common.dialogue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A named collection of {@link DialogueNode}s with a designated root node.
 * Serialized to/from NBT for persistence and network sync.
 */
public class DialogueTree {
	private final Identifier id;
	private final Map<String, DialogueNode> nodes;
	private String rootNodeId;
	private DialogueStyle style = new DialogueStyle();

	public DialogueTree(Identifier id, String rootNodeId) {
		this.id = id;
		this.rootNodeId = rootNodeId;
		this.nodes = new LinkedHashMap<>();
	}

	public static DialogueTree fromNbt(Identifier id, CompoundTag tag) {
		var tree = new DialogueTree(id, tag.getStringOr("RootNodeId", ""));
		var nodeList = tag.getListOrEmpty("Nodes");
		for (int i = 0; i < nodeList.size(); i++) {
			var node = DialogueNode.fromNbt(nodeList.getCompoundOrEmpty(i));
			tree.nodes.put(node.getId(), node);
		}
		if (tag.contains("Style")) {
			tree.style = DialogueStyle.fromNbt(tag.getCompoundOrEmpty("Style"));
		}
		return tree;
	}

	public CompoundTag toNbt() {
		var tag = new CompoundTag();
		tag.putString("RootNodeId", rootNodeId);
		var nodeList = new ListTag();
		for (var node : nodes.values()) {
			nodeList.add(node.toNbt());
		}
		tag.put("Nodes", nodeList);
		tag.put("Style", style.toNbt());
		return tag;
	}

	public Identifier getId() {
		return id;
	}

	public String getRootNodeId() {
		return rootNodeId;
	}

	public void setRootNodeId(String rootNodeId) {
		this.rootNodeId = rootNodeId;
	}

	public Map<String, DialogueNode> getNodes() {
		return nodes;
	}

	public DialogueStyle getStyle() {
		return style;
	}

	public void setStyle(DialogueStyle style) {
		this.style = style;
	}

	public DialogueNode getNode(String nodeId) {
		return nodes.get(nodeId);
	}

	public DialogueNode addNode(DialogueNode node) {
		nodes.put(node.getId(), node);
		return node;
	}

	public void removeNode(String nodeId) {
		nodes.remove(nodeId);
	}
}
