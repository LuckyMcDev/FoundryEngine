package de.luckymcdev.foundryengine.waypoint;

import de.luckymcdev.foundryengine.Client;
import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.editor.client.panel.tool.WaypointPanel;
import de.luckymcdev.foundryengine.network.common.packet.editor.WaypointPacket;
import de.luckymcdev.foundryengine.util.common.EngineModPart;
import de.luckymcdev.foundryengine.util.common.color.Color;
import de.luckymcdev.foundryengine.waypoint.client.ClientWaypointManager;
import de.luckymcdev.foundryengine.waypoint.common.WaypointManager;
import de.luckymcdev.foundryengine.manager.api.ManagerKey;
import net.minecraft.core.Vec3i;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@Mod(WaypointMod.MODID)
public final class WaypointMod implements EngineModPart {
	public static final String MODID = "foundryengine_waypoint";

	public WaypointMod(IEventBus modBus, ModContainer modContainer) {
		this.wire(modBus);
		Common.getManagers().register(ManagerKey.WAYPOINT, new WaypointManager(Common.getSavedDataManager()));
		if (FMLEnvironment.getDist().isClient()) {
			Client.getManagers().register(ManagerKey.CLIENT_WAYPOINT, new ClientWaypointManager());
		}
		var b = bus();
		if (FMLEnvironment.getDist().isClient()) {
			modBus.addListener(this::onRegisterKeyMapping);
			b.addListener(this::onClientTickPost);
		}
	}

	@Override
	public void onCommonSetup(FMLCommonSetupEvent event) {
		Common.getNetworkManager().register(WaypointPacket.DEFINITION);
	}

	@Override
	public void onClientSetup(FMLClientSetupEvent event) {
		Client.getEditorManager().register(WaypointPanel.INSTANCE);
	}

	private void onRegisterKeyMapping(RegisterKeyMappingsEvent event) {
		event.register(ClientWaypointManager.PRIMARY_WAYPOINT_KEY);
		event.register(ClientWaypointManager.REMOVE_WAYPOINT_KEY);
	}

	private void onClientTickPost(ClientTickEvent.Post event) {
		Vec3i coords = Client.getBlockHitOrNull();
		while (ClientWaypointManager.PRIMARY_WAYPOINT_KEY.consumeClick()) {
			if (coords != null) {
				ClientPacketDistributor.sendToServer(
					WaypointPacket.add(coords.getX(), coords.getY(), coords.getZ(),
						"Info", "I", Color.TURQUOISE));
			}
		}
		while (ClientWaypointManager.REMOVE_WAYPOINT_KEY.consumeClick()) {
			if (coords != null) {
				ClientPacketDistributor.sendToServer(
					WaypointPacket.remove(coords.getX(), coords.getY(), coords.getZ()));
			}
		}
	}
}