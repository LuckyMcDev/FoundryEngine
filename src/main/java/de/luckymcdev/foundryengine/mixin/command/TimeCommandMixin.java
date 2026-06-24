package de.luckymcdev.foundryengine.mixin.command;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.world.clock.ServerClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Redirects clock manager lookups in time commands to use the source level's clock.
 */
@Mixin(TimeCommand.class)
public class TimeCommandMixin {
    /**
     * Wraps clockManager() lookup in time commands to use the command source's level clock.
     */
    @WrapOperation(
            method = {"suggestTimeMarkers", "queryTime", "setTotalTicks", "addTime", "setTimeToTimeMarker", "setPaused", "setRate", "queryTimelineTicks", "queryTimelineRepetitions"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;")
    )
    private static ServerClockManager useRuntimeClockManager(MinecraftServer instance, Operation<ServerClockManager> original, @Local(argsOnly = true) CommandSourceStack source) {
        return source.getLevel().clockManager();
    }

}
