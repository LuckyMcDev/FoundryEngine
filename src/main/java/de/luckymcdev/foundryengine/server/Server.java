package de.luckymcdev.foundryengine.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.WorldData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Shared server-side utilities and helpers for FoundryEngine.
 * Only use this class from dedicated server or logical server contexts.
 */
public final class Server {
    private Server() {
    }

    /**
     * Returns the current {@link MinecraftServer}, or {@code null} if none is running.
     */
    public static @Nullable MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    /**
     * Returns the server's {@link RecipeManager}, or {@code null} if the server is not running.
     */
    public static @Nullable RecipeManager getRecipeManager() {
        MinecraftServer server = getServer();
        return server != null ? server.getRecipeManager() : null;
    }

    /**
     * Reloads all server resources, picking up any newly available data packs.
     * Returns a completed future immediately if no server is running.
     */
    public static CompletableFuture<Void> reloadResources() {
        MinecraftServer server = getServer();
        if (server == null) return CompletableFuture.completedFuture(null);

        PackRepository repo = server.getPackRepository();
        WorldData worldData = server.getWorldData();
        Collection<String> selected = new ArrayList<>(repo.getSelectedIds());

        repo.reload();
        for (String pack : repo.getAvailableIds()) {
            if (!worldData.getDataConfiguration().dataPacks().getDisabled().contains(pack)
                    && !selected.contains(pack)) {
                selected.add(pack);
            }
        }

        return server.reloadResources(selected);
    }
}