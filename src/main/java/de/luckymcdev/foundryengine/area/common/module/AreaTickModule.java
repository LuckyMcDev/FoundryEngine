package de.luckymcdev.foundryengine.area.common.module;

import de.luckymcdev.foundryengine.area.common.Area;
import net.minecraft.server.level.ServerLevel;

public interface AreaTickModule extends AreaModule {
	void tick(ServerLevel level, Area area);
}
