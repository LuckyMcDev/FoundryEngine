package de.luckymcdev.foundryengine.area.common.module;

import de.luckymcdev.foundryengine.area.common.Area;
import net.minecraft.server.level.ServerPlayer;

public interface AreaLeaveModule extends AreaModule {
	void onLeave(ServerPlayer player, Area area);
}
