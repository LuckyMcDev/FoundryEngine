package de.luckymcdev.foundryengine.common.network.packets.editor;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.area.AABBArea;
import de.luckymcdev.foundryengine.common.area.Area;
import de.luckymcdev.foundryengine.common.area.BlockArea;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.network.codecs.AABBCodec;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record AreaPacket(
        AreaPacket.Action action,
        Identifier id,
        AABB bounds,
        Identifier dimensionId,
        Color color,
        List<Identifier> modules,
        CompoundTag moduleData,
        CompoundTag linkedAreas,
        AreaPacket.AreaType areaType
) implements AbstractPacket<AreaPacket> {

    public static final Type<AreaPacket> TYPE = AbstractPacket.createType(Common.id("area_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AreaPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(Action::fromOrdinal, Action::ordinal), AreaPacket::action,
            Identifier.STREAM_CODEC, AreaPacket::id,
            AABBCodec.INSTANCE, AreaPacket::bounds,
            Identifier.STREAM_CODEC, AreaPacket::dimensionId,
            ByteBufCodecs.INT.map(Color::new, Color::argb), AreaPacket::color,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), AreaPacket::modules,
            ByteBufCodecs.COMPOUND_TAG, AreaPacket::moduleData,
            ByteBufCodecs.COMPOUND_TAG, AreaPacket::linkedAreas,
            ByteBufCodecs.VAR_INT.map(AreaType::fromOrdinal, AreaType::ordinal), AreaPacket::areaType,
            AreaPacket::new
    );

    public static final Definition<AreaPacket> DEFINITION = new Definition<>(
            TYPE,
            PacketBounds.SERVER,
            CODEC,
            null,
            (packet, ctx) -> handleServer(packet, ctx)
    );
    private static final Identifier SENTINEL = Identifier.fromNamespaceAndPath("foundryengine", "_");

    public static AreaPacket create(Area area) {
        return ofAction(Action.CREATE, area);
    }

    public static AreaPacket update(Area area) {
        return ofAction(Action.UPDATE, area);
    }

    public static AreaPacket remove(Identifier areaId) {
        return new AreaPacket(Action.REMOVE, areaId, new AABB(0, 0, 0, 0, 0, 0), SENTINEL, new Color(0), List.of(), new CompoundTag(), new CompoundTag(), AreaType.AABB);
    }

    public static AreaPacket requestSync() {
        return new AreaPacket(Action.REQUEST_SYNC, SENTINEL, new AABB(0, 0, 0, 0, 0, 0), SENTINEL, new Color(0), List.of(), new CompoundTag(), new CompoundTag(), AreaType.AABB);
    }

    private static AreaPacket ofAction(Action action, Area area) {
        CompoundTag modData = new CompoundTag();
        for (var entry : area.moduleData().entrySet()) {
            modData.put(entry.getKey().toString(), entry.getValue());
        }

        CompoundTag links = new CompoundTag();
        for (var entry : area.linkedAreas().entrySet()) {
            links.putString(entry.getKey(), entry.getValue().toString());
        }

        AreaType areaType = area instanceof BlockArea ? AreaType.BLOCK : AreaType.AABB;

        return new AreaPacket(
                action,
                area.id(),
                area.bounds(),
                area.dimension().identifier(),
                area.color(),
                List.copyOf(area.moduleIds()),
                modData,
                links,
                areaType
        );
    }

    private static void handleServer(AreaPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!PermissionChecks.COMMANDS_GAMEMASTER.check(player.permissions())) return;

            ServerLevel level = player.level();
            if (level == null) return;

            var areaManager = Common.getAreaManager();

            switch (packet.action()) {
                case REQUEST_SYNC -> {
                    areaManager.syncToPlayer(player);
                }
                case REMOVE -> {
                    Area area = areaManager.getArea(packet.id());
                    if (area != null) {
                        areaManager.remove(level, area);
                    }
                }
                case CREATE -> {
                    var area = switch (packet.areaType()) {
                        case BLOCK -> {
                            BlockPos pos = BlockPos.containing(
                                    packet.bounds().minX + 0.5,
                                    packet.bounds().minY,
                                    packet.bounds().minZ + 0.5
                            );
                            yield new BlockArea(packet.id(), pos, level.dimension(), packet.color());
                        }
                        case AABB -> new AABBArea(packet.id(), packet.bounds(), level.dimension(), packet.color());
                    };
                    applyPacketData(area, packet);
                    areaManager.register(level, area);
                }
                case UPDATE -> {
                    Area existing = areaManager.getArea(packet.id());
                    var updated = existing instanceof BlockArea
                            ? new BlockArea(packet.id(), ((BlockArea) existing).pos(), level.dimension(), packet.color())
                            : new AABBArea(packet.id(), packet.bounds(), level.dimension(), packet.color());
                    applyPacketData(updated, packet);
                    areaManager.update(level, updated);
                }
            }
        });
    }

    private static void applyPacketData(Area area, AreaPacket packet) {
        for (Identifier mid : packet.modules()) {
            area.addModule(mid);
        }
        CompoundTag modData = packet.moduleData();
        for (String key : modData.keySet()) {
            modData.getCompound(key).ifPresent(ct -> area.setModuleData(Identifier.parse(key), ct));
        }
        CompoundTag links = packet.linkedAreas();
        for (String key : links.keySet()) {
            links.getString(key).ifPresent(value -> area.linkArea(key, Identifier.parse(value)));
        }
    }

    public enum Action {
        CREATE, UPDATE, REMOVE, REQUEST_SYNC;

        public static Action fromOrdinal(int ordinal) {
            return values()[ordinal];
        }
    }

    public enum AreaType {
        BLOCK, AABB;

        public static AreaType fromOrdinal(int ordinal) {
            return values()[ordinal];
        }
    }

    @Override
    public Type<AreaPacket> getType() {
        return TYPE;
    }

    @Override
    public PacketBounds getBoundTo() {
        return PacketBounds.SERVER;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AreaPacket> getCodec() {
        return CODEC;
    }
}
