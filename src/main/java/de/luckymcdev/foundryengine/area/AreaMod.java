package de.luckymcdev.foundryengine.area;

import de.luckymcdev.foundryengine.Client;
import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.config.ModuleConfig;
import de.luckymcdev.foundryengine.editor.EditorMod;
import de.luckymcdev.foundryengine.editor.client.panel.editor.AreaPanel;
import de.luckymcdev.foundryengine.area.common.AreaManager;
import de.luckymcdev.foundryengine.area.client.ClientAreaManager;
import de.luckymcdev.foundryengine.manager.api.ManagerKey;
import de.luckymcdev.foundryengine.network.common.packet.editor.AreaPacket;
import de.luckymcdev.foundryengine.util.common.EngineModPart;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(AreaMod.MODID)
public final class AreaMod implements EngineModPart {
	public static final String MODID = "foundryengine_area";
	private static final ModuleConfig MODULE = new ModuleConfig("Area");

	public AreaMod(IEventBus modBus, ModContainer modContainer) {
		if (!MODULE.registerAndCheck(modContainer)) {
			return;
		}
		this.wire(modBus);
		Common.getManagers().register(ManagerKey.AREA, new AreaManager(Common.getSavedDataManager()));
		if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) {
			Client.getManagers().register(ManagerKey.CLIENT_AREA, new ClientAreaManager());
		}
		var b = bus();
		var mgr = Common.getAreaManager();
		b.addListener(mgr::onBlockBreak);
		b.addListener(mgr::onBlockPlace);
	}

	public static boolean isEnabled() {
		return MODULE.ENABLED.get();
	}

	@Override
	public void onCommonSetup(FMLCommonSetupEvent event) {
		Common.getNetworkManager().register(AreaPacket.DEFINITION);
	}

	@Override
	public void onClientSetup(FMLClientSetupEvent event) {
		if (EditorMod.isEnabled()) {
			Client.getEditorManager().register(AreaPanel.INSTANCE);
		}
	}

}