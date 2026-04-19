package de.luckymcdev.foundryengine.common.world.entity;

import com.mojang.math.Transformation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Brightness;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class EngineBlockDisplay extends Display.BlockDisplay implements Attackable, Targeting {

    @Nullable
    private InteractionConsumer interactionConsumer;
    @Nullable
    private AttackConsumer attackConsumer;
    @Nullable
    private LivingEntity lastAttacker;
    @Nullable
    private LivingEntity target;

    private DisplayHitboxUtil.HitboxBounds hitboxBounds =
            new DisplayHitboxUtil.HitboxBounds(0f, 0f, 0f, 1f, 1f, 1f);
    private boolean pickable = true;

    public EngineBlockDisplay(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (hitboxBounds == null) return EntityDimensions.scalable(1.0f, 1.0f);
        return EntityDimensions.scalable(hitboxBounds.cullingWidth(), hitboxBounds.cullingHeight());
    }

    @Override
    protected AABB makeBoundingBox(Vec3 position) {
        if (hitboxBounds == null) {
            return new AABB(position.x, position.y, position.z,
                    position.x + 1, position.y + 1, position.z + 1);
        }
        return new AABB(
                position.x + hitboxBounds.minX(), position.y + hitboxBounds.minY(), position.z + hitboxBounds.minZ(),
                position.x + hitboxBounds.maxX(), position.y + hitboxBounds.maxY(), position.z + hitboxBounds.maxZ()
        );
    }

    @Override
    public boolean isPickable() {
        return pickable;
    }

    public void setPickable(boolean pickable) {
        this.pickable = pickable;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (Display.DATA_TRANSLATION_ID.equals(accessor)
                || Display.DATA_SCALE_ID.equals(accessor)
                || Display.DATA_LEFT_ROTATION_ID.equals(accessor)
                || Display.DATA_RIGHT_ROTATION_ID.equals(accessor)) {
            refreshHitboxFromTransformation();
        }
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        refreshHitboxFromTransformation();
    }

    private Transformation getCurrentTransformation() {
        SynchedEntityData data = this.getEntityData();
        Vector3fc translation = data.get(Display.DATA_TRANSLATION_ID);
        Quaternionfc leftRotation = data.get(Display.DATA_LEFT_ROTATION_ID);
        Vector3fc scale = data.get(Display.DATA_SCALE_ID);
        Quaternionfc rightRotation = data.get(Display.DATA_RIGHT_ROTATION_ID);
        return new Transformation(translation, leftRotation, scale, rightRotation);
    }

    private void refreshHitboxFromTransformation() {
        this.hitboxBounds = DisplayHitboxUtil.forBlock(getCurrentTransformation());
        setWidth(hitboxBounds.cullingWidth());
        setHeight(hitboxBounds.cullingHeight());
        this.refreshDimensions();
    }

    @Nullable
    public InteractionConsumer getInteractionConsumer() {
        return interactionConsumer;
    }

    public void setInteractionConsumer(@Nullable InteractionConsumer interactionConsumer) {
        this.interactionConsumer = interactionConsumer;
    }

    @Nullable
    public AttackConsumer getAttackConsumer() {
        return attackConsumer;
    }

    public void setAttackConsumer(@Nullable AttackConsumer attackConsumer) {
        this.attackConsumer = attackConsumer;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (level().isClientSide()) {
            return interactionConsumer != null ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (interactionConsumer != null) {
            return interactionConsumer.onInteract(this, player, hand, location);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        if (attacker instanceof Player player && attackConsumer != null) {
            this.lastAttacker = player;
            attackConsumer.onAttack(this, player);
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

    public void setTarget(@Nullable LivingEntity target) {
        this.target = target;
    }

    public void setDisplayBlockState(BlockState state) {
        setBlockState(state);
    }

    public void setDisplayTransformation(Transformation transformation) {
        setTransformation(transformation);
        refreshHitboxFromTransformation();
    }

    public void setDisplayTransformationInterpolationDuration(int ticks) {
        setTransformationInterpolationDuration(ticks);
    }

    public void setDisplayTransformationInterpolationDelay(int ticks) {
        setTransformationInterpolationDelay(ticks);
    }

    public void setDisplayPosRotInterpolationDuration(int ticks) {
        setPosRotInterpolationDuration(ticks);
    }

    public void setDisplayBillboardConstraints(BillboardConstraints c) {
        setBillboardConstraints(c);
    }

    public void setDisplayBrightnessOverride(@Nullable Brightness b) {
        setBrightnessOverride(b);
    }

    public void setDisplayViewRange(float range) {
        setViewRange(range);
    }

    public void setDisplayShadowRadius(float radius) {
        setShadowRadius(radius);
    }

    public void setDisplayShadowStrength(float strength) {
        setShadowStrength(strength);
    }

    public void setDisplayWidth(float width) {
        setWidth(width);
    }

    public void setDisplayHeight(float height) {
        setHeight(height);
    }

    public void setDisplayGlowColorOverride(int argb) {
        setGlowColorOverride(argb);
    }

    @FunctionalInterface
    public interface InteractionConsumer {
        InteractionResult onInteract(EngineBlockDisplay display, Player player, InteractionHand hand, Vec3 location);
    }

    @FunctionalInterface
    public interface AttackConsumer {
        void onAttack(EngineBlockDisplay display, Player player);
    }
}