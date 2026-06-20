package de.luckymcdev.foundryengine.common.area.module;

import de.luckymcdev.foundryengine.common.area.Area;
import net.minecraft.server.level.ServerPlayer;

public interface AreaEnterModule extends AreaModule {
    void onEnter(ServerPlayer player, Area area);
}
