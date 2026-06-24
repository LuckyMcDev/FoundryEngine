package de.luckymcdev.foundryengine.mixin.command;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ReloadCommand;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Wraps reload resources to add a success feedback message.
 */
@Mixin(ReloadCommand.class)
public class ReloadCommandMixin {
    @Shadow
    @Final
    private static Logger LOGGER;

    /**
     * Wraps reloadResources to send a success message after reload completes.
     */
    @WrapOperation(
            method = "reloadPacks",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;reloadResources(Ljava/util/Collection;)Ljava/util/concurrent/CompletableFuture;")
    )
    private static CompletableFuture<Void> engine$wrapReloadResources(MinecraftServer instance, Collection<String> packs, Operation<CompletableFuture<Void>> original, Collection<String> selectedPacks, CommandSourceStack source) {
        return original.call(instance, packs).thenRun(() -> {
            source.sendSuccess(() -> Component.translatable("fondryengine.commands.reload.success"), false);
        });
    }
}