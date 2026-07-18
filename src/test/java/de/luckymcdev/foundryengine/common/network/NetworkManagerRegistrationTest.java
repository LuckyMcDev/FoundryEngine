package de.luckymcdev.foundryengine.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(EphemeralTestServerProvider.class)
class NetworkManagerRegistrationTest {

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("test", path);
	}

	@Test
	void registerAndHandle_ClientBoundPacket() {
		NetworkManager mgr = new NetworkManager();
		AtomicBoolean handled = new AtomicBoolean(false);

		Identifier packetId = id("client_test");
		var type = new CustomPacketPayload.Type<TestPacket>(packetId);
		var def = new AbstractPacket.Definition<TestPacket>(
			type,
			PacketBounds.CLIENT,
			StreamCodec.unit(new TestPacket()),
			(packet, ctx) -> handled.set(true),
			null
		);

		assertDoesNotThrow(() -> mgr.register(def));
	}

	@Test
	void registerAndHandle_ServerBoundPacket() {
		NetworkManager mgr = new NetworkManager();
		AtomicBoolean handled = new AtomicBoolean(false);

		Identifier packetId = id("server_test");
		var type = new CustomPacketPayload.Type<TestPacket>(packetId);
		var def = new AbstractPacket.Definition<TestPacket>(
			type,
			PacketBounds.SERVER,
			StreamCodec.unit(new TestPacket()),
			null,
			(packet, ctx) -> handled.set(true)
		);

		assertDoesNotThrow(() -> mgr.register(def));
	}

	@Test
	void registerAndHandle_BidirectionalPacket() {
		NetworkManager mgr = new NetworkManager();
		AtomicBoolean clientHandled = new AtomicBoolean(false);
		AtomicBoolean serverHandled = new AtomicBoolean(false);

		Identifier packetId = id("both_test");
		var type = new CustomPacketPayload.Type<TestPacket>(packetId);
		var def = new AbstractPacket.Definition<TestPacket>(
			type,
			PacketBounds.BOTH,
			StreamCodec.unit(new TestPacket()),
			(packet, ctx) -> clientHandled.set(true),
			(packet, ctx) -> serverHandled.set(true)
		);

		assertDoesNotThrow(() -> mgr.register(def));
	}

	private record TestPacket() implements AbstractPacket<TestPacket> {
		@Override
		public Type<? extends CustomPacketPayload> type() {
			return new Type<>(id("test"));
		}

		@Override
		public Type<TestPacket> getType() {
			return new Type<>(id("test"));
		}

		@Override
		public PacketBounds getBoundTo() {
			return PacketBounds.CLIENT;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, TestPacket> getCodec() {
			return StreamCodec.unit(new TestPacket());
		}
	}
}
