package de.luckymcdev.foundryengine.common.area.module;

import de.luckymcdev.foundryengine.common.area.Area;
import net.minecraft.server.level.ServerLevel;

public interface AreaTickModule extends AreaModule {
	void tick(ServerLevel level, Area area);
}
