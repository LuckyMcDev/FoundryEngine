package de.luckymcdev.foundryengine.particle;

import de.luckymcdev.foundryengine.util.common.EngineModPart;
import de.luckymcdev.foundryengine.config.ModuleConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(ParticleMod.MODID)
public class ParticleMod implements EngineModPart {
	public static final String MODID = "foundryengine_particle";
	private static final ModuleConfig MODULE = new ModuleConfig("Particle");

	public ParticleMod(IEventBus modBus, ModContainer modContainer) {
		if (!MODULE.registerAndCheck(modContainer)) {
			return;
		}
		this.wire(modBus);
	}

	public static boolean isEnabled() {
		return MODULE.ENABLED.get();
	}
}
