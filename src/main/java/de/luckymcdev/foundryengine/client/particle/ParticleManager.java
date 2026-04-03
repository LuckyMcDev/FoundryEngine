package de.luckymcdev.foundryengine.client.particle;

import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilderImpl;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.SimpleParticleType;

public class ParticleManager {
    public void spawn(ParticleBuilder builder) {
        ParticleBuilderImpl impl = (ParticleBuilderImpl) builder;
        Particle particle = Client.getMinecraft().particleEngine.createParticle(
                (SimpleParticleType) impl.get(),
                impl.getPosition().x(), impl.getPosition().y(), impl.getPosition().z(),
                impl.getVelocity().x(), impl.getVelocity().y(), impl.getVelocity().z()
        );
        if (particle != null) {
            Client.getMinecraft().particleEngine.add(particle);
        }
    }
}
