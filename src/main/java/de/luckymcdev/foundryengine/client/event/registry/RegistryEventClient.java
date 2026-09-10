package de.luckymcdev.foundryengine.client.event.registry;

import de.luckymcdev.foundryengine.client.particle.EngineParticle;
import de.luckymcdev.foundryengine.common.builder.blockentity.BlockEntityBuilder;
import de.luckymcdev.foundryengine.common.builder.menu.MenuBuilder;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.common.registry.RegistryCollector;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RegistryEventClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(RegistryEventClient.class);

	public static void registerParticleProviders(RegisterParticleProvidersEvent event, RegistryCollector collector) {
		for (ParticleBuilder builder : List.copyOf(collector.getParticles())) {
			ParticleType<?> type = builder.get();
			if (type instanceof SimpleParticleType simpleType) {
				event.registerSpriteSet(simpleType, (SpriteSet sprites) ->
					new EngineParticle.Provider(
						builder.getId(),
						builder.getLifetime(),
						builder.getLayer(),
						builder.mergedData(),
						sprites
					)
				);
			} else {
				LOGGER.warn("Skipping particle provider registration for {} because type {} is not SimpleParticleType.",
					builder.getId(),
					type.getClass().getName());
			}
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void registerRenderers(RegistryCollector collector) {
		for (BlockEntityBuilder<?> builder : List.copyOf(collector.getBlockEntities())) {
			BlockEntityRendererProvider<?, BlockEntityRenderState> rendererFactory = (BlockEntityRendererProvider<?, BlockEntityRenderState>) builder.getRendererFactory();
			if (rendererFactory != null) {
				var type = builder.get();
				BlockEntityRenderers.register((BlockEntityType) type, rendererFactory);
			}
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void registerMenuScreens(RegisterMenuScreensEvent event, RegistryCollector collector) {
		for (MenuBuilder builder : List.copyOf(collector.getMenus())) {
			var screenFactory = builder.getScreenFactory();
			if (screenFactory != null && !builder.isScreenDisabled()) {
				MenuType type = (MenuType) builder.get();
				event.register(type, (MenuScreens.ScreenConstructor) (menu, inventory, title) ->
					(Screen) screenFactory.create(menu, inventory, title));
			}
		}
	}
}
