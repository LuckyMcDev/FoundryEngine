package de.luckymcdev.foundryengine.common.game.stage;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.game.stage.addon.builtin.DimensionStages;
import de.luckymcdev.foundryengine.common.game.stage.addon.builtin.ItemStages;
import de.luckymcdev.foundryengine.common.game.stage.addon.builtin.LootStages;
import de.luckymcdev.foundryengine.common.game.stage.addon.builtin.MobStages;
import de.luckymcdev.foundryengine.common.game.stage.addon.builtin.RecipeStages;
import groovyjarjarantlr4.v4.runtime.misc.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Handles the Attachment and All Game Stages for all Players.
 */
public class GameStageHandler {
	private final Logger LOGGER = LogUtils.getLogger();
	private final DimensionStages DIMENSIONS = new DimensionStages();
	private final ItemStages ITEMS = new ItemStages();
	private final LootStages LOOT = new LootStages();
	private final MobStages MOBS = new MobStages();
	private final RecipeStages RECIPES = new RecipeStages();
	private final List<Pair<StageAdditionCondition, String>> PENDING_STAGES = new ArrayList<>();
	private final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Common.MODID);
	private final Codec<Set<String>> STRING_SET_CODEC = Codec.STRING.listOf().xmap(
		HashSet::new,
		ArrayList::new
	);
	public final Supplier<AttachmentType<Set<String>>> ATTACHMENT = ATTACHMENT_TYPES.register(
		"player_stages",
		() -> AttachmentType.builder(() -> (Set<String>) new HashSet<String>())
			.serialize(STRING_SET_CODEC.fieldOf("stages"))
			.copyOnDeath()
			.sync(new AttachmentSyncHandler<>() {
				@Override
				public void write(@NonNull RegistryFriendlyByteBuf buf, Set<String> attachment, boolean initialSync) {
					buf.writeCollection(attachment, FriendlyByteBuf::writeUtf);
				}

				@Override
				public Set<String> read(@NonNull IAttachmentHolder holder, @NonNull RegistryFriendlyByteBuf buf, @Nullable Set<String> previousValue) {
					return new HashSet<>(buf.readCollection(HashSet::new, FriendlyByteBuf::readUtf));
				}
			})
			.build()
	);

	public void register(IEventBus modEventbus) {
		LOGGER.debug("Registered {} GameStageHandler", Common.MODNAME);
		ATTACHMENT_TYPES.register(modEventbus);
		NeoForge.EVENT_BUS.register(DIMENSIONS);
		NeoForge.EVENT_BUS.register(ITEMS);
		NeoForge.EVENT_BUS.register(LOOT);
		NeoForge.EVENT_BUS.register(MOBS);
		NeoForge.EVENT_BUS.register(RECIPES);
	}

	/**
	 * Adds a stage to the player.
	 *
	 * @return true if the stage was added, false if they already had it.
	 */
	public boolean addStage(Player player, String stage) {
		if (hasStage(player, stage)) {
			return false;
		}
		GameStageEvent.Add event = new GameStageEvent.Add(player, stage);
		if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
			return false;
		}

		Set<String> newStages = new HashSet<>(player.getData(ATTACHMENT));
		if (newStages.add(stage)) {
			player.setData(ATTACHMENT, newStages);

			NeoForge.EVENT_BUS.post(new GameStageEvent.Added(player, stage));
			return true;
		}
		return false;
	}

	/**
	 * Removes a stage from the player.
	 *
	 * @return true if the stage was removed, false if they didn't have it.
	 */
	public boolean removeStage(Player player, String stage) {
		if (!hasStage(player, stage)) {
			return false;
		}

		GameStageEvent.Remove event = new GameStageEvent.Remove(player, stage);
		if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
			return false;
		}

		Set<String> newStages = new HashSet<>(player.getData(ATTACHMENT));
		if (newStages.remove(stage)) {
			player.setData(ATTACHMENT, newStages);

			NeoForge.EVENT_BUS.post(new GameStageEvent.Removed(player, stage));
			return true;
		}
		return false;
	}

	/**
	 * Clears all Stages from the Player
	 */
	public void clearStages(Player player) {
		Set<String> stages = player.getData(ATTACHMENT);
		stages.clear();
		player.setData(ATTACHMENT, stages);
	}

	public boolean hasStage(Player player, String stage) {
		return player.getData(ATTACHMENT).contains(stage);
	}

	public Set<String> getStages(Player player) {
		return Collections.unmodifiableSet(player.getData(ATTACHMENT));
	}

	public void addStageIf(StageAdditionCondition condition, String stage) {
		PENDING_STAGES.add(Pair.of(condition, stage));
	}

	public void onPlayerTick(ServerTickEvent.Post event) {
		MinecraftServer server = event.getServer();
		server.getPlayerList().getPlayers().forEach(serverPlayer -> {
			for (Pair<StageAdditionCondition, String> task : PENDING_STAGES) {
				if (task.getKey().test(serverPlayer)) {
					addStage(serverPlayer, task.getValue());
				}
			}
		});
	}

	public DimensionStages dimensions() {
		return DIMENSIONS;
	}

	public ItemStages item() {
		return ITEMS;
	}

	@ApiStatus.Experimental
	public LootStages loot() {
		return LOOT;
	}

	public MobStages mobs() {
		return MOBS;
	}

	@ApiStatus.Experimental
	public RecipeStages recipes() {
		return RECIPES;
	}
}