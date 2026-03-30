package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class TestCommand implements EngineCommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
        return Commands.literal("test")
                .requires(this::isAdmin)
                .executes(context -> {
                    return 1;
                });
    }
}