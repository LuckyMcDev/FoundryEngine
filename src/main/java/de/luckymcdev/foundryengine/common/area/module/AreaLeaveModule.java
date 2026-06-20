package de.luckymcdev.foundryengine.common.area.module;

import de.luckymcdev.foundryengine.common.area.Area;
import net.minecraft.server.level.ServerPlayer;

public interface AreaLeaveModule extends AreaModule {
    void onLeave(ServerPlayer player, Area area);
}
