package de.luckymcdev.foundryengine.sound;

import de.luckymcdev.foundryengine.util.common.EngineModPart;
import de.luckymcdev.foundryengine.config.ModuleConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(SoundMod.MODID)
public class SoundMod implements EngineModPart {
	public static final String MODID = "foundryengine_sound";
	private static final ModuleConfig MODULE = new ModuleConfig("Sound");

	public SoundMod(IEventBus modBus, ModContainer modContainer) {
		if (!MODULE.registerAndCheck(modContainer)) {
			return;
		}
		this.wire(modBus);
	}

	public static boolean isEnabled() {
		return MODULE.ENABLED.get();
	}
}
