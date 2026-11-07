package de.luckymcdev.foundryengine.editor;

import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.network.common.packet.editor.GiveItemPacket;
import de.luckymcdev.foundryengine.network.common.packet.explorer.ClientBoundExplorerPacket;
import de.luckymcdev.foundryengine.network.common.packet.explorer.ServerBoundExplorerPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(value = EditorMod.MODID, dist = Dist.DEDICATED_SERVER)
public final class EditorServerMod {
	public EditorServerMod(IEventBus modBus, ModContainer modContainer) {
		modBus.addListener(this::onCommonSetup);
	}

	private void onCommonSetup(FMLCommonSetupEvent event) {
		var network = Common.getNetworkManager();
		network.register(ServerBoundExplorerPacket.DEFINITION);
		network.register(ClientBoundExplorerPacket.DEFINITION);
		network.register(GiveItemPacket.DEFINITION);
	}
}
