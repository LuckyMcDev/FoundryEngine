package de.luckymcdev.foundryengine.common.network.packets.editor;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.cutscene.util.LerpType;
import de.luckymcdev.foundryengine.common.easing.BezierPath;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.function.Consumer;

public record CutscenePacket(CompoundTag nbt) implements AbstractPacket<CutscenePacket> {

	public static final Definition<CutscenePacket> DEFINITION = new Definition<>(
		AbstractPacket.createType(Common.id("cutscene_nbt")),
		PacketBounds.BOTH,
		StreamCodec.composite(AbstractPacket.GENEROUS_NBT_CODEC, CutscenePacket::nbt, CutscenePacket::new),
		CutscenePacket::handleClient,
		CutscenePacket::handleServer
	);
	public static volatile Consumer<CutscenePacket> CLIENT_HANDLER;
	private static boolean clientHandlerWarned;

	public static CutscenePacket addAction(String name) {
		CompoundTag tag = new CompoundTag();
		tag.putString("Action", CutsceneAction.ADD.name());
		tag.putString("Name", name);
		return new CutscenePacket(tag);
	}

	public static CutscenePacket removeAction(String name) {
		CompoundTag tag = new CompoundTag();
		tag.putString("Action", CutsceneAction.REMOVE.name());
		tag.putString("Name", name);
		return new CutscenePacket(tag);
	}

	public static CutscenePacket playAction(String targetPlayer, String name, int length, String lerpType, int holdStart, int holdEnd) {
		CompoundTag tag = new CompoundTag();
		tag.putString("Action", CutsceneAction.PLAY.name());
		tag.putString("TargetPlayer", targetPlayer);
		tag.putString("Name", name);
		tag.putInt("Length", length);
		tag.putString("LerpType", lerpType);
		tag.putInt("HoldStart", holdStart);
		tag.putInt("HoldEnd", holdEnd);
		return new CutscenePacket(tag);
	}

	public static CutscenePacket cancelAction(String targetPlayer) {
		CompoundTag tag = new CompoundTag();
		tag.putString("Action", CutsceneAction.CANCEL.name());
		tag.putString("TargetPlayer", targetPlayer);
		return new CutscenePacket(tag);
	}

	public static CutscenePacket requestSync() {
		CompoundTag tag = new CompoundTag();
		tag.putBoolean("Request", true);
		return new CutscenePacket(tag);
	}

	private static CutscenePacket cancelPacket() {
		CompoundTag tag = new CompoundTag();
		tag.putBoolean("Cancel", true);
		return new CutscenePacket(tag);
	}

	@Override
	public Type<CutscenePacket> getType() {
		return DEFINITION.type();
	}

	@Override
	public PacketBounds getBoundTo() {
		return DEFINITION.bounds();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, CutscenePacket> getCodec() {
		return DEFINITION.codec();
	}

	@Override
	public void handleClient(IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			var handler = CLIENT_HANDLER;
			if (handler == null) {
				if (!clientHandlerWarned) {
					clientHandlerWarned = true;
					Common.LOGGER.warn("CutscenePacket: client handler not initialized; dropping packet");
				}
				return;
			}
			handler.accept(this);
		});
	}

	@Override
	public void handleServer(IPayloadContext ctx) {
		ServerPlayer player = AbstractPacket.serverPlayer(ctx);
		if (player == null || !PermissionChecks.COMMANDS_GAMEMASTER.check(player.permissions())) {
			return;
		}

		ServerLevel level = player.level();
		var dimension = level.dimension();
		var cutsceneManager = Common.getCutsceneManager();

		if (this.nbt.getBooleanOr("Request", false)) {
			cutsceneManager.syncToAll();
			return;
		}

		String actionName = this.nbt.getStringOr("Action", "");
		if (actionName.isEmpty()) {
			var list = new ArrayList<Cutscene>();
			var nbtList = this.nbt.getListOrEmpty("CutsceneList");
			for (int i = 0; i < nbtList.size(); i++) {
				list.add(Cutscene.fromNbt(nbtList.getCompoundOrEmpty(i)));
			}
			cutsceneManager.replaceAll(dimension, list);
			cutsceneManager.save();
			cutsceneManager.syncToAll();
			return;
		}
		CutsceneAction action = CutsceneAction.fromString(actionName);
		if (action == null) {
			Common.LOGGER.warn("CutscenePacket: unknown action '{}' from player {}; skipping packet",
				actionName, player.getName().getString());
			return;
		}
		switch (action) {
			case ADD -> {
				String name = this.nbt.getStringOr("Name", "");
				if (name.isBlank()) {
					return;
				}
				BezierPath path = new BezierPath(player.getEyePosition());
				Vec2 rot = new Vec2(player.getXRot(), player.getYRot());
				boolean added = cutsceneManager.add(dimension, new Cutscene(name, rot, rot, path));
				if (added) {
					cutsceneManager.syncToAll();
				}
			}
			case REMOVE -> {
				String name = this.nbt.getStringOr("Name", "");
				if (name.isBlank()) {
					return;
				}
				boolean removed = cutsceneManager.remove(dimension, name);
				if (removed) {
					cutsceneManager.syncToAll();
				}
			}
			case PLAY -> {
				String targetName = this.nbt.getStringOr("TargetPlayer", "");
				String cutsceneName = this.nbt.getStringOr("Name", "");
				if (targetName.isBlank() || cutsceneName.isBlank()) {
					return;
				}
				ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
				if (target == null) {
					return;
				}
				Cutscene cutscene = cutsceneManager.find(level.dimension(), cutsceneName);
				if (cutscene == null) {
					return;
				}

				int length = this.nbt.getIntOr("Length", 0);
				int holdStart = this.nbt.getIntOr("HoldStart", 0);
				int holdEnd = this.nbt.getIntOr("HoldEnd", 0);
				LerpType easing = LerpType.fromString(this.nbt.getStringOr("LerpType", LerpType.LINEAR.name()));

				CompoundTag playTag = new CompoundTag();
				playTag.putString("PlayName", cutsceneName);
				playTag.putString("LerpType", easing.name());
				playTag.putInt("Length", length);
				playTag.putInt("holdStart", holdStart);
				playTag.putInt("holdEnd", holdEnd);

				cutsceneManager.syncToAll();
				int total = length + holdStart + holdEnd + cutscene.getTotalAnchorHoldTicks();
				Common.getCutsceneSessionManager().addInstance(target, total);
				PacketDistributor.sendToPlayer(target, new CutscenePacket(playTag));
			}
			case CANCEL -> {
				String targetName = this.nbt.getStringOr("TargetPlayer", "");
				if (targetName.isBlank()) {
					return;
				}
				ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
				if (target == null) {
					return;
				}
				PacketDistributor.sendToPlayer(target, cancelPacket());
				Common.getCutsceneSessionManager().cancelCutscene(target);
			}
		}
	}
}
