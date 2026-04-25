package de.luckymcdev.foundryengine.common.world.entity.display;

import com.mojang.math.Transformation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EngineBlockDisplay extends Display.BlockDisplay implements EngineDisplay {

    private static final String INTERACTION_KEY = "interaction_command";
    private static final String OFFHAND_INTERACTION_KEY = "offhand_interaction_command";
    private static final String ATTACK_KEY = "attack_command";

    @Nullable
    private String interactionCommand;
    @Nullable
    private String offhandInteractionCommand;
    @Nullable
    private String attackCommand;
    @Nullable
    private LivingEntity lastAttacker;
    @Nullable
    private LivingEntity target;
    private boolean pickable = true;

    private DisplayHitboxUtil.HitboxBounds hitboxBounds =
            new DisplayHitboxUtil.HitboxBounds(0f, 0f, 0f, 1f, 1f, 1f);

    public EngineBlockDisplay(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(hitboxBounds.cullingWidth(), hitboxBounds.cullingHeight());
    }

    @Override
    protected AABB makeBoundingBox(Vec3 position) {
        if (hitboxBounds == null) {
            return new AABB(
                    position.x, position.y, position.z,
                    position.x + 1.0, position.y + 1.0, position.z + 1.0
            );
        }
        return new AABB(
                position.x + hitboxBounds.minX(), position.y + hitboxBounds.minY(), position.z + hitboxBounds.minZ(),
                position.x + hitboxBounds.maxX(), position.y + hitboxBounds.maxY(), position.z + hitboxBounds.maxZ()
        );
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (Display.DATA_TRANSLATION_ID.equals(accessor)
                || Display.DATA_SCALE_ID.equals(accessor)
                || Display.DATA_LEFT_ROTATION_ID.equals(accessor)
                || Display.DATA_RIGHT_ROTATION_ID.equals(accessor)) {
            refreshHitbox();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (interactionCommand != null) {
            output.putString(INTERACTION_KEY, interactionCommand);
        }
        if (offhandInteractionCommand != null) {
            output.putString(OFFHAND_INTERACTION_KEY, offhandInteractionCommand);
        }
        if (attackCommand != null) {
            output.putString(ATTACK_KEY, attackCommand);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        if (input.getString(INTERACTION_KEY).isPresent()) {
            interactionCommand = input.getString(INTERACTION_KEY).get();
        }
        if (input.getString(OFFHAND_INTERACTION_KEY).isPresent()) {
            offhandInteractionCommand = input.getString(OFFHAND_INTERACTION_KEY).get();
        }
        if (input.getString(ATTACK_KEY).isPresent()) {
            attackCommand = input.getString(ATTACK_KEY).get();
        }
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        refreshHitbox();
    }

    private void refreshHitbox() {
        SynchedEntityData data = getEntityData();
        hitboxBounds = DisplayHitboxUtil.forBlock(new Transformation(
                data.get(Display.DATA_TRANSLATION_ID),
                data.get(Display.DATA_LEFT_ROTATION_ID),
                data.get(Display.DATA_SCALE_ID),
                data.get(Display.DATA_RIGHT_ROTATION_ID)
        ));
        setWidth(hitboxBounds.cullingWidth());
        setHeight(hitboxBounds.cullingHeight());
        refreshDimensions();
    }

    @Override
    public boolean isPickable() {
        return pickable;
    }

    @Override
    public void setPickable(boolean pickable) {
        this.pickable = pickable;
    }

    @Override
    public @Nullable String getInteractionCommand() {
        return interactionCommand;
    }

    @Override
    public void setInteractionCommand(@Nullable String cmd) {
        this.interactionCommand = cmd;
    }

    @Override
    public @Nullable String getOffhandInteractionCommand() {
        return this.offhandInteractionCommand;
    }

    @Override
    public void setOffhandInteractionCommand(@Nullable String command) {
        this.offhandInteractionCommand = command;
    }

    @Override
    public @Nullable String getAttackCommand() {
        return attackCommand;
    }

    @Override
    public void setAttackCommand(@Nullable String cmd) {
        this.attackCommand = cmd;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        return EngineDisplayHelper.interact(this, interactionCommand, offhandInteractionCommand, player, hand);
    }

    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        LivingEntity attk = EngineDisplayHelper.skipAttackInteraction(this, attackCommand, attacker);
        if (attk != null) {
            lastAttacker = attk;
            return true;
        }
        return false;
    }

    @Override
    public @Nullable LivingEntity getLastAttacker() {
        return lastAttacker;
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return target;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        this.target = target;
    }

    @Override
    public void setTransformation(Transformation t) {
        super.setTransformation(t);
        refreshHitbox();
    }
}