package de.luckymcdev.foundryengine.common.network.packets.editor;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LinearizeCutscenePacket(String cutsceneName) implements AbstractPacket<LinearizeCutscenePacket> {

	public static final Definition<LinearizeCutscenePacket> DEFINITION = new Definition<>(
		AbstractPacket.createType(Common.id("linearize_cutscene")),
		PacketBounds.SERVER,
		StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, LinearizeCutscenePacket::cutsceneName,
			LinearizeCutscenePacket::new
		),
		null,
		LinearizeCutscenePacket::handleServer
	);

	@Override
	public Type<LinearizeCutscenePacket> getType() {
		return DEFINITION.type();
	}

	@Override
	public PacketBounds getBoundTo() {
		return DEFINITION.bounds();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, LinearizeCutscenePacket> getCodec() {
		return DEFINITION.codec();
	}

	@Override
	public void handleServer(IPayloadContext ctx) {
		if (!(ctx.player() instanceof ServerPlayer player)) {
			return;
		}
		if (!PermissionChecks.COMMANDS_GAMEMASTER.check(player.permissions())) {
			return;
		}

		ServerLevel level = player.level();
		var manager = Common.getCutsceneManager();

		Cutscene target = manager.find(level.dimension(), cutsceneName);
		if (target == null) {
			Common.LOGGER.warn("LinearizeCutscenePacket: no cutscene found with name '{}'", cutsceneName);
			return;
		}

		if (target.path.getPoints().size() != 4) {
			Common.LOGGER.warn("LinearizeCutscenePacket: cutscene '{}' must have exactly 2 path nodes to linearize", cutsceneName);
			return;
		}

		Vec3 p1 = target.path.getPoints().getFirst().getPos();
		Vec3 p2 = target.path.getPoints().getLast().getPos();
		Vec3 tangent = p2.add(p1.subtract(p2).scale(0.5));
		target.path.getPoints().get(1).setPos(tangent);
		target.path.getPoints().get(2).setPos(tangent);

		manager.save();
		manager.syncToAll();
	}
}
