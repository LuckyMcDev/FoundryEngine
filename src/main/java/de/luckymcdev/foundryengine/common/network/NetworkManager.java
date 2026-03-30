package de.luckymcdev.foundryengine.common.network;

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
        this.registry.register(definition.type().id(), definition);

        if (definition.bounds().isServer() || definition.bounds().isBoth()) {
            commonQueue.add(definition);
        }
        if (definition.bounds().isClient() || definition.bounds().isBoth()) {
            clientQueue.add(definition);
        }
    }

    @ApiStatus.Internal
    public void handleRegistration(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        for (AbstractPacket.Definition<?> def : commonQueue) {
            registerCommon(registrar, def);
        }
        commonQueue.clear();
    }

    @ApiStatus.Internal
    public void handleClientRegistration(RegisterClientPayloadHandlersEvent event) {
        for (AbstractPacket.Definition<?> def : clientQueue) {
            registerClient(event, def);
        }
        clientQueue.clear();
    }

    private <T extends AbstractPacket<T>> void registerCommon(PayloadRegistrar registrar, AbstractPacket.Definition<T> def) {
        var type = def.type();
        var codec = def.codec();

        switch (def.bounds()) {
            case CLIENT -> registrar.playToClient(type, codec, (p, ctx) -> {
            });
            case SERVER -> {
                if (def.serverHandler() != null) {
                    registrar.playToServer(type, codec, def.serverHandler()::accept);
                }
            }
            case BOTH -> {
                if (def.serverHandler() != null) {
                    registrar.playBidirectional(type, codec, def.serverHandler()::accept);
                }
            }
        }
    }

    private <T extends AbstractPacket<T>> void registerClient(RegisterClientPayloadHandlersEvent event, AbstractPacket.Definition<T> def) {
        if (def.clientHandler() != null) {
            event.register(def.type(), def.clientHandler()::accept);
        }
    }
}