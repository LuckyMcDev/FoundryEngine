package de.luckymcdev.foundryengine.common.cutscene.network;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.cutscene.util.LerpType;
import de.luckymcdev.foundryengine.common.easing.BezierPath;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CutsceneActionPacket(Action action, String targetPlayer, String cutsceneName,
                                   int length, String lerpType, int holdStart,
                                   int holdEnd) implements AbstractPacket<CutsceneActionPacket> {

    public static final Definition<CutsceneActionPacket> DEFINITION = new Definition<>(
            AbstractPacket.createType(Common.id("cutscene_action")),
            PacketBounds.SERVER,
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, obj -> obj.action.name(),
                    ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), obj -> java.util.Optional.ofNullable(obj.targetPlayer),
                    ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), obj -> java.util.Optional.ofNullable(obj.cutsceneName),
                    ByteBufCodecs.INT, CutsceneActionPacket::length,
                    ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), obj -> java.util.Optional.ofNullable(obj.lerpType),
                    ByteBufCodecs.INT, CutsceneActionPacket::holdStart,
                    ByteBufCodecs.INT, CutsceneActionPacket::holdEnd,
                    (actionStr, targetPlayerOpt, cutsceneNameOpt, length, lerpTypeOpt, holdStart, holdEnd) ->
                            new CutsceneActionPacket(Action.valueOf(actionStr),
                                    targetPlayerOpt.orElse(null),
                                    cutsceneNameOpt.orElse(null),
                                    length,
                                    lerpTypeOpt.orElse(null),
                                    holdStart, holdEnd)
            ),
            null,
            CutsceneActionPacket::handleServer
    );

    @Override
    public Type<CutsceneActionPacket> getType() {
        return DEFINITION.type();
    }

    @Override
    public PacketBounds getBoundTo() {
        return DEFINITION.bounds();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CutsceneActionPacket> getCodec() {
        return DEFINITION.codec();
    }

    @Override
    public void handleServer(IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        if (!PermissionChecks.COMMANDS_GAMEMASTER.check(player.permissions())) return;

        ServerLevel level = player.level();
        var cutsceneManager = Common.getCutsceneManager();
        if (!cutsceneManager.isLoaded(level.dimension())) {
            cutsceneManager.loadFromLevel(level);
        }

        switch (action) {
            case ADD -> {
                BezierPath path = new BezierPath(player.getEyePosition());
                Vec2 rot = new Vec2(player.getXRot(), player.getYRot());
                boolean added = cutsceneManager.add(level, new Cutscene(cutsceneName, rot, rot, path));
                if (added) {
                    Common.getSavedDataManager().syncToDimension(level);
                }
            }
            case REMOVE -> {
                boolean removed = cutsceneManager.remove(level, cutsceneName);
                if (removed) {
                    Common.getSavedDataManager().syncToDimension(level);
                }
            }
            case PLAY -> {
                ServerPlayer target = player.level().getServer().getPlayerList().getPlayerByName(targetPlayer);
                if (target == null) return;
                Cutscene cutscene = cutsceneManager.find(level.dimension(), cutsceneName);
                if (cutscene == null) return;

                LerpType easing = LerpType.fromString(lerpType);
                CompoundTag tag = new CompoundTag();
                tag.putString("PlayName", cutsceneName);
                tag.putString("LerpType", easing.name());
                tag.putInt("Length", length);
                tag.putInt("holdStart", holdStart);
                tag.putInt("holdEnd", holdEnd);

                Common.getSavedDataManager().syncToPlayer(target);
                int total = length + holdStart + holdEnd;
                Common.getCutsceneSessionManager().addInstance(target, total);
                PacketDistributor.sendToPlayer(target, new CutscenePacket(tag));
            }
            case CANCEL -> {
                ServerPlayer target = player.level().getServer().getPlayerList().getPlayerByName(targetPlayer);
                if (target == null) return;
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("Cancel", true);
                PacketDistributor.sendToPlayer(target, new CutscenePacket(tag));
                Common.getCutsceneSessionManager().cancelCutscene(target);
            }
        }
    }

    public enum Action {
        ADD,
        REMOVE,
        PLAY,
        CANCEL
    }
}
