package de.luckymcdev.foundryengine.mixin.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeLevel;
import de.luckymcdev.foundryengine.interfaces.EngineLevelAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.clock.ServerClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Implements {@link EngineLevelAccess} on ServerLevel to support tick-when-empty and per-level clock routing.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements EngineLevelAccess {
    @Unique
    private static final int TICK_TIMEOUT = 20 * 15;

    @Unique
    private boolean engine$tickWhenEmpty = true;
    @Unique
    private int engine$tickTimeout;

    @Shadow
    public abstract List<ServerPlayer> players();

    @Shadow
    public abstract ServerChunkCache getChunkSource();

    /**
     * Sets whether this level should tick even when empty.
     */
    @Override
    public void engine$setTickWhenEmpty(boolean tickWhenEmpty) {
        this.engine$tickWhenEmpty = tickWhenEmpty;
    }

    /**
     * Injects before tick to skip ticking empty levels after a timeout.
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tick(BooleanSupplier haveTime, CallbackInfo ci) {
        boolean shouldTick = this.engine$tickWhenEmpty || !this.isLevelEmpty();
        if (shouldTick) {
            this.engine$tickTimeout = TICK_TIMEOUT;
        } else if (this.engine$tickTimeout-- <= 0) {
            ci.cancel();
        }
    }

    /**
     * Returns whether this level should continue ticking.
     */
    @Override
    public boolean engine$shouldTick() {
        boolean shouldTick = this.engine$tickWhenEmpty || !this.isLevelEmpty();
        return shouldTick || this.engine$tickTimeout > 0;
    }

    @Unique
    private boolean isLevelEmpty() {
        return this.players().isEmpty() && this.getChunkSource().getLoadedChunksCount() <= 0;
    }

    /**
     * Wraps clock manager lookup in tick to use the runtime level's clock for RuntimeLevel instances.
     */
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
    private ServerClockManager onTickMoveToTimeMarkerWakeUp(MinecraftServer instance, Operation<ServerClockManager> original) {
        if ((Object) this instanceof RuntimeLevel level) {
            return level.clockManager();
        } else {
            return original.call(instance);
        }
    }
}
