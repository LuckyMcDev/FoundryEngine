package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.luckymcdev.foundryengine.common.world.entity.EntitySpawner;
import de.luckymcdev.foundryengine.common.world.level.EngineLevels;
import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeLevelConfig;
import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeLevelHandle;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;

public class TestCommand implements EngineCommand {
	private final HashMap<Identifier, RuntimeLevelHandle> worlds = new HashMap<>();

	private LiteralArgumentBuilder<CommandSourceStack> worldSection() {
		var world = Commands.literal("world");

		world.then(Commands.literal("open")
			.then(Commands.argument("name", IdentifierArgument.id())
				.then(Commands.argument("temp", BoolArgumentType.bool())
					.executes(ctx -> {
						var source = ctx.getSource();
						try {
							boolean temp = BoolArgumentType.getBool(ctx, "temp");
							long[] ref = {System.currentTimeMillis()};

							var id = IdentifierArgument.getId(ctx, "name");
							var server = source.getServer();
							var config = new RuntimeLevelConfig()
								.setGenerator(server.overworld().getChunkSource().getGenerator())
								.setGameRule(GameRules.BLOCK_DROPS, false)
								.setSeed(id.hashCode());

							RuntimeLevelHandle handle;
							if (temp) {
								handle = EngineLevels.get(server).openTemporaryLevel(id, config);
							} else {
								handle = EngineLevels.get(server).getOrOpenPersistentLevel(id, config);
							}

							source.sendSuccess(
								() -> Component.literal("LevelCreate: " + (System.currentTimeMillis() - ref[0])),
								false);

							worlds.put(id, handle);

							ref[0] = System.currentTimeMillis();
							if (source.getEntity() != null) {
								source.getEntity().teleport(
									new TeleportTransition(
										handle.asLevel(),
										new Vec3(0, 100, 0),
										Vec3.ZERO,
										0, 0,
										TeleportTransition.DO_NOTHING));
							}

							source.sendSuccess(
								() -> Component.literal("Teleport: " + (System.currentTimeMillis() - ref[0])),
								false);

							return 1;
						} catch (Throwable e) {
							source.sendFailure(Component.literal("Failed to open world"));
							return 0;
						}
					})
				)
			)
		);

		world.then(Commands.literal("delete")
			.then(Commands.argument("name", IdentifierArgument.id())
				.executes(ctx -> {
					var source = ctx.getSource();
					try {
						var id = IdentifierArgument.getId(ctx, "name");
						var handle = worlds.get(id);
						if (handle == null) {
							source.sendFailure(Component.literal("This world does not exist"));
							return 0;
						}
						handle.delete();
						worlds.remove(id);
						source.sendSuccess(
							() -> Component.literal("World \"" + id + "\" deleted"),
							true);
					} catch (Throwable e) {
						source.sendFailure(Component.literal("Failed to delete world"));
					}
					return 1;
				})
			)
		);

		world.then(Commands.literal("unload")
			.then(Commands.argument("name", IdentifierArgument.id())
				.executes(ctx -> {
					var source = ctx.getSource();
					try {
						var id = IdentifierArgument.getId(ctx, "name");
						RuntimeLevelHandle handle = worlds.get(id);
						if (handle == null) {
							source.sendFailure(Component.literal("This world does not exist"));
							return 0;
						}
						handle.unload();
						worlds.remove(id);
						source.sendSuccess(
							() -> Component.literal("World \"" + id + "\" unloaded"),
							true);
					} catch (Throwable e) {
						source.sendFailure(Component.literal("Failed to unload world"));
					}
					return 1;
				})
			)
		);

		return world;
	}

	private LiteralArgumentBuilder<CommandSourceStack> fakeSection() {
		var fake = Commands.literal("fake");

		fake.then(Commands.literal("spawn")
			.executes(context -> {
				Mannequin test = EntitySpawner.spawnServer(context.getSource().getLevel(), EntityType.MANNEQUIN, new Vec3(0, 100, 0));
				test.setCustomName(Component.literal("supertest"));
				return 1;
			})
		);

		return fake;
	}

	@Override
	public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
		return Commands.literal("test").requires(this::isAdmin)
			.then(worldSection())
			.then(fakeSection());
	}
}
