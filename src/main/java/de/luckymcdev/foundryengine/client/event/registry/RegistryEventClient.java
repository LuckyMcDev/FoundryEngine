package de.luckymcdev.foundryengine.client.event.registry;

import de.luckymcdev.foundryengine.client.particle.EngineParticle;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.common.registry.RegistryCollector;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RegistryEventClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(RegistryEventClient.class);

	public static void registerParticleProviders(RegisterParticleProvidersEvent event, RegistryCollector collector) {
		for (ParticleBuilder builder : List.copyOf(collector.getParticles())) {
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
