package de.luckymcdev.foundryengine.common.network;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class NetworkManager {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final GenericRegistry<Identifier, AbstractPacket.Definition<?>> registry = new GenericRegistry<>();
	private final List<AbstractPacket.Definition<?>> registrationQueue = new ArrayList<>();

	public <T extends AbstractPacket<T>> void register(AbstractPacket.Definition<T> definition) {
		registry.register(definition.type().id(), definition);
		registrationQueue.add(definition);
	}

	@ApiStatus.Internal
	public void handleRegistration(RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(Common.MODID).versioned("1").optional();

		registrationQueue.forEach(def -> registerPacket(registrar, def));
		registrationQueue.clear();
	}

	private <T extends AbstractPacket<T>> void registerPacket(PayloadRegistrar registrar, AbstractPacket.Definition<T> def) {
		switch (def.bounds()) {
			case CLIENT -> handleClient(registrar, def);
			case SERVER -> handleServer(registrar, def);
			case BOTH -> handleBoth(registrar, def);
		}
	}

	private <T extends AbstractPacket<T>> void handleClient(PayloadRegistrar registrar, AbstractPacket.Definition<T> def) {
		if (def.clientHandler() != null) {
			registrar.playToClient(def.type(), def.codec(), def.clientHandler()::accept);
		} else {
			LOGGER.warn("NetworkManager: packet {} is bound to client but has no client handler; skipping registration", def.type().id());
		}
	}

	private <T extends AbstractPacket<T>> void handleServer(PayloadRegistrar registrar, AbstractPacket.Definition<T> def) {
		if (def.serverHandler() != null) {
			registrar.playToServer(def.type(), def.codec(), def.serverHandler()::accept);
		} else {
			LOGGER.warn("NetworkManager: packet {} is bound to server but has no server handler; skipping registration", def.type().id());
		}
	}

	private <T extends AbstractPacket<T>> void handleBoth(PayloadRegistrar registrar, AbstractPacket.Definition<T> def) {
		if (def.serverHandler() != null && def.clientHandler() != null) {
			registrar.playBidirectional(def.type(), def.codec(), def.serverHandler()::accept, def.clientHandler()::accept);
		} else {
			LOGGER.warn("NetworkManager: packet {} is bound to both sides but is missing {} handler; skipping registration",
				def.type().id(), def.serverHandler() == null ? "server" : "client");
		}
	}

	public <T extends AbstractPacket<T>> void sendToServer(T packet) {
		ClientPacketDistributor.sendToServer(packet);
	}

	public <T extends AbstractPacket<T>> void sendToPlayer(T packet, ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, packet);
	}

	public <T extends AbstractPacket<T>> void sendToAllPlayers(T packet) {
		PacketDistributor.sendToAllPlayers(packet);
	}
}