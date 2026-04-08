package de.luckymcdev.foundryengine.mixin.command;

import de.luckymcdev.foundryengine.interfaces.EngineReloadCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ReloadCommand;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;

@Mixin(ReloadCommand.class)
public class ReloadCommandMixin implements EngineReloadCommand {
    @Shadow
    @Final
    private static Logger LOGGER;

    /**
     * @author LuckyMcDev
     * @reason To add a "Done!" message to the reload command.
     */
    @Overwrite
    public static void reloadPacks(Collection<String> selectedPacks, CommandSourceStack source) {
        source.getServer().reloadResources(selectedPacks).thenRun(() -> {
            source.sendSuccess(() -> Component.translatable("fondryengine.commands.reload.success"), false);
        }).exceptionally(throwable -> {
            LOGGER.warn("Failed to execute reload", throwable);
            source.sendFailure(Component.translatable("commands.reload.failure"));
            return null;
        });
    }
}