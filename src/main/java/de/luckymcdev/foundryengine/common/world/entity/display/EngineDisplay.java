package de.luckymcdev.foundryengine.common.world.entity.display;

import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Targeting;
import org.jspecify.annotations.Nullable;

public interface EngineDisplay extends Attackable, Targeting {

    boolean isPickable();

    void setPickable(boolean pickable);

    @Nullable
    String getInteractionCommand();

    void setInteractionCommand(@Nullable String command);

    @Nullable
    String getOffhandInteractionCommand();

    void setOffhandInteractionCommand(@Nullable String command);

    @Nullable
    String getAttackCommand();

    void setAttackCommand(@Nullable String command);

    void setTarget(@Nullable LivingEntity target);
}