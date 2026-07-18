package de.luckymcdev.foundryengine.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NetworkManagerTest {

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("test", path);
	}

	private static AbstractPacket.Definition<TestPacket> def(Identifier id, PacketBounds bounds) {
		return new AbstractPacket.Definition<>(
			new CustomPacketPayload.Type<>(id),
			bounds,
			StreamCodec.unit(new TestPacket(id)),
			null,
			null
		);
	}

	@Test
	void register_SinglePacket_Stored() {
		NetworkManager mgr = new NetworkManager();
		Identifier packetId = id("test_packet");
		var d = def(packetId, PacketBounds.CLIENT);
		mgr.register(d);
		assertDoesNotThrow(() -> mgr.register(d));
	}

	@Test
	void register_MultiplePackets_AllAccepted() {
		NetworkManager mgr = new NetworkManager();
		mgr.register(def(id("packet_a"), PacketBounds.SERVER));
		mgr.register(def(id("packet_b"), PacketBounds.BOTH));
		mgr.register(def(id("packet_c"), PacketBounds.CLIENT));
	}

	@Test
	void register_DuplicateId_DoesNotThrow() {
		NetworkManager mgr = new NetworkManager();
		Identifier dup = id("dup");
		mgr.register(def(dup, PacketBounds.CLIENT));
		assertDoesNotThrow(() -> mgr.register(def(dup, PacketBounds.SERVER)));
	}

	@Test
	void register_ClientBound_Packet() {
		NetworkManager mgr = new NetworkManager();
		assertDoesNotThrow(() -> mgr.register(def(id("client_pkt"), PacketBounds.CLIENT)));
	}

	@Test
	void register_ServerBound_Packet() {
		NetworkManager mgr = new NetworkManager();
		assertDoesNotThrow(() -> mgr.register(def(id("server_pkt"), PacketBounds.SERVER)));
	}

	@Test
	void register_Bidirectional_Packet() {
		NetworkManager mgr = new NetworkManager();
		assertDoesNotThrow(() -> mgr.register(def(id("both_pkt"), PacketBounds.BOTH)));
	}

	private record TestPacket(Identifier id) implements AbstractPacket<TestPacket> {
		@Override
		public Type<TestPacket> getType() {
			return new Type<>(id);
		}

		@Override
		public PacketBounds getBoundTo() {
			return PacketBounds.CLIENT;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, TestPacket> getCodec() {
			return StreamCodec.unit(this);
		}
	}
}
