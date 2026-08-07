package de.luckymcdev.foundryengine.client.command.builtin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.util.Util;

public class BrowseCommand implements EngineCommand {
	@Override
	public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext context) {
		return Commands.literal("browse").executes(c -> {
			Util.getPlatform().openPath(Common.DIRECTORY);
			return 1;
		});
	}
}
