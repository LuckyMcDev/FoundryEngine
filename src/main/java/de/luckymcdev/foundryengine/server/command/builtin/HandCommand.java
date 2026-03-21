package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;

/**
 * Command to supply info about the item you're holding.
 */
public class HandCommand implements EngineCommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
        return Commands.literal("hand")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer player = source.getPlayerOrException();
                    ItemStack stack = player.getMainHandItem();

                    if (stack.isEmpty()) {
                        source.sendFailure(Component.literal("You are not holding anything!"));
                        return 0;
                    }

                    source.sendSuccess(() -> Component.literal("--- Item Information ---").withStyle(ChatFormatting.GOLD), false);

                    String itemId = stack.typeHolder().unwrapKey().map(key -> key.identifier().toString()).orElse("unknown");
                    source.sendSuccess(() -> Component.literal("ID: ").withStyle(ChatFormatting.GRAY).append(Component.literal(itemId).withStyle(ChatFormatting.AQUA)), false);

                    source.sendSuccess(() -> Component.literal("Components:").withStyle(ChatFormatting.GRAY), false);

                    for (TypedDataComponent<?> typedComponent : stack.getComponents()) {
                        String typeId = typedComponent.type().toString();
                        String valueStr = String.valueOf(typedComponent.value());

                        MutableComponent line = Component.literal("  - ")
                                .append(Component.literal(typeId).withStyle(ChatFormatting.WHITE))
                                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                                .append(Component.literal(StringUtil.trimChatMessage(valueStr)).withStyle(ChatFormatting.GREEN));

                        line.withStyle(style -> style
                                .withHoverEvent(new HoverEvent.ShowText(Component.literal(valueStr)))
                                .withClickEvent(new ClickEvent.CopyToClipboard(valueStr))
                        );

                        source.sendSuccess(() -> line, false);
                    }

                    return 1;
                });
    }
}