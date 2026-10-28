package de.luckymcdev.foundryengine.area;

import de.luckymcdev.foundryengine.Client;
import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.editor.client.panel.editor.AreaPanel;
import de.luckymcdev.foundryengine.network.common.packet.editor.AreaPacket;
import de.luckymcdev.foundryengine.util.common.EngineModPart;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod(AreaMod.MODID)
public final class AreaMod implements EngineModPart {
	public static final String MODID = "foundryengine_area";

	public AreaMod(IEventBus modBus, ModContainer modContainer) {
		this.wire(modBus);
		var b = bus();
		var mgr = Common.getAreaManager();
		b.addListener(mgr::onBlockBreak);
		b.addListener(mgr::onBlockPlace);
		b.addListener(mgr::onLevelTick);
		b.addListener(this::onServerStarting);
		b.addListener(this::onServerStopping);
	}

	@Override
	public void onCommonSetup(FMLCommonSetupEvent event) {
		Common.getNetworkManager().register(AreaPacket.DEFINITION);
	}

	@Override
	public void onClientSetup(FMLClientSetupEvent event) {
		Client.getEditorManager().register(AreaPanel.INSTANCE);
	}

	private void onServerStarting(ServerStartingEvent event) {
		Common.getAreaManager().load();
	}

	private void onServerStopping(ServerStoppingEvent event) {
		Common.getAreaManager().save();
	}
}