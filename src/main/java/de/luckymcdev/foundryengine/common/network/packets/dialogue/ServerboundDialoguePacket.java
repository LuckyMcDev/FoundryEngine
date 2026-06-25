package de.luckymcdev.foundryengine.common.network.packets.dialogue;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-to-server packet for dialogue interaction.
 * Carries a SELECT_OPTION / ADVANCE_NEXT / END action and an optional optionId.
 */
public record ServerboundDialoguePacket(
        Action action,
        String optionId
) implements AbstractPacket<ServerboundDialoguePacket> {

    public static final Type<ServerboundDialoguePacket> TYPE = AbstractPacket.createType(Common.id("dialogue_server"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundDialoguePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(Action::fromOrdinal, Action::ordinal), ServerboundDialoguePacket::action,
            ByteBufCodecs.STRING_UTF8, ServerboundDialoguePacket::optionId,
            ServerboundDialoguePacket::new
    );
    public static final Definition<ServerboundDialoguePacket> DEFINITION = new Definition<>(
            TYPE,
            PacketBounds.SERVER,
            CODEC,
            null,
            (packet, ctx) -> handleServer(packet, ctx)
    );

    public static ServerboundDialoguePacket selectOption(String optionId) {
        return new ServerboundDialoguePacket(Action.SELECT_OPTION, optionId);
    }

    public static ServerboundDialoguePacket advanceNext() {
        return new ServerboundDialoguePacket(Action.ADVANCE_NEXT, "");
    }

    public static ServerboundDialoguePacket end() {
        return new ServerboundDialoguePacket(Action.END, "");
    }

    private static void handleServer(ServerboundDialoguePacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            var dialogueManager = Common.getDialogueManager();
            switch (packet.action()) {
                case SELECT_OPTION -> {
                    dialogueManager.selectOption(player, packet.optionId());
                }
                case ADVANCE_NEXT -> {
                    dialogueManager.advanceNext(player);
                }
                case END -> {
                    dialogueManager.endDialogue(player);
                }
            }
        });
    }

    @Override
    public Type<ServerboundDialoguePacket> getType() {
        return TYPE;
    }

    @Override
    public PacketBounds getBoundTo() {
        return PacketBounds.SERVER;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ServerboundDialoguePacket> getCodec() {
        return CODEC;
    }

    public enum Action {
        SELECT_OPTION, ADVANCE_NEXT, END;

        public static Action fromOrdinal(int ordinal) {
            var values = values();
            if (ordinal < 0 || ordinal >= values.length) return END;
            return values[ordinal];
        }
    }
}
