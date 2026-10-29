package de.luckymcdev.foundryengine.world;

import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.network.common.packet.world.ServerBoundChangeWeatherPacket;
import de.luckymcdev.foundryengine.network.common.packet.world.ServerBoundSetTimePacket;
import de.luckymcdev.foundryengine.network.common.packet.world.ServerBoundSpawnEntityPacket;
import de.luckymcdev.foundryengine.network.common.packet.world.ServerBoundTeleportPacket;
import de.luckymcdev.foundryengine.util.common.EngineModPart;
import de.luckymcdev.foundryengine.world.common.level.util.TransientChunkGenerator;
import de.luckymcdev.foundryengine.world.common.level.util.VoidChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(WorldMod.MODID)
public class WorldMod implements EngineModPart {
	public static final String MODID = "foundryengine_world";

	public WorldMod(IEventBus modBus, ModContainer modContainer) {
		this.wire(modBus);
		modBus.addListener(EventPriority.LOWEST, this::onRegisterChunkGenerators);
	}

	@Override
	public void onCommonSetup(FMLCommonSetupEvent event) {
		var network = Common.getNetworkManager();
		network.register(ServerBoundSetTimePacket.DEFINITION);
		network.register(ServerBoundChangeWeatherPacket.DEFINITION);
		network.register(ServerBoundTeleportPacket.DEFINITION);
		network.register(ServerBoundSpawnEntityPacket.DEFINITION);
	}

	private void onRegisterChunkGenerators(RegisterEvent event) {
		event.register(Registries.CHUNK_GENERATOR, helper -> {
			helper.register(Common.id("void"), VoidChunkGenerator.CODEC);
			helper.register(Common.id("transient"), TransientChunkGenerator.CODEC);
		});
	}
}