package showcase

import de.luckymcdev.foundryengine.client.particle.ParticleLayer
import de.luckymcdev.foundryengine.client.particle.data.KeyframeSequence
import de.luckymcdev.foundryengine.client.particle.data.ParticleScaleData
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder
import de.luckymcdev.foundryengine.common.easing.Easing
import de.luckymcdev.foundryengine.common.event.BundleEvents
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import de.luckymcdev.foundryengine.common.util.color.Color
import net.minecraft.resources.Identifier
import org.joml.Vector3d

class ClientParticleEntrypoint implements BundleEntrypoint {

    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("showcase", path)
    }

    @Override
    void onLoad() {
        def particle = ParticleBuilder.create(id("sparkle"))
            .alwaysShow()
            .lifetime(30)
            .layer(ParticleLayer.TRANSLUCENT)
            .color(Color.WHITE, Color.RED, Easing.SINE_IN)
            .scaleData(new ParticleScaleData(new KeyframeSequence<Float>()
                .add(0.5f, 0f, Easing.LINEAR)
                .add(1.5f, 1f, Easing.SINE_OUT)))
            .velocity(new Vector3d(0.0, 0.1, 0.0))

        BundleEvents.registry {
            it.particles(particle)
        }
    }

    @Override
    void onUnload() {}
}
