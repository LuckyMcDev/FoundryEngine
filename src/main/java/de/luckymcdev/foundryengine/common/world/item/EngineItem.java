package de.luckymcdev.foundryengine.common.world.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class EngineItem extends Item {
    private final Map<CallbackType, Object> callbacks = new EnumMap<>(CallbackType.class);

    public EngineItem(Properties properties) {
        super(properties);
    }

    private void setCallback(CallbackType type, Object callback) {
        callbacks.put(type, callback);
    }

    public void clearCallback(CallbackType type) {
        callbacks.remove(type);
    }

    @SuppressWarnings("unchecked")
    private <T> @Nullable T get(CallbackType type) {
        return (T) callbacks.get(type);
    }

    public EngineItem onUseTick(OnUseTickCallback cb) {
        setCallback(CallbackType.ON_USE_TICK, cb);
        return this;
    }

    public EngineItem useOn(UseOnCallback cb) {
        setCallback(CallbackType.USE_ON, cb);
        return this;
    }

    public EngineItem use(UseCallback cb) {
        setCallback(CallbackType.USE, cb);
        return this;
    }

    public EngineItem finishUsingItem(FinishUsingItemCallback cb) {
        setCallback(CallbackType.FINISH_USING_ITEM, cb);
        return this;
    }

    public EngineItem hurtEnemy(HurtEnemyCallback cb) {
        setCallback(CallbackType.HURT_ENEMY, cb);
        return this;
    }

    public EngineItem postHurtEnemy(PostHurtEnemyCallback cb) {
        setCallback(CallbackType.POST_HURT_ENEMY, cb);
        return this;
    }

    public EngineItem inventoryTick(InventoryTickCallback cb) {
        setCallback(CallbackType.INVENTORY_TICK, cb);
        return this;
    }

    public EngineItem onCraftedPostProcess(OnCraftedPostProcessCallback cb) {
        setCallback(CallbackType.ON_CRAFTED_POST_PROCESS, cb);
        return this;
    }

    public EngineItem releaseUsing(ReleaseUsingCallback cb) {
        setCallback(CallbackType.RELEASE_USING, cb);
        return this;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        OnUseTickCallback cb = get(CallbackType.ON_USE_TICK);
        if (cb != null) cb.run(level, entity, stack, ticksRemaining);
        else super.onUseTick(level, entity, stack, ticksRemaining);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        UseOnCallback cb = get(CallbackType.USE_ON);
        return cb != null ? cb.run(context) : super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        UseCallback cb = get(CallbackType.USE);
        return cb != null ? cb.run(level, player, hand) : super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        FinishUsingItemCallback cb = get(CallbackType.FINISH_USING_ITEM);
        return cb != null ? cb.run(stack, level, entity) : super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity mob, LivingEntity attacker) {
        HurtEnemyCallback cb = get(CallbackType.HURT_ENEMY);
        if (cb != null) cb.run(stack, mob, attacker);
        else super.hurtEnemy(stack, mob, attacker);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity mob, LivingEntity attacker) {
        PostHurtEnemyCallback cb = get(CallbackType.POST_HURT_ENEMY);
        if (cb != null) cb.run(stack, mob, attacker);
        else super.postHurtEnemy(stack, mob, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        InventoryTickCallback cb = get(CallbackType.INVENTORY_TICK);
        if (cb != null) cb.run(stack, level, owner, slot);
        else super.inventoryTick(stack, level, owner, slot);
    }

    @Override
    public void onCraftedPostProcess(ItemStack stack, Level level) {
        OnCraftedPostProcessCallback cb = get(CallbackType.ON_CRAFTED_POST_PROCESS);
        if (cb != null) cb.run(stack, level);
        else super.onCraftedPostProcess(stack, level);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        ReleaseUsingCallback cb = get(CallbackType.RELEASE_USING);
        return cb != null ? cb.run(stack, level, entity, remainingTime) : super.releaseUsing(stack, level, entity, remainingTime);
    }

    public enum CallbackType {
        ON_USE_TICK,
        USE_ON,
        USE,
        FINISH_USING_ITEM,
        HURT_ENEMY,
        POST_HURT_ENEMY,
        INVENTORY_TICK,
        ON_CRAFTED_POST_PROCESS,
        RELEASE_USING
    }

    @FunctionalInterface
    public interface OnUseTickCallback {
        void run(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining);
    }

    @FunctionalInterface
    public interface UseOnCallback {
        InteractionResult run(UseOnContext context);
    }

    @FunctionalInterface
    public interface UseCallback {
        InteractionResult run(Level level, Player player, InteractionHand hand);
    }

    @FunctionalInterface
    public interface FinishUsingItemCallback {
        ItemStack run(ItemStack stack, Level level, LivingEntity entity);
    }

    @FunctionalInterface
    public interface HurtEnemyCallback {
        void run(ItemStack stack, LivingEntity mob, LivingEntity attacker);
    }

    @FunctionalInterface
    public interface PostHurtEnemyCallback {
        void run(ItemStack stack, LivingEntity mob, LivingEntity attacker);
    }

    @FunctionalInterface
    public interface InventoryTickCallback {
        void run(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot);
    }

    @FunctionalInterface
    public interface OnCraftedPostProcessCallback {
        void run(ItemStack stack, Level level);
    }

    @FunctionalInterface
    public interface ReleaseUsingCallback {
        boolean run(ItemStack stack, Level level, LivingEntity entity, int remainingTime);
    }
}