package de.luckymcdev.foundryengine.cutscene;

import de.luckymcdev.foundryengine.Client;
import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.cutscene.common.util.ServerScreenEffectManager;
import de.luckymcdev.foundryengine.cutscene.common.CutsceneManager;
import de.luckymcdev.foundryengine.cutscene.common.CutsceneSessionManager;
import de.luckymcdev.foundryengine.cutscene.client.ClientCutsceneManager;
import de.luckymcdev.foundryengine.manager.api.ManagerKey;
import net.neoforged.fml.loading.FMLEnvironment;
import de.luckymcdev.foundryengine.editor.client.panel.cutscene.CutscenePanel;
import de.luckymcdev.foundryengine.editor.client.panel.cutscene.CutsceneTimelinePanel;
import de.luckymcdev.foundryengine.network.common.packet.editor.CutsceneCommandPacket;
import de.luckymcdev.foundryengine.network.common.packet.editor.CutscenePacket;
import de.luckymcdev.foundryengine.network.common.packet.editor.LinearizeCutscenePacket;
import de.luckymcdev.foundryengine.network.common.packet.sync.ScreenEffectPacket;
import de.luckymcdev.foundryengine.util.common.EngineModPart;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(CutsceneMod.MODID)
public final class CutsceneMod implements EngineModPart {
	public static final String MODID = "foundryengine_cutscene";

	public CutsceneMod(IEventBus modBus, ModContainer modContainer) {
		this.wire(modBus);
		Common.getManagers().register(ManagerKey.CUTSCENE,
			new CutsceneManager(Common.getSavedDataManager()));
		Common.getManagers().register(ManagerKey.CUTSCENE_SESSION,
			new CutsceneSessionManager());
		if (FMLEnvironment.getDist().isClient()) {
			Client.getManagers().register(ManagerKey.CLIENT_CUTSCENE, new ClientCutsceneManager());
		}
		var b = bus();
		b.addListener((ServerTickEvent.Post event) -> ServerScreenEffectManager.tick());
		b.addListener(this::onPlayerDisconnectForEffects);
	}

	private void onPlayerDisconnectForEffects(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			ServerScreenEffectManager.onPlayerDisconnect(player);
		}
	}

	@Override
	public void onCommonSetup(FMLCommonSetupEvent event) {
		var network = Common.getNetworkManager();
		network.register(CutscenePacket.DEFINITION);
		network.register(CutsceneCommandPacket.DEFINITION);
		network.register(LinearizeCutscenePacket.DEFINITION);
		network.register(ScreenEffectPacket.DEFINITION);
	}

	@Override
	public void onClientSetup(FMLClientSetupEvent event) {
		Client.getEditorManager().register(CutscenePanel.INSTANCE, CutsceneTimelinePanel.INSTANCE);
		CutscenePacket.CLIENT_HANDLER = p -> Client.getCutsceneManager().handlePacket(p);
		ScreenEffectPacket.CLIENT_HANDLER = p -> Client.getPostEffectManager()
			.startScreenEffect(p.name(), p.introTicks(), p.holdTicks(), p.outroTicks(), p.lerpType());
	}

}