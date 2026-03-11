package io.github.luckymcdev.foundryengine.mixin.command;

import com.mojang.brigadier.context.CommandContext;
import io.github.luckymcdev.foundryengine.interfaces.EngineReloadCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ReloadCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ReloadCommand.class)
public class ReloadCommandMixin implements EngineReloadCommand {

    @Inject(
            method = "lambda$register$2", // This targets the code inside the .executes() block
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/commands/ReloadCommand;reloadPacks(Ljava/util/Collection;Lnet/minecraft/commands/CommandSourceStack;)V",
                    shift = At.Shift.AFTER
            )
    )
    private static void fe$lambda$register$2(CommandContext<CommandSourceStack> context, CallbackInfoReturnable<Integer> cir) {
        CommandSourceStack sourceStack = context.getSource();
        sourceStack.sendSuccess(
                () -> Component.literal("Done!"),
                true
        );
    }
}