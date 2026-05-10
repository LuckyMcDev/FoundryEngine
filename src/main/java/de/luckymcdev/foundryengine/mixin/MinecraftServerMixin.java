package de.luckymcdev.foundryengine.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow
    @Final
    private Map<ResourceKey<Level>, ServerLevel> levels;
    @Shadow
    @Final
    private ServerClockManager clockManager;

    @Shadow
    @Deprecated
    public abstract GameRules getGlobalGameRules();

    @ModifyExpressionValue(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorldArray()[Lnet/minecraft/server/level/ServerLevel;"))
    private ServerLevel[] engine$copyBeforeTicking(ServerLevel[] original) {
        return original.clone();
    }

    @WrapOperation(method = {"onGameRuleChanged"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void forceGameRunTimeSynchronization(PlayerList instance, Packet<?> packet, Operation<Void> original) {
        for (ServerLevel level : this.levels.values()) {
            if (level.clockManager() == this.clockManager) {
                instance.broadcastAll(packet, level.dimension());
            }
        }
    }
}
