package de.luckymcdev.foundryengine.dialogue;

import de.luckymcdev.foundryengine.Client;
import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.dialogue.common.DialogueNode;
import de.luckymcdev.foundryengine.dialogue.common.DialogueSession;
import de.luckymcdev.foundryengine.dialogue.common.DialogueManager;
import de.luckymcdev.foundryengine.dialogue.client.ClientDialogueManager;
import de.luckymcdev.foundryengine.manager.api.ManagerKey;
import net.neoforged.fml.loading.FMLEnvironment;
import de.luckymcdev.foundryengine.editor.client.panel.editor.DialogueEditorPanel;
import de.luckymcdev.foundryengine.event.common.DialogueEvents;
import de.luckymcdev.foundryengine.network.common.packet.dialogue.*;
import de.luckymcdev.foundryengine.util.common.EngineModPart;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(DialogueMod.MODID)
public final class DialogueMod implements EngineModPart {
	public static final String MODID = "foundryengine_dialogue";

	public DialogueMod(IEventBus modBus, ModContainer modContainer) {
		DialogueEvents.Internal.register(bus());
		this.wire(modBus);
		Common.getManagers().register(ManagerKey.DIALOGUE, new DialogueManager(Common.getSavedDataManager()));
		if (FMLEnvironment.getDist().isClient()) {
			Client.getManagers().register(ManagerKey.CLIENT_DIALOGUE, new ClientDialogueManager());
		}
		var b = bus();
	}

	@Override
	public void onCommonSetup(FMLCommonSetupEvent event) {
		var network = Common.getNetworkManager();
		network.register(ClientboundDialoguePacket.DEFINITION);
		network.register(ServerboundDialoguePacket.DEFINITION);
		network.register(DialogueSavePacket.DEFINITION);
	}

	@Override
	public void onClientSetup(FMLClientSetupEvent event) {
		Client.getEditorManager().register(DialogueEditorPanel.INSTANCE);
		ClientboundDialoguePacket.CLIENT_HANDLER = p -> {
			var mgr = Client.getDialogueManager();
			switch (p.action()) {
				case SHOW -> mgr.startDialogue(p.treeId(),
					DialogueSession.fromNbt(p.session()), DialogueNode.fromNbt(p.node()));
				case ADVANCE -> mgr.advanceDialogue(
					DialogueSession.fromNbt(p.session()), DialogueNode.fromNbt(p.node()));
				case ENDED -> mgr.endDialogue();
			}
		};
	}

}