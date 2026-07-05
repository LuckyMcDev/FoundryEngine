package de.luckymcdev.foundryengine.common.network.packets.explorer;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.util.PackResourceScanner;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record ServerBoundExplorerPacket(
	Action action,
	String path,
	String payload
) implements AbstractPacket<ServerBoundExplorerPacket> {

	private static final StreamCodec<ByteBuf, Action> ACTION_CODEC = ByteBufCodecs.VAR_INT.map(
		i -> Action.values()[i],
		a -> a.ordinal()
	);
	public static final Definition<ServerBoundExplorerPacket> DEFINITION = new Definition<>(
		AbstractPacket.createType(Common.id("server_explorer")),
		PacketBounds.SERVER,
		StreamCodec.composite(
			ACTION_CODEC, ServerBoundExplorerPacket::action,
			ByteBufCodecs.STRING_UTF8, ServerBoundExplorerPacket::path,
			ByteBufCodecs.STRING_UTF8, ServerBoundExplorerPacket::payload,
			ServerBoundExplorerPacket::new
		),
		null,
		ServerBoundExplorerPacket::handleServer
	);

	@Override
	public Type<ServerBoundExplorerPacket> getType() {
		return DEFINITION.type();
	}

	@Override
	public PacketBounds getBoundTo() {
		return DEFINITION.bounds();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ServerBoundExplorerPacket> getCodec() {
		return DEFINITION.codec();
	}

	@Override
	public void handleServer(IPayloadContext ctx) {
		if (!(ctx.player() instanceof ServerPlayer player)) return;
		if (!PermissionChecks.COMMANDS_OWNER.check(player.permissions())) return;

		switch (action) {
			case REQUEST_FILE_LIST -> handleFileList(player);
			case REQUEST_FILE_CONTENT -> handleFileContent(player);
			case SAVE_FILE -> handleSaveFile(player);
			case REQUEST_RESOURCE_LIST -> handleResourceList(player);
			case REQUEST_RESOURCE_CONTENT -> handleResourceContent(player);
		}
	}

	private void handleFileList(ServerPlayer player) {
		Path serverRoot = Common.DIRECTORY;
		Path resolved = serverRoot.resolve(path).normalize();
		if (!resolved.startsWith(serverRoot)) return;

		File rootFile = resolved.toFile();
		if (!rootFile.exists() || !rootFile.isDirectory()) return;

		List<ClientBoundExplorerPacket.RemoteEntry> entries = new ArrayList<>();
		collectEntries(serverRoot, rootFile, entries);

		PacketDistributor.sendToPlayer(player, new ClientBoundExplorerPacket(
			ClientBoundExplorerPacket.Action.FILE_LIST, path, "", entries, List.of()));
	}

	private void collectEntries(Path serverRoot, File dir, List<ClientBoundExplorerPacket.RemoteEntry> out) {
		File[] children = dir.listFiles();
		if (children == null) return;
		for (File child : children) {
			String relative = serverRoot.relativize(child.toPath().normalize()).toString().replace('\\', '/');
			out.add(new ClientBoundExplorerPacket.RemoteEntry(relative, child.isDirectory()));
			if (child.isDirectory()) collectEntries(serverRoot, child, out);
		}
	}

	private void handleFileContent(ServerPlayer player) {
		Path serverRoot = Common.DIRECTORY;
		Path target = serverRoot.resolve(path).normalize();
		if (!target.startsWith(serverRoot)) return;
		if (!Files.isRegularFile(target)) return;

		try {
			String content = Files.readString(target);
			PacketDistributor.sendToPlayer(player, new ClientBoundExplorerPacket(
				ClientBoundExplorerPacket.Action.FILE_CONTENT, path, content, List.of(), List.of()));
		} catch (IOException e) {
			Common.LOGGER.error("Failed to read remote file {} for player {}: {}",
				path, player.getName().getString(), e.getMessage());
		}
	}

	private void handleSaveFile(ServerPlayer player) {
		Path serverRoot = Common.DIRECTORY;
		Path target = serverRoot.resolve(path).normalize();
		if (!target.startsWith(serverRoot)) {
			Common.LOGGER.warn("Player {} attempted path traversal on save: {}",
				player.getName().getString(), path);
			return;
		}
		if (!Files.isRegularFile(target)) {
			Common.LOGGER.warn("Player {} tried to save to non-existent remote file: {}",
				player.getName().getString(), path);
			return;
		}
		try {
			Files.writeString(target, payload);
			Common.LOGGER.info("Player {} saved remote file: {}", player.getName().getString(), path);
		} catch (IOException e) {
			Common.LOGGER.error("Failed to write remote file {} for player {}: {}",
				path, player.getName().getString(), e.getMessage());
		}
	}

	private void handleResourceList(ServerPlayer player) {
		MinecraftServer server = player.level().getServer();
		if (server == null) return;

		List<String> resources = new ArrayList<>();
		try {
			PackResourceScanner.scanAll(server.getResourceManager(), PackType.SERVER_DATA,
				(id, file) -> resources.add(id.getNamespace() + ":" + id.getPath()));
		} catch (Exception e) {
			Common.LOGGER.debug("Failed to list resources for player {}: {}", player.getName().getString(), e.getMessage());
		}

		PacketDistributor.sendToPlayer(player, new ClientBoundExplorerPacket(
			ClientBoundExplorerPacket.Action.RESOURCE_LIST, "", "", List.of(), resources));
	}

	private void handleResourceContent(ServerPlayer player) {
		Identifier id = Identifier.tryParse(path);
		if (id == null) return;

		try {
			var opt = player.level().getServer().getResourceManager().getResource(id);
			if (opt.isEmpty()) return;

			try (InputStream in = opt.get().open();
			     BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
				String content = reader.lines().collect(Collectors.joining("\n"));
				PacketDistributor.sendToPlayer(player, new ClientBoundExplorerPacket(
					ClientBoundExplorerPacket.Action.RESOURCE_CONTENT, path, content, List.of(), List.of()));
			}
		} catch (IOException e) {
			Common.LOGGER.error("Failed to read resource '{}' for player {}: {}",
				path, player.getName().getString(), e.getMessage());
		}
	}

	public enum Action {
		REQUEST_FILE_LIST,
		REQUEST_FILE_CONTENT,
		SAVE_FILE,
		REQUEST_RESOURCE_LIST,
		REQUEST_RESOURCE_CONTENT
	}
}