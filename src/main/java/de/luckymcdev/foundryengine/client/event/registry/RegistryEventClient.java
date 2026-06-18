package de.luckymcdev.foundryengine.client.event.registry;

import de.luckymcdev.foundryengine.client.particle.EngineParticle;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.common.event.registry.RegistryEvent;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Client-only companion to {@link RegistryEvent}.
 * This class must never be loaded on the dedicated server — it is only referenced
 * from within a {@code FMLEnvironment.getDist().isClient()} guard in RegistryEvent,
 * which prevents the server classloader from ever touching it.
 */
public class RegistryEventClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryEventClient.class);

    public static void registerListener(IEventBus modBus) {
        modBus.addListener(RegistryEventClient::registerParticleProviders);
    }

    private static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        for (ParticleBuilder builder : List.copyOf(RegistryEvent.getParticleBuilders())) {
            ParticleType<?> type = builder.get();
            if (type instanceof SimpleParticleType simpleType) {
                event.registerSpriteSet(simpleType, (SpriteSet sprites) ->
                        new EngineParticle.Provider(
                                builder.getId(),
                                builder.getLifetime(),
                                builder.getLayer(),
                                builder.mergedData(),
                                sprites
                        )
                );
            } else {
                LOGGER.warn("Skipping particle provider registration for {} because type {} is not SimpleParticleType.",
                        builder.getId(),
                        type.getClass().getName());
            }
        }
    }
}
