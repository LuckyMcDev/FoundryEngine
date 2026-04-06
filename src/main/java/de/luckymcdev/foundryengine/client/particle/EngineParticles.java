package de.luckymcdev.foundryengine.client.particle;

import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.api.event.RegistryEvent;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.particle.data.KeyframeSequence;
import de.luckymcdev.foundryengine.client.particle.data.ParticleColorData;
import de.luckymcdev.foundryengine.client.particle.data.ParticleScaleData;
import de.luckymcdev.foundryengine.client.particle.data.ParticleVelocityData;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.easing.BezierEasing;
import de.luckymcdev.foundryengine.common.easing.Easing;
import de.luckymcdev.foundryengine.common.util.color.Color;
import org.joml.Vector3d;

public class EngineParticles {
    public static final ParticleBuilder FIRE = ParticleBuilder.create(Common.id("fire"))
            .alwaysShow()
            .scale(4.0f)
            .color(Color.WHITE, Color.ORANGE, Easing.QUAD_OUT)
            .lifetime(40);

    public static final ParticleBuilder SWOOPING_WISP = ParticleBuilder.create(Common.id("swooping_wisp"))
            .alwaysShow()
            .lifetime(60)
            .scaleData(new ParticleScaleData(new KeyframeSequence<Float>()
                    .add(2.0f, 0.0f, Easing.LINEAR)
                    .add(10.0f, 0.2f, Easing.BACK_OUT)
                    .add(4.0f, 0.8f, Easing.SINE_IN)
                    .add(1.0f, 1.0f, Easing.SINE_IN)))
            .velocityData(new ParticleVelocityData(new KeyframeSequence<Vector3d>()
                    .add(new Vector3d(0, 0, 0), 0.0f, Easing.LINEAR)
                    .add(new Vector3d(0.2, 0.2, 0.2), 1.0f, new BezierEasing("swoop", 0.7f, 0f, 0.3f, 1f))))
            .color(new Color(0, 255, 255, 255), new Color(0, 255, 255, 0), Easing.QUAD_IN);

    public static final ParticleBuilder MAGIC_FLASH = ParticleBuilder.create(Common.id("magic_flash"))
            .lifetime(30)
            .scaleData(new ParticleScaleData(new KeyframeSequence<Float>()
                    .add(0.5f, 0.0f, Easing.LINEAR)
                    .add(5.0f, 0.1f, Easing.EXPO_OUT)
                    .add(20.0f, 0.5f, Easing.SINE_IN_OUT)
                    .add(0.0f, 1.0f, Easing.QUAD_IN)))
            .colorData(new ParticleColorData(new KeyframeSequence<Color>()
                    .add(Color.WHITE, 0.0f, Easing.LINEAR)
                    .add(new Color(180, 0, 255), 0.2f, Easing.SINE_OUT)
                    .add(Color.BLACK, 1.0f, Easing.QUAD_IN)));

    public static final ParticleBuilder TEST = ParticleBuilder.create(Common.id("test"))
            .lifetime(30)
            .colorData(new ParticleColorData(new KeyframeSequence<Color>()
                    .add(Color.WHITE, 0.0f, Easing.LINEAR)
                    .add(new Color(180, 0, 255), 0.2f, Easing.SINE_OUT)
                    .add(Color.BLACK, 1.0f, Easing.QUAD_IN)));

    public static final ParticleBuilder CURSED_SOUL = ParticleBuilder.create(Common.id("cursed_soul"))
            .lifetime(80)
            .rotation(0f, 25f, Easing.EXPO_IN)
            .scaleData(new ParticleScaleData(new KeyframeSequence<Float>()
                    .add(0.5f, 0.0f, Easing.LINEAR)
                    .add(4.5f, 0.5f, new BezierEasing("shiver", 0.6f, -0.28f, 0.7f, 0.05f))
                    .add(0.0f, 1.0f, Easing.CUBIC_IN)))
            .colorData(new ParticleColorData(new KeyframeSequence<Color>()
                    .add(new Color(160, 32, 240), 0.0f, Easing.LINEAR)
                    .add(new Color(50, 255, 50), 0.6f, Easing.SINE_IN_OUT)
                    .add(Color.BLACK, 1.0f, Easing.QUAD_IN)))
            .velocityData(new ParticleVelocityData(new KeyframeSequence<Vector3d>()
                    .add(new Vector3d(0, 0.05, 0), 0.0f, Easing.LINEAR)
                    .add(new Vector3d(0, 0.1, 0), 1.0f, BezierEasing.EASE_IN_OUT)));
    private static int tickCounter = 0;

    public static void register(RegistryEvent event) {
        event.particles(FIRE);
        event.particles(SWOOPING_WISP);
        event.particles(MAGIC_FLASH);
        event.particles(CURSED_SOUL);
    }

    public static void tick() {
        tickCounter++;

        if (tickCounter % 20 == 0) {
            double baseX = 0;
            double baseY = 100;
            double baseZ = 0;

            Client.getParticleManager().spawn(FIRE, baseX - 5, baseY, baseZ);
            Client.getParticleManager().spawn(SWOOPING_WISP, baseX, baseY, baseZ);
            Client.getParticleManager().spawn(MAGIC_FLASH, baseX + 5, baseY, baseZ);
            Client.getParticleManager().spawn(CURSED_SOUL, baseX + 10, baseY, baseZ);
        }
    }
}