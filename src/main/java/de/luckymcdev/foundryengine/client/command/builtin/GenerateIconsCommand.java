package de.luckymcdev.foundryengine.client.command.builtin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.command.ModIdArgument;

import java.util.List;


public class GenerateIconsCommand implements EngineCommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext context) {
        return Commands.literal("generate_icons")
                .executes(ctx -> run(ctx, context, null, false))
                .then(Commands.literal("hand")
                        .executes(this::runHand))
                .then(Commands.literal("mod")
                        .then(Commands.argument("mod", ModIdArgument.modIdArgument())
                                .executes(ctx -> run(ctx, context,
                                        StringArgumentType.getString(ctx, "mod"), false))))
                .then(Commands.literal("modRegex")
                        .then(Commands.argument("pattern", StringArgumentType.greedyString())
                                .executes(ctx -> run(ctx, context,
                                        StringArgumentType.getString(ctx, "pattern"), true))));
    }

    private int runHand(CommandContext<CommandSourceStack> ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return 0;

        ItemStack stackInHand = mc.player.getMainHandItem();
        if (stackInHand.isEmpty()) {
            mc.player.sendOverlayMessage(Component.literal("§cYou must hold an item!"));
            return 0;
        }

        List<ItemStack> singleItemList = List.of(stackInHand.copy());

        Client.getIconExporterLayer().exportCustomItems(singleItemList);
        return 1;
    }

    private int run(CommandContext<CommandSourceStack> ctx,
                    HolderLookup.Provider lookup,
                    String modIdFilter,
                    boolean regex) {
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        Client.getIconExporterLayer().startExport(lookup, guiScale, modIdFilter, regex);
        return 1;
    }
}
