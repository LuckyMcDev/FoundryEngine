package de.luckymcdev.foundryengine.bundle;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.Client;
import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.EnginePaths;
import de.luckymcdev.foundryengine.bundle.common.modcompat.BundleModContainer;
import de.luckymcdev.foundryengine.debug.client.screen.BundleDebugEntry;
import de.luckymcdev.foundryengine.network.common.packet.BundleHashPacket;
import de.luckymcdev.foundryengine.script.client.MaliciousScriptScreen;
import de.luckymcdev.foundryengine.script.common.sandbox.SandboxWaiver;
import de.luckymcdev.foundryengine.util.common.FolderHash;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(value = BundleMod.MODID, dist = Dist.CLIENT)
public final class BundleClientMod {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static boolean handledClassFilter;

	public BundleClientMod(IEventBus modBus, ModContainer modContainer) {
		modBus.addListener(this::onRegisterDebugEntry);
		modBus.addListener(this::onClientSetup);
		var bus = NeoForge.EVENT_BUS;
		bus.addListener(this::onScreenOpening);
		bus.addListener(this::onLoggingIn);
	}

	private void onClientSetup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
		if (!BundleMod.isEnabled()) {
			return;
		}
		event.enqueueWork(() -> {
			for (var container : Common.getBundleManager().getBundleContainers()) {
				if (container instanceof BundleModContainer) {
					container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
				}
			}
		});
	}

	private void onRegisterDebugEntry(RegisterDebugEntriesEvent event) {
		if (!BundleMod.isEnabled()) {
			return;
		}
		event.register(Common.id("bundles_info"), new BundleDebugEntry(Common.getBundleManager()));
	}

	private void onScreenOpening(ScreenEvent.Opening event) {
		if (!BundleMod.isEnabled()) {
			return;
		}
		if (handledClassFilter || !(event.getNewScreen() instanceof TitleScreen)) {
			return;
		}
		handledClassFilter = true;
		if (de.luckymcdev.foundryengine.config.StartupConfig.DISABLE_CLASS_FILTER.get()
			&& !SandboxWaiver.isAccepted()) {
			var parent = Client.getCurrentScreen();
			Minecraft.getInstance().execute(() ->
				Client.setScreen(new MaliciousScriptScreen(parent, this::onClassFilterAccepted, this::onClassFilterDeclined)));
		}
	}

	private void onClassFilterAccepted() {
		try {
			SandboxWaiver.markAccepted();
		} catch (java.io.IOException e) {
			LOGGER.error("Failed to write acceptance file", e);
			return;
		}
		Common.getScriptManager().unloadAll();
	}

	private void onClassFilterDeclined() {
		de.luckymcdev.foundryengine.config.StartupConfig.DISABLE_CLASS_FILTER.set(false);
		de.luckymcdev.foundryengine.config.StartupConfig.DISABLE_CLASS_FILTER.save();
		de.luckymcdev.foundryengine.config.StartupConfig.SPEC.save();
		Common.getScriptManager().unloadAll();
	}

	private void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
		if (!BundleMod.isEnabled()) {
			return;
		}
		FolderHash.hashFolderAsync(EnginePaths.BUNDLES)
			.thenAccept(hash -> ClientPacketDistributor.sendToServer(new BundleHashPacket(hash)))
			.exceptionally(e -> {
				LOGGER.error("Failed to hash bundles folder", e);
				return null;
			});
	}
}
