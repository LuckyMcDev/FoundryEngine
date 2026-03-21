package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.md.MdScreen;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class TestCommand implements EngineCommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
        return Commands.literal("test")
                .requires(this::isAdmin)
                .executes(context -> {
                    Minecraft.getInstance().execute(() ->
                            Minecraft.getInstance().setScreen(
                                    new MdScreen(Component.literal("Markdown Screen"), Common.id("md/test.md"))
                            )
                    );
                    return 1;
                });
    }
}