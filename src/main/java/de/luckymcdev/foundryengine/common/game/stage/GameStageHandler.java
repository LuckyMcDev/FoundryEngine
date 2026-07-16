package de.luckymcdev.foundryengine.common.game.stage;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.game.stage.addon.builtin.BlockStages;
import de.luckymcdev.foundryengine.common.game.stage.addon.builtin.DimensionStages;
import de.luckymcdev.foundryengine.common.game.stage.addon.builtin.ItemStages;
import de.luckymcdev.foundryengine.common.game.stage.addon.builtin.LootStages;
import de.luckymcdev.foundryengine.common.game.stage.addon.builtin.MobStages;
import de.luckymcdev.foundryengine.common.game.stage.addon.builtin.RecipeStages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
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
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class GameStageHandler {
	private final Logger LOGGER = LogUtils.getLogger();
	private final StageRegistry STAGE_REGISTRY = new StageRegistry();
	private final BlockStages BLOCKS = new BlockStages();
	private final DimensionStages DIMENSIONS = new DimensionStages();
	private final ItemStages ITEMS = new ItemStages();
	private final LootStages LOOT = new LootStages();
	private final MobStages MOBS = new MobStages();
	private final RecipeStages RECIPES = new RecipeStages();
	private final List<Map.Entry<StageAdditionCondition, Identifier>> PENDING_STAGES = new ArrayList<>();
	private final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Common.MODID);
	private final Codec<Set<Identifier>> IDENTIFIER_SET_CODEC = Identifier.CODEC.listOf().xmap(
		HashSet::new,
		ArrayList::new
	);
	public final Supplier<AttachmentType<Set<Identifier>>> ATTACHMENT = ATTACHMENT_TYPES.register(
		"player_stages",
		() -> AttachmentType.<Set<Identifier>>builder(() -> new HashSet<>())
			.serialize(IDENTIFIER_SET_CODEC.fieldOf("stages"))
			.copyOnDeath()
			.sync(new AttachmentSyncHandler<Set<Identifier>>() {
				@Override
				public void write(@NonNull RegistryFriendlyByteBuf buf, Set<Identifier> attachment, boolean initialSync) {
					buf.writeCollection(attachment, FriendlyByteBuf::writeIdentifier);
				}

				@Override
				public Set<Identifier> read(@NonNull IAttachmentHolder holder, @NonNull RegistryFriendlyByteBuf buf, Set<Identifier> previousValue) {
					return new HashSet<>(buf.readCollection(HashSet::new, FriendlyByteBuf::readIdentifier));
				}
			})
			.build()
	);

	public void register(IEventBus modEventbus) {
		LOGGER.debug("Registered {} GameStageHandler", Common.MODNAME);
		ATTACHMENT_TYPES.register(modEventbus);
	}

	public StageRegistry getStageRegistry() {
		return STAGE_REGISTRY;
	}

	public boolean addStage(Player player, Identifier stage) {
		if (hasStage(player, stage)) {
			return false;
		}

		var event = new GameStageEvent.Add(player, stage);
		if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
			return false;
		}

		var newStages = new HashSet<>(player.getData(ATTACHMENT));
		if (newStages.add(stage)) {
			player.setData(ATTACHMENT, newStages);
			grantParentStages(player, stage);
			NeoForge.EVENT_BUS.post(new GameStageEvent.Added(player, stage));
			return true;
		}
		return false;
	}

	public boolean removeStage(Player player, Identifier stage) {
		if (!hasStage(player, stage)) {
			return false;
		}

		var event = new GameStageEvent.Remove(player, stage);
		if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
			return false;
		}

		var newStages = new HashSet<>(player.getData(ATTACHMENT));
		if (newStages.remove(stage)) {
			player.setData(ATTACHMENT, newStages);
			NeoForge.EVENT_BUS.post(new GameStageEvent.Removed(player, stage));
			return true;
		}
		return false;
	}

	public int addStages(Player player, Collection<Identifier> stages) {
		var current = player.getData(ATTACHMENT);
		var toAdd = new HashSet<>(stages);
		toAdd.removeAll(current);
		if (toAdd.isEmpty()) {
			return 0;
		}

		for (var it = toAdd.iterator(); it.hasNext(); ) {
			var stage = it.next();
			var event = new GameStageEvent.Add(player, stage);
			if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
				it.remove();
			}
		}

		if (toAdd.isEmpty()) {
			return 0;
		}

		var newStages = new HashSet<>(current);
		int count = 0;
		for (var stage : toAdd) {
			if (newStages.add(stage)) {
				grantParentStages(player, stage);
				count++;
			}
		}
		player.setData(ATTACHMENT, newStages);

		for (var stage : toAdd) {
			if (newStages.contains(stage)) {
				NeoForge.EVENT_BUS.post(new GameStageEvent.Added(player, stage));
			}
		}

		return count;
	}

	public int removeStages(Player player, Collection<Identifier> stages) {
		var current = player.getData(ATTACHMENT);
		var toRemove = new HashSet<>(stages);
		toRemove.retainAll(current);
		if (toRemove.isEmpty()) {
			return 0;
		}

		for (var it = toRemove.iterator(); it.hasNext(); ) {
			var stage = it.next();
			var event = new GameStageEvent.Remove(player, stage);
			if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
				it.remove();
			}
		}

		if (toRemove.isEmpty()) {
			return 0;
		}

		var newStages = new HashSet<>(current);
		newStages.removeAll(toRemove);
		player.setData(ATTACHMENT, newStages);

		for (var stage : toRemove) {
			NeoForge.EVENT_BUS.post(new GameStageEvent.Removed(player, stage));
		}

		return toRemove.size();
	}

	public void clearStages(Player player) {
		var oldStages = player.getData(ATTACHMENT);
		if (oldStages.isEmpty()) {
			return;
		}

		var removed = new HashSet<>(oldStages);
		player.setData(ATTACHMENT, new HashSet<>());

		for (var stage : removed) {
			NeoForge.EVENT_BUS.post(new GameStageEvent.Removed(player, stage));
		}
	}

	public boolean hasStage(Player player, Identifier stage) {
		return player.getData(ATTACHMENT).contains(stage);
	}

	public Set<Identifier> getStages(Player player) {
		return Collections.unmodifiableSet(player.getData(ATTACHMENT));
	}

	public void addStageIf(StageAdditionCondition condition, Identifier stage) {
		PENDING_STAGES.add(new AbstractMap.SimpleEntry<>(condition, stage));
	}

	public void onPlayerTick(ServerTickEvent.Post event) {
		if (PENDING_STAGES.isEmpty()) {
			return;
		}

		MinecraftServer server = event.getServer();
		var iterator = PENDING_STAGES.iterator();
		while (iterator.hasNext()) {
			var entry = iterator.next();
			var condition = entry.getKey();
			var stage = entry.getValue();

			for (var player : server.getPlayerList().getPlayers()) {
				if (!hasStage(player, stage) && condition.test(player)) {
					addStage(player, stage);
				}
			}
			iterator.remove();
		}
	}

	private void grantParentStages(Player player, Identifier stage) {
		if (!STAGE_REGISTRY.hasParents(stage)) {
			return;
		}
		for (var parent : STAGE_REGISTRY.getParents(stage)) {
			if (!hasStage(player, parent)) {
				addStage(player, parent);
			}
		}
	}

	public BlockStages blocks() {
		return BLOCKS;
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
