package de.luckymcdev.foundryengine.common.builder.particle;

import de.luckymcdev.foundryengine.client.particle.ParticleLayer;
import de.luckymcdev.foundryengine.client.particle.data.GenericParticleData;
import de.luckymcdev.foundryengine.client.particle.data.KeyframeSequence;
import de.luckymcdev.foundryengine.client.particle.data.ParticleColorData;
import de.luckymcdev.foundryengine.client.particle.data.ParticlePositionData;
import de.luckymcdev.foundryengine.client.particle.data.ParticleRotationData;
import de.luckymcdev.foundryengine.client.particle.data.ParticleScaleData;
import de.luckymcdev.foundryengine.client.particle.data.ParticleVelocityData;
import de.luckymcdev.foundryengine.common.builder.AbstractBuilder;
import de.luckymcdev.foundryengine.common.easing.Easing;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ParticleBuilder extends AbstractBuilder<ParticleType<?>> {
	private ParticleColorData colorData;
	private ParticleScaleData scaleData;
	private ParticleVelocityData velocityData;
	private ParticlePositionData positionData;
	private ParticleRotationData rotationData;
	private boolean alwaysShow = false;
	private Function<Boolean, ParticleType<?>> factory = SimpleParticleType::new;
	private int lifetime = 20;
	private ParticleLayer layer = ParticleLayer.OPAQUE;

	public ParticleBuilder(Identifier id) {
		super(id);
	}

	public static ParticleBuilder create(Identifier id) {
		return new ParticleBuilder(id);
	}

	public ParticleBuilder factory(Function<Boolean, ParticleType<?>> factory) {
		this.factory = factory;
		return this;
	}

	public ParticleBuilder alwaysShow() {
		this.alwaysShow = true;
		return this;
	}

	public ParticleBuilder lifetime(int lifetime) {
		this.lifetime = lifetime;
		return this;
	}

	public ParticleBuilder layer(ParticleLayer layer) {
		this.layer = layer;
		return this;
	}

	public ParticleBuilder colorData(ParticleColorData data) {
		this.colorData = data;
		return this;
	}

	public ParticleBuilder color(Color color) {
		this.colorData = new ParticleColorData(new KeyframeSequence<Color>().add(color, 0, Easing.LINEAR));
		return this;
	}

	public ParticleBuilder color(Color start, Color end, Easing easing) {
		this.colorData = new ParticleColorData(new KeyframeSequence<Color>()
			.add(start, 0, Easing.LINEAR)
			.add(end, 1, easing));
		return this;
	}

	public ParticleBuilder scaleData(ParticleScaleData data) {
		this.scaleData = data;
		return this;
	}

	public ParticleBuilder scale(float scale) {
		this.scaleData = new ParticleScaleData(new KeyframeSequence<Float>().add(scale, 0, Easing.LINEAR));
		return this;
	}

	public ParticleBuilder velocityData(ParticleVelocityData data) {
		this.velocityData = data;
		return this;
	}

	public ParticleBuilder velocity(Vector3d vel) {
		this.velocityData = new ParticleVelocityData(new KeyframeSequence<Vector3d>().add(vel, 0, Easing.LINEAR));
		return this;
	}

	public ParticleBuilder positionData(ParticlePositionData data) {
		this.positionData = data;
		return this;
	}

	public ParticleBuilder position(Vector3d pos) {
		this.positionData = new ParticlePositionData(new KeyframeSequence<Vector3d>().add(pos, 0, Easing.LINEAR));
		return this;
	}

	public ParticleBuilder rotationData(ParticleRotationData data) {
		this.rotationData = data;
		return this;
	}

	public ParticleBuilder rotation(float radians) {
		this.rotationData = new ParticleRotationData(new KeyframeSequence<Float>().add(radians, 0, Easing.LINEAR));
		return this;
	}

	public ParticleBuilder rotation(float start, float end, Easing easing) {
		this.rotationData = new ParticleRotationData(new KeyframeSequence<Float>()
			.add(start, 0, Easing.LINEAR)
			.add(end, 1, easing));
		return this;
	}

	public List<GenericParticleData> mergedData() {
		List<GenericParticleData> merged = new ArrayList<>();
		if (colorData != null) {
			merged.add(colorData);
		}
		if (scaleData != null) {
			merged.add(scaleData);
		}
		if (velocityData != null) {
			merged.add(velocityData);
		}
		if (positionData != null) {
			merged.add(positionData);
		}
		if (rotationData != null) {
			merged.add(rotationData);
		}
		return merged;
	}

	public ParticleColorData getColorData() {
		return colorData;
	}

	public ParticleScaleData getScaleData() {
		return scaleData;
	}

	public ParticleVelocityData getVelocityData() {
		return velocityData;
	}

	public ParticlePositionData getPositionData() {
		return positionData;
	}

	public ParticleRotationData getRotationData() {
		return rotationData;
	}

	/**
	 * The first position keyframe, or {@link Vector3d#ZERO} when none is defined.
	 */
	public Vector3d startPosition() {
		Vector3d value = positionData == null ? null : positionData.getSequence().getFirstValue();
		return value == null ? new Vector3d(0, 0, 0) : value;
	}

	/**
	 * The first velocity keyframe, or {@link Vector3d#ZERO} when none is defined.
	 */
	public Vector3d startVelocity() {
		Vector3d value = velocityData == null ? null : velocityData.getSequence().getFirstValue();
		return value == null ? new Vector3d(0, 0, 0) : value;
	}

	public int getLifetime() {
		return lifetime;
	}

	public ParticleLayer getLayer() {
		return layer;
	}

	public ParticleType<?> register(RegisterEvent.RegisterHelper<ParticleType<?>> helper) {
		ParticleType<?> type = build();
		helper.register(id, type);
		setObject(type);
		return type;
	}

	public ParticleType<?> build() {
		return factory.apply(alwaysShow);
	}

	public ParticleBuilder generateData(boolean generate) {
		this.generateData = generate;
		return this;
	}
}
