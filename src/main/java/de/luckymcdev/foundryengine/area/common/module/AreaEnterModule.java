package de.luckymcdev.foundryengine.area.common.module;

import de.luckymcdev.foundryengine.area.common.Area;
import net.minecraft.server.level.ServerPlayer;

public interface AreaEnterModule extends AreaModule {
	void onEnter(ServerPlayer player, Area area);
}
