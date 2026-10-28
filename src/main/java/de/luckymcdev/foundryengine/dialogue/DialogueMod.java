package de.luckymcdev.foundryengine.dialogue;

import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.event.common.DialogueEvents;
import de.luckymcdev.foundryengine.network.common.packet.dialogue.ClientboundDialoguePacket;
import de.luckymcdev.foundryengine.network.common.packet.dialogue.DialogueSavePacket;
import de.luckymcdev.foundryengine.network.common.packet.dialogue.ServerboundDialoguePacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod(DialogueMod.MODID)
public final class DialogueMod {
	public static final String MODID = "foundryengine_dialogue";

	public DialogueMod(IEventBus modBus, ModContainer modContainer) {
		DialogueEvents.Internal.register(NeoForge.EVENT_BUS);
		var bus = NeoForge.EVENT_BUS;
		bus.addListener(this::onServerStarting);
		bus.addListener(this::onServerStopping);
		bus.addListener(this::onPlayerDisconnect);
		modBus.addListener(this::onCommonSetup);
	}

	private void onCommonSetup(FMLCommonSetupEvent event) {
		var network = Common.getNetworkManager();
		network.register(ClientboundDialoguePacket.DEFINITION);
		network.register(ServerboundDialoguePacket.DEFINITION);
		network.register(DialogueSavePacket.DEFINITION);
	}

	private void onServerStarting(ServerStartingEvent event) {
		Common.getDialogueManager().load();
	}

	private void onServerStopping(ServerStoppingEvent event) {
		Common.getDialogueManager().save();
	}

	private void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			Common.getDialogueManager().onPlayerDisconnect(player);
		}
	}
}
