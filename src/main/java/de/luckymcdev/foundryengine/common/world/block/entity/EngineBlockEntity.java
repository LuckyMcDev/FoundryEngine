package de.luckymcdev.foundryengine.common.world.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class EngineBlockEntity extends BlockEntity {
	private final Map<CallbackType, Object> callbacks = new EnumMap<>(CallbackType.class);

	public EngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static EngineBlockEntity of(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		return new EngineBlockEntity(type, pos, state);
	}

	@SuppressWarnings("unchecked")
	public <C> void setCallback(CallbackType type, C cb) {
		callbacks.put(type, cb);
	}

	public void tick(Level level, BlockPos pos, BlockState state) {
		TickCallback cb = (TickCallback) callbacks.get(CallbackType.TICK);
		if (cb != null) {
			cb.tick(level, pos, state, this);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		LoadCallback cb = (LoadCallback) callbacks.get(CallbackType.LOAD);
		if (cb != null) {
			cb.handleLoad(input);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		SaveCallback cb = (SaveCallback) callbacks.get(CallbackType.SAVE);
		if (cb != null) {
			cb.handleSave(output);
		}
	}

	@Override
	public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
		UpdatePacketCallback cb = (UpdatePacketCallback) callbacks.get(CallbackType.UPDATE_PACKET);
		if (cb != null) {
			return cb.getUpdatePacket();
		}
		return super.getUpdatePacket();
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		UpdateTagCallback cb = (UpdateTagCallback) callbacks.get(CallbackType.UPDATE_TAG);
		if (cb != null) {
			return cb.getUpdateTag(registries);
		}
		return super.getUpdateTag(registries);
	}

	@Override
	public void handleUpdateTag(ValueInput input) {
		HandleUpdateTagCallback cb = (HandleUpdateTagCallback) callbacks.get(CallbackType.HANDLE_UPDATE_TAG);
		if (cb != null) {
			cb.handleUpdateTag(input);
		} else {
			super.handleUpdateTag(input);
		}
	}

	@Override
	public void onDataPacket(Connection net, ValueInput input) {
		OnDataPacketCallback cb = (OnDataPacketCallback) callbacks.get(CallbackType.ON_DATA_PACKET);
		if (cb != null) {
			cb.onDataPacket(net, input);
		} else {
			super.onDataPacket(net, input);
		}
	}

	@Override
	public void onChunkUnloaded() {
		OnChunkUnloadedCallback cb = (OnChunkUnloadedCallback) callbacks.get(CallbackType.ON_CHUNK_UNLOADED);
		if (cb != null) {
			cb.onChunkUnloaded();
		} else {
			super.onChunkUnloaded();
		}
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		PreRemoveCallback cb = (PreRemoveCallback) callbacks.get(CallbackType.PRE_REMOVE);
		if (cb != null) {
			cb.preRemoveSideEffects(pos, state);
		} else {
			super.preRemoveSideEffects(pos, state);
		}
	}

	public enum CallbackType {
		TICK,
		LOAD,
		SAVE,
		UPDATE_PACKET,
		UPDATE_TAG,
		HANDLE_UPDATE_TAG,
		ON_DATA_PACKET,
		ON_CHUNK_UNLOADED,
		PRE_REMOVE
	}

	@FunctionalInterface
	public interface TickCallback {
		void tick(Level level, BlockPos pos, BlockState state, EngineBlockEntity be);
	}

	@FunctionalInterface
	public interface LoadCallback {
		void handleLoad(ValueInput input);
	}

	@FunctionalInterface
	public interface SaveCallback {
		void handleSave(ValueOutput output);
	}

	@FunctionalInterface
	public interface UpdatePacketCallback {
		@Nullable Packet<ClientGamePacketListener> getUpdatePacket();
	}

	@FunctionalInterface
	public interface UpdateTagCallback {
		CompoundTag getUpdateTag(HolderLookup.Provider registries);
	}

	@FunctionalInterface
	public interface HandleUpdateTagCallback {
		void handleUpdateTag(ValueInput input);
	}

	@FunctionalInterface
	public interface OnDataPacketCallback {
		void onDataPacket(Connection net, ValueInput input);
	}

	@FunctionalInterface
	public interface OnChunkUnloadedCallback {
		void onChunkUnloaded();
	}

	@FunctionalInterface
	public interface PreRemoveCallback {
		void preRemoveSideEffects(BlockPos pos, BlockState state);
	}
}
