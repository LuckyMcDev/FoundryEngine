package de.luckymcdev.foundryengine.sound;

import de.luckymcdev.foundryengine.util.common.EngineModPart;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(SoundMod.MODID)
public class SoundMod implements EngineModPart {
	public static final String MODID = "foundryengine_sound";

	public SoundMod(IEventBus modBus, ModContainer modContainer) {
		this.wire(modBus);
	}
}
