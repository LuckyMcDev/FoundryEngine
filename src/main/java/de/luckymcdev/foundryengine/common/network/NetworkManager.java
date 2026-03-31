package de.luckymcdev.foundryengine.common.network;

import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

public class NetworkManager {
    private final GenericRegistry<Identifier, AbstractPacket.Definition<?>> registry = new GenericRegistry<>();
    private final List<AbstractPacket.Definition<?>> commonQueue = new ArrayList<>();
    private final List<AbstractPacket.Definition<?>> clientQueue = new ArrayList<>();

    public <T extends AbstractPacket<T>> void register(AbstractPacket.Definition<T> definition) {
        registry.register(definition.type().id(), definition);

        if (definition.bounds().isServer() || definition.bounds().isBoth()) {
            commonQueue.add(definition);
        }
        if (definition.bounds().isClient() || definition.bounds().isBoth()) {
            clientQueue.add(definition);
        }
    }

    @ApiStatus.Internal
    public void handleRegistration(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        commonQueue.forEach(def -> registerCommon(registrar, def));
        commonQueue.clear();
    }

    @ApiStatus.Internal
    public void handleClientRegistration(RegisterClientPayloadHandlersEvent event) {
        clientQueue.forEach(def -> registerClient(event, def));
        clientQueue.clear();
    }

    private <T extends AbstractPacket<T>> void registerCommon(PayloadRegistrar registrar, AbstractPacket.Definition<T> def) {
        switch (def.bounds()) {
            case CLIENT -> { /*Deferred to a later stage*/ }
            case SERVER -> handleServer(registrar, def);
            case BOTH -> handleBoth(registrar, def);
            default -> throw new EngineException("Unknown PacketBounds: " + def.bounds());
        }
    }

    private <T extends AbstractPacket<T>> void handleServer(PayloadRegistrar registrar, AbstractPacket.Definition<T> def) {
        if (def.serverHandler() != null) {
            registrar.playToServer(def.type(), def.codec(), def.serverHandler()::accept);
        }
    }

    private <T extends AbstractPacket<T>> void handleBoth(PayloadRegistrar registrar, AbstractPacket.Definition<T> def) {
        if (def.serverHandler() != null && def.clientHandler() != null) {
            registrar.playBidirectional(def.type(), def.codec(), def.serverHandler()::accept, def.clientHandler()::accept);
        }
    }


    private <T extends AbstractPacket<T>> void registerClient(RegisterClientPayloadHandlersEvent event, AbstractPacket.Definition<T> def) {
        if (def.clientHandler() != null) {
            event.register(def.type(), def.clientHandler()::accept);
        }
    }
}