package de.luckymcdev.foundryengine.common.dialogue;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.packets.dialogue.ClientboundDialoguePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DialogueManager {
	public static final String SAVE_SECTION = "dialogue";
	private final Map<Identifier, DialogueTree> trees = new LinkedHashMap<>();
	private final Map<UUID, DialogueSession> sessions = new HashMap<>();
	private final Map<String, DialogueAction> actions = new LinkedHashMap<>();
	private final Map<String, DialogueCondition> conditions = new LinkedHashMap<>();

	public void registerTree(DialogueTree tree) {
		trees.put(tree.getId(), tree);
	}

	public void unregisterTree(Identifier id) {
		trees.remove(id);
	}

	public DialogueTree getTree(Identifier id) {
		return trees.get(id);
	}

	public Collection<DialogueTree> getTrees() {
		return Collections.unmodifiableCollection(trees.values());
	}

	public void replaceAll(List<DialogueTree> newTrees) {
		trees.clear();
		for (var t : newTrees) {
			trees.put(t.getId(), t);
		}
	}

	public void registerAction(String id, DialogueAction action) {
		actions.put(id, action);
	}

	public void registerCondition(String id, DialogueCondition condition) {
		conditions.put(id, condition);
	}

	public DialogueSession getSession(ServerPlayer player) {
		return sessions.get(player.getUUID());
	}

	public boolean hasActiveSession(ServerPlayer player) {
		var session = sessions.get(player.getUUID());
		return session != null && !session.isEnded();
	}

	public void startDialogue(ServerPlayer player, Identifier treeId) {
		startDialogue(player, treeId, DialogueDisplayMode.SCREEN);
	}

	public void startDialogue(ServerPlayer player, Identifier treeId, DialogueDisplayMode displayMode) {
		if (hasActiveSession(player)) {
			endDialogue(player);
		}

		var tree = trees.get(treeId);
		if (tree == null) {
			Common.LOGGER.warn("Dialogue tree not found: {}", treeId);
			return;
		}

		var node = tree.getNode(tree.getRootNodeId());
		if (node == null) {
			Common.LOGGER.warn("Dialogue tree '{}' has no root node", treeId);
			return;
		}

		var session = new DialogueSession(treeId, node.getId());
		session.setDisplayMode(displayMode);
		session.setStyle(tree.getStyle());
		sessions.put(player.getUUID(), session);
		executeEnterActions(player, session, node);

		NeoForge.EVENT_BUS.post(new DialogueEvent.Started(player, session));

		PacketDistributor.sendToPlayer(player, ClientboundDialoguePacket.show(treeId, session, node));
	}

	public void selectOption(ServerPlayer player, String optionId) {
		var session = sessions.get(player.getUUID());
		if (session == null || session.isEnded()) {
			return;
		}

		var tree = trees.get(session.getTreeId());
		if (tree == null) {
			return;
		}

		var node = tree.getNode(session.getCurrentNodeId());
		if (node == null) {
			return;
		}

		var option = node.getOptions().stream()
			.filter(o -> o.getId().equals(optionId))
			.findFirst()
			.orElse(null);
		if (option == null) {
			return;
		}

		if (!evaluateConditions(player, session, option.getConditionIds())) {
			return;
		}

		NeoForge.EVENT_BUS.post(new DialogueEvent.OptionSelected(player, session, option));
		executeActions(player, session, option.getActionIds());
		advanceTo(player, session, tree, option.getTargetNodeId());
	}

	public void advanceNext(ServerPlayer player) {
		var session = sessions.get(player.getUUID());
		if (session == null || session.isEnded()) {
			return;
		}

		var tree = trees.get(session.getTreeId());
		if (tree == null) {
			return;
		}

		var node = tree.getNode(session.getCurrentNodeId());
		if (node == null || node.getNextNodeId() == null || node.getNextNodeId().isBlank()) {
			endDialogue(player);
			return;
		}
		advanceTo(player, session, tree, node.getNextNodeId());
	}

	public void endDialogue(ServerPlayer player) {
		var session = sessions.remove(player.getUUID());
		if (session == null) {
			return;
		}

		session.end();
		NeoForge.EVENT_BUS.post(new DialogueEvent.Ended(player, session));

		PacketDistributor.sendToPlayer(player, ClientboundDialoguePacket.ended(session.getTreeId()));
	}

	public void onPlayerDisconnect(ServerPlayer player) {
		endDialogue(player);
	}

	public void clearSessions() {
		sessions.clear();
	}

	public void syncToAll() {
		Common.getSavedDataManager().syncToAll();
	}

	public CompoundTag toNbt() {
		var tag = new CompoundTag();
		var list = new ListTag();
		for (var tree : trees.values()) {
			var entry = tree.toNbt();
			entry.putString("Id", tree.getId().toString());
			list.add(entry);
		}
		tag.put("DialogueTrees", list);
		return tag;
	}

	public void applyNbt(CompoundTag tag) {
		var list = tag.getListOrEmpty("DialogueTrees");
		trees.clear();
		for (int i = 0; i < list.size(); i++) {
			var entry = list.getCompoundOrEmpty(i);
			var id = Identifier.parse(entry.getStringOr("Id", "foundryengine:empty"));
			trees.put(id, DialogueTree.fromNbt(id, entry));
		}
	}

	public void save() {
		Common.getSavedDataManager().setSection(SAVE_SECTION, toNbt());
	}

	public void load() {
		applyNbt(Common.getSavedDataManager().getSection(SAVE_SECTION));
	}

	private void advanceTo(ServerPlayer player, DialogueSession session, DialogueTree tree, String targetNodeId) {
		if (targetNodeId == null || targetNodeId.isBlank()) {
			endDialogue(player);
			return;
		}
		var fromNodeId = session.getCurrentNodeId();
		var nextNode = tree.getNode(targetNodeId);
		if (nextNode == null) {
			endDialogue(player);
			return;
		}
		executeEnterActions(player, session, nextNode);
		session.getHistory().push(fromNodeId);
		session.setCurrentNodeId(targetNodeId);
		NeoForge.EVENT_BUS.post(new DialogueEvent.Advanced(player, session, fromNodeId, targetNodeId));

		PacketDistributor.sendToPlayer(player, ClientboundDialoguePacket.advance(tree.getId(), session, nextNode));
	}

	private void executeEnterActions(ServerPlayer player, DialogueSession session, DialogueNode node) {
		if (!evaluateConditions(player, session, node.getConditionIds())) {
			endDialogue(player);
			return;
		}
		executeActions(player, session, node.getEnterActionIds());
	}

	private void executeActions(ServerPlayer player, DialogueSession session, List<String> actionIds) {
		for (var id : actionIds) {
			var action = actions.get(id);
			if (action != null) {
				action.execute(player, session);
			}
		}
	}

	private boolean evaluateConditions(ServerPlayer player, DialogueSession session, List<String> conditionIds) {
		for (var id : conditionIds) {
			var condition = conditions.get(id);
			if (condition != null && !condition.test(player, session)) {
				return false;
			}
		}
		return true;
	}
}
