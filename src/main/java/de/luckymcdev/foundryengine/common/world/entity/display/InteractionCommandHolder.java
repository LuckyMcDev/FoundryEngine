package de.luckymcdev.foundryengine.common.world.entity.display;

import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public interface InteractionCommandHolder {

    @Nullable
    String getInteractionCommand();

    void setInteractionCommand(@Nullable String command);

    @Nullable
    String getOffhandInteractionCommand();

    void setOffhandInteractionCommand(@Nullable String command);

    @Nullable
    String getAttackCommand();

    void setAttackCommand(@Nullable String command);

    @Nullable
    LivingEntity getLastAttacker();

    @Nullable
    LivingEntity getTarget();

    void setTarget(@Nullable LivingEntity target);
}
