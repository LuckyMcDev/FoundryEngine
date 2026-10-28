package de.luckymcdev.foundryengine.cutscene;

import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.cutscene.common.util.ServerScreenEffectManager;
import de.luckymcdev.foundryengine.event.common.GameEvents;
import de.luckymcdev.foundryengine.network.common.packet.BundleHashPacket;
import de.luckymcdev.foundryengine.network.common.packet.editor.CutsceneCommandPacket;
import de.luckymcdev.foundryengine.network.common.packet.editor.CutscenePacket;
import de.luckymcdev.foundryengine.network.common.packet.editor.LinearizeCutscenePacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(CutsceneMod.MODID)
public final class CutsceneMod {
	public static final String MODID = "foundryengine_cutscene";

	public CutsceneMod(IEventBus modBus, ModContainer modContainer) {
		var bus = NeoForge.EVENT_BUS;
		bus.addListener(this::onServerStarting);
		bus.addListener(this::onServerStopping);
		bus.addListener(this::onServerTick);
		bus.addListener(this::onPlayerDisconnect);
		modBus.addListener(this::onCommonSetup);
	}

	private void onCommonSetup(FMLCommonSetupEvent event) {
		var network = Common.getNetworkManager();
		network.register(CutscenePacket.DEFINITION);
		network.register(CutsceneCommandPacket.DEFINITION);
		network.register(LinearizeCutscenePacket.DEFINITION);
	}

	private void onServerStarting(ServerStartingEvent event) {
		Common.getCutsceneManager().load();
	}

	private void onServerStopping(ServerStoppingEvent event) {
		Common.getCutsceneManager().save();
	}

	private void onServerTick(ServerTickEvent.Post event) {
		Common.getCutsceneSessionManager().tick(event.getServer());
		ServerScreenEffectManager.tick();
	}

	private void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			Common.getCutsceneSessionManager().onPlayerDisconnect(player);
			ServerScreenEffectManager.onPlayerDisconnect(player);
		}
	}
}
