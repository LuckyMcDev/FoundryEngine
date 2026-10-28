package de.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.config.Config;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import org.slf4j.Logger;

/** Dedicated-server entrypoint — config only; feature wiring lives in the module mods. */
@Mod(value = Common.MODID, dist = Dist.DEDICATED_SERVER)
public class EngineServerMod {
	private static final Logger LOGGER = LogUtils.getLogger();

	public EngineServerMod(IEventBus modBus, ModContainer modContainer) {
		modBus.addListener(this::onServerSetup);
		Config.registerServer(modContainer);
	}

	private void onServerSetup(FMLDedicatedServerSetupEvent event) {
		LOGGER.debug("ServerMod setup called");
	}
}