package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.luckymcdev.foundryengine.client.icons.ScreenIconExporter;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.server.command.ModIdArgument;


public class GenerateIconsCommand implements EngineCommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext context) {
        return Commands.literal("generate_icons")
                .executes(ctx -> run(ctx, context, null, false))
                .then(Commands.literal("mod")
                        .then(Commands.argument("mod", ModIdArgument.modIdArgument())
                                .executes(ctx -> run(ctx, context,
                                        StringArgumentType.getString(ctx, "mod"), false))))
                .then(Commands.literal("modRegex")
                        .then(Commands.argument("pattern", StringArgumentType.greedyString())
                                .executes(ctx -> run(ctx, context,
                                        StringArgumentType.getString(ctx, "pattern"), true))));
    }

    private int run(CommandContext<CommandSourceStack> ctx,
                    HolderLookup.Provider lookup,
                    String modIdFilter,
                    boolean regex) {
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        ScreenIconExporter screen = new ScreenIconExporter(lookup, guiScale, modIdFilter, regex);
        Minecraft.getInstance().submitAsync(() -> Minecraft.getInstance().setScreen(screen));
        return 1;
    }
}