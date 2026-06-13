package de.luckymcdev.foundryengine.common.world.entity.display;

import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Targeting;

public interface EngineDisplay extends Attackable, Targeting, InteractionCommandHolder {

    boolean isPickable();

    void setPickable(boolean pickable);
}