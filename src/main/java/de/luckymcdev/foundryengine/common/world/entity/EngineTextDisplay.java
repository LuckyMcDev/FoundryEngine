package de.luckymcdev.foundryengine.common.world.entity;

import com.mojang.math.Transformation;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Brightness;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EngineTextDisplay extends Display.TextDisplay {

    @Nullable
    private InteractionConsumer interactionConsumer;
    @Nullable
    private AttackConsumer attackConsumer;

    private DisplayHitboxUtil.HitboxSize hitboxSize = new DisplayHitboxUtil.HitboxSize(1.0f, 1.0f);

    public EngineTextDisplay(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (hitboxSize == null) return EntityDimensions.scalable(1.0f, 1.0f);
        return EntityDimensions.scalable(hitboxSize.width(), hitboxSize.height());
    }

    @Override
    protected AABB makeBoundingBox(Vec3 position) {
        return getDimensions(getPose()).makeBoundingBox(position);
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
            attackConsumer.onAttack(this, player);
            return true;
        }
        return false;
    }

    public void setDisplayText(Component text) {
        setText(text);
    }

    public void setDisplayLineWidth(int width) {
        setLineWidth(width);
    }

    public void setDisplayTextOpacity(byte opacity) {
        setTextOpacity(opacity);
    }

    public void setDisplayBackgroundColor(int argb) {
        setBackgroundColor(argb);
    }

    public void setDisplayFlags(byte flags) {
        setFlags(flags);
    }

    public void setDisplayShadow(boolean shadow) {
        byte current = getCurrentFlags();
        setFlags(shadow ? (byte) (current | FLAG_SHADOW) : (byte) (current & ~FLAG_SHADOW));
    }

    public void setDisplaySeeThrough(boolean seeThrough) {
        byte current = getCurrentFlags();
        setFlags(seeThrough ? (byte) (current | FLAG_SEE_THROUGH) : (byte) (current & ~FLAG_SEE_THROUGH));
    }

    public void setDisplayUseDefaultBackground(boolean useDefault) {
        byte current = getCurrentFlags();
        setFlags(useDefault ? (byte) (current | FLAG_USE_DEFAULT_BACKGROUND) : (byte) (current & ~FLAG_USE_DEFAULT_BACKGROUND));
    }

    public void setDisplayAlignment(Align alignment) {
        byte current = (byte) (getCurrentFlags() & ~FLAG_ALIGN_LEFT & ~FLAG_ALIGN_RIGHT);
        byte flags = switch (alignment) {
            case LEFT -> (byte) (current | FLAG_ALIGN_LEFT);
            case RIGHT -> (byte) (current | FLAG_ALIGN_RIGHT);
            case CENTER -> current;
        };
        setFlags(flags);
    }

    private byte getCurrentFlags() {
        Display.TextDisplay.TextRenderState state = textRenderState();
        return state != null ? state.flags() : 0;
    }

    public void setDisplayTransformation(Transformation transformation) {
        setTransformation(transformation);
        hitboxSize = DisplayHitboxUtil.forText(transformation);
        refreshDimensions();
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

    public void setDisplayBillboardConstraints(BillboardConstraints constraints) {
        setBillboardConstraints(constraints);
    }

    public void setDisplayBrightnessOverride(@Nullable Brightness brightness) {
        setBrightnessOverride(brightness);
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
        InteractionResult onInteract(EngineTextDisplay display, Player player, InteractionHand hand, Vec3 location);
    }

    @FunctionalInterface
    public interface AttackConsumer {
        void onAttack(EngineTextDisplay display, Player player);
    }
}