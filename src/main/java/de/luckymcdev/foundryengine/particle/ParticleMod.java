package de.luckymcdev.foundryengine.particle;

import de.luckymcdev.foundryengine.util.common.EngineModPart;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(ParticleMod.MODID)
public class ParticleMod implements EngineModPart {
	public static final String MODID = "foundryengine_particle";

	public ParticleMod(IEventBus modBus, ModContainer modContainer) {
		this.wire(modBus);
	}
}
