package de.luckymcdev.foundryengine.common.game.stage.table;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StageEntry {
	private final Identifier stage;
	private final int weight;
	private final List<StageCondition> conditions;

	public StageEntry(Identifier stage, int weight) {
		this.stage = stage;
		this.weight = weight;
		this.conditions = new ArrayList<>();
	}

	public Identifier getStage() {
		return stage;
	}

	public int getWeight() {
		return weight;
	}

	public void addCondition(StageCondition condition) {
		conditions.add(condition);
	}

	public void addStageCondition(Identifier requiredStage) {
		conditions.add(player -> Common.getGameStageHandler().hasStage(player, requiredStage));
	}

	public boolean canPlayerObtain(Player player) {
		if (Common.getGameStageHandler().hasStage(player, stage)) {
			return false;
		}
		for (var condition : conditions) {
			if (!condition.test(player)) {
				return false;
			}
		}
		return true;
	}

	public int getConditionCount() {
		return conditions.size();
	}

	public List<StageCondition> getConditions() {
		return Collections.unmodifiableList(conditions);
	}
}
