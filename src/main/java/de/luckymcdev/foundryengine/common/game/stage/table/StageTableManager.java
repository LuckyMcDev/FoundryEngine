package de.luckymcdev.foundryengine.common.game.stage.table;

import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StageTableManager {
	private final Map<Identifier, StageTable> tables = new HashMap<>();

	public StageTable createTable(Identifier name) {
		var table = new StageTable(name);
		tables.put(name, table);
		return table;
	}

	public StageTable getTable(Identifier name) {
		return tables.get(name);
	}

	public boolean removeTable(Identifier name) {
		return tables.remove(name) != null;
	}

	public Collection<Identifier> getTableNames() {
		return Collections.unmodifiableSet(tables.keySet());
	}

	public Collection<StageTable> getTables() {
		return Collections.unmodifiableCollection(tables.values());
	}

	public void clear() {
		tables.clear();
	}

	public int size() {
		return tables.size();
	}
}
