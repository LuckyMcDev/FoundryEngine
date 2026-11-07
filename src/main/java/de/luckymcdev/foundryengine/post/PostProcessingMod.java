package de.luckymcdev.foundryengine.post;

import de.luckymcdev.foundryengine.Client;
import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.config.ModuleConfig;
import de.luckymcdev.foundryengine.util.common.EngineModPart;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

import java.util.concurrent.CompletableFuture;

@Mod(PostProcessingMod.MODID)
public class PostProcessingMod implements EngineModPart {
	public static final String MODID = "foundryengine_postprocessing";
	private static final ModuleConfig MODULE = new ModuleConfig("Post Processing");

	public PostProcessingMod(IEventBus modBus, ModContainer modContainer) {
		if (!MODULE.registerAndCheck(modContainer)) {
			return;
		}
		this.wire(modBus);
		modBus.addListener(this::addClientReloadListener);
	}

	public static boolean isEnabled() {
		return MODULE.ENABLED.get();
	}

	private void addClientReloadListener(AddClientReloadListenersEvent event) {
		event.addListener(Common.id("post_effects"), createReloadListener(
			() -> Client.getPostEffectManager().getRegistry().invalidatePipelineCaches()));
	}

	private PreparableReloadListener createReloadListener(Runnable r) {
		return (s, bg, barrier, game) ->
			CompletableFuture.<Void>supplyAsync(() -> null, bg)
				.thenCompose(barrier::wait)
				.thenAcceptAsync(v -> r.run(), game);
	}
}