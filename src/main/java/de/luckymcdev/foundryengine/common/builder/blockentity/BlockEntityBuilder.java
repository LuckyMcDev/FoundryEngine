package de.luckymcdev.foundryengine.common.builder.blockentity;

import de.luckymcdev.foundryengine.common.builder.AbstractBuilder;
import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.common.world.block.entity.EngineBlockEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlockEntityBuilder<T extends BlockEntity> extends AbstractBuilder<BlockEntityType<T>> {

	private final Map<EngineBlockEntity.CallbackType, Object> callbacks = new EnumMap<>(EngineBlockEntity.CallbackType.class);
	private final List<BlockBuilder> blockBuilders = new ArrayList<>();
	private final List<Runnable> blockResolvers = new ArrayList<>();
	private final Set<Block> resolvedBlocks = new HashSet<>();
	private BlockEntityType.BlockEntitySupplier<T> factory;
	private @Nullable Object rendererFactory;
	private boolean onlyOpCanSetNbt;

	@SuppressWarnings("unchecked")
	protected BlockEntityBuilder(Identifier id) {
		super(id);
		this.factory = (pos, state) -> (T) new EngineBlockEntity(this.getOrCreate(), pos, state);
	}

	public static <T extends BlockEntity> BlockEntityBuilder<T> create(Identifier id) {
		return new BlockEntityBuilder<>(id);
	}

	public BlockEntityBuilder<T> factory(BlockEntityType.BlockEntitySupplier<T> factory) {
		this.factory = factory;
		return this;
	}

	public BlockEntityBuilder<T> validBlock(BlockBuilder block) {
		this.blockBuilders.add(block);
		this.blockResolvers.add(() -> resolvedBlocks.add(block.get()));
		return this;
	}

	public BlockEntityBuilder<T> renderer(Object rendererFactory) {
		this.rendererFactory = rendererFactory;
		return this;
	}

	public BlockEntityBuilder<T> tick(EngineBlockEntity.TickCallback cb) {
		callbacks.put(EngineBlockEntity.CallbackType.TICK, cb);
		return this;
	}

	public BlockEntityBuilder<T> onLoad(EngineBlockEntity.LoadCallback cb) {
		callbacks.put(EngineBlockEntity.CallbackType.LOAD, cb);
		return this;
	}

	public BlockEntityBuilder<T> onSave(EngineBlockEntity.SaveCallback cb) {
		callbacks.put(EngineBlockEntity.CallbackType.SAVE, cb);
		return this;
	}

	public BlockEntityBuilder<T> onUpdatePacket(EngineBlockEntity.UpdatePacketCallback cb) {
		callbacks.put(EngineBlockEntity.CallbackType.UPDATE_PACKET, cb);
		return this;
	}

	public BlockEntityBuilder<T> onUpdateTag(EngineBlockEntity.UpdateTagCallback cb) {
		callbacks.put(EngineBlockEntity.CallbackType.UPDATE_TAG, cb);
		return this;
	}

	public BlockEntityBuilder<T> onHandleUpdateTag(EngineBlockEntity.HandleUpdateTagCallback cb) {
		callbacks.put(EngineBlockEntity.CallbackType.HANDLE_UPDATE_TAG, cb);
		return this;
	}

	public BlockEntityBuilder<T> onDataPacket(EngineBlockEntity.OnDataPacketCallback cb) {
		callbacks.put(EngineBlockEntity.CallbackType.ON_DATA_PACKET, cb);
		return this;
	}

	public BlockEntityBuilder<T> onChunkUnloaded(EngineBlockEntity.OnChunkUnloadedCallback cb) {
		callbacks.put(EngineBlockEntity.CallbackType.ON_CHUNK_UNLOADED, cb);
		return this;
	}

	public BlockEntityBuilder<T> onPreRemove(EngineBlockEntity.PreRemoveCallback cb) {
		callbacks.put(EngineBlockEntity.CallbackType.PRE_REMOVE, cb);
		return this;
	}

	public BlockEntityBuilder<T> isOpOnly(boolean onlyOpCanSetNbt) {
		this.onlyOpCanSetNbt = onlyOpCanSetNbt;
		return this;
	}

	public boolean hasTick() {
		return callbacks.containsKey(EngineBlockEntity.CallbackType.TICK);
	}

	public boolean isOpOnly() {
		return onlyOpCanSetNbt;
	}

	public @Nullable Object getRendererFactory() {
		return rendererFactory;
	}

	public List<BlockBuilder> getBlockBuilders() {
		return blockBuilders;
	}

	public Set<Block> getResolvedBlocks() {
		return resolvedBlocks;
	}

	public void resolveBlocks() {
		resolvedBlocks.clear();
		for (var resolver : blockResolvers) {
			resolver.run();
		}
	}

	@SuppressWarnings("unchecked")
	public BlockEntityType<T> register(RegisterEvent.RegisterHelper<BlockEntityType<?>> helper) {
		BlockEntityType<T> type = build();
		helper.register(id, type);
		setObject(type);
		return type;
	}

	@Override
	@SuppressWarnings("unchecked")
	public BlockEntityType<T> build() {
		resolveBlocks();

		BlockEntityType.BlockEntitySupplier<T> effectiveFactory;
		if (!callbacks.isEmpty()) {
			effectiveFactory = (pos, state) -> {
				T be = factory.create(pos, state);
				if (be instanceof EngineBlockEntity ebe) {
					callbacks.forEach((type, cb) -> {
						switch (type) {
							case TICK -> ebe.setCallback(EngineBlockEntity.CallbackType.TICK, cb);
							case LOAD -> ebe.setCallback(EngineBlockEntity.CallbackType.LOAD, cb);
							case SAVE -> ebe.setCallback(EngineBlockEntity.CallbackType.SAVE, cb);
							case UPDATE_PACKET -> ebe.setCallback(EngineBlockEntity.CallbackType.UPDATE_PACKET, cb);
							case UPDATE_TAG -> ebe.setCallback(EngineBlockEntity.CallbackType.UPDATE_TAG, cb);
							case HANDLE_UPDATE_TAG -> ebe.setCallback(EngineBlockEntity.CallbackType.HANDLE_UPDATE_TAG, cb);
							case ON_DATA_PACKET -> ebe.setCallback(EngineBlockEntity.CallbackType.ON_DATA_PACKET, cb);
							case ON_CHUNK_UNLOADED -> ebe.setCallback(EngineBlockEntity.CallbackType.ON_CHUNK_UNLOADED, cb);
							case PRE_REMOVE -> ebe.setCallback(EngineBlockEntity.CallbackType.PRE_REMOVE, cb);
						}
					});
				}
				return be;
			};
		} else {
			effectiveFactory = factory;
		}

		return new BlockEntityType<>(effectiveFactory, Set.copyOf(resolvedBlocks), onlyOpCanSetNbt);
	}
}
