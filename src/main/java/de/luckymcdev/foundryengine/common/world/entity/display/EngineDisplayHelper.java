package de.luckymcdev.foundryengine.common.world.entity.display;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class EngineDisplayHelper {

    public static InteractionResult interact(
            Entity self,
            @Nullable String interactionCommand,
            @Nullable String offhandInteractionCommand,
            Player player,
            InteractionHand hand
    ) {
        if (self.level().isClientSide()) {
            return (hand == InteractionHand.MAIN_HAND && interactionCommand != null) ||
                    (hand == InteractionHand.OFF_HAND && offhandInteractionCommand != null) ?
                    InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        if (player instanceof ServerPlayer sp) {
            String commandToExecute = null;

            if (hand == InteractionHand.MAIN_HAND && interactionCommand != null && !interactionCommand.isEmpty()) {
                commandToExecute = interactionCommand;
            } else if (hand == InteractionHand.OFF_HAND && offhandInteractionCommand != null && !offhandInteractionCommand.isEmpty()) {
                commandToExecute = offhandInteractionCommand;
            }

            if (commandToExecute != null) {
                CommandSourceStack source = sp.createCommandSourceStack()
                        .withPosition(self.position())
                        .withPermission(sp.permissions());
                self.level().getServer().getCommands().performPrefixedCommand(source, commandToExecute);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    /**
     * @return the attacker as a LivingEntity to store as lastAttacker, or null if no command ran.
     */
    public static @Nullable LivingEntity skipAttackInteraction(
            Entity self,
            @Nullable String attackCommand,
            Entity attacker
    ) {
        if (attacker instanceof ServerPlayer sp && attackCommand != null && !attackCommand.isEmpty()) {
            CommandSourceStack source = sp.createCommandSourceStack()
                    .withPosition(self.position())
                    .withPermission(sp.permissions());
            self.level().getServer().getCommands().performPrefixedCommand(source, attackCommand);
            return sp;
        }
        return null;
    }
}
