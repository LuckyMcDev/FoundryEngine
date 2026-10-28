package de.luckymcdev.foundryengine.waypoint;

import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.network.common.packet.editor.WaypointPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod(WaypointMod.MODID)
public final class WaypointMod {
	public static final String MODID = "foundryengine_waypoint";

	public WaypointMod(IEventBus modBus, ModContainer modContainer) {
		var bus = NeoForge.EVENT_BUS;
		bus.addListener(this::onServerStarting);
		bus.addListener(this::onServerStopping);
		modBus.addListener(this::onCommonSetup);
	}

	private void onCommonSetup(FMLCommonSetupEvent event) {
		var network = Common.getNetworkManager();
		network.register(WaypointPacket.DEFINITION);
	}

	private void onServerStarting(ServerStartingEvent event) {
		Common.getWaypointManager().load();
	}

	private void onServerStopping(ServerStoppingEvent event) {
		Common.getWaypointManager().save();
	}
}
