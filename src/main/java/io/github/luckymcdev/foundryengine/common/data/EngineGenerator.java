package io.github.luckymcdev.foundryengine.common.data;

import io.github.luckymcdev.foundryengine.common.Common;
import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main generator that coordinates both client and server data generation for all bundles.
 */
public class EngineGenerator {
    private final List<BundleClientGenerator> clientGenerators = new ArrayList<>();
    private final List<BundleServerGenerator> serverGenerators = new ArrayList<>();

    public EngineGenerator() {
        this(CompletableFuture.completedFuture(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)));
    }

    public EngineGenerator(CompletableFuture<HolderLookup.Provider> lookupProvider) {
        for (Bundle bundle : Common.getBundleManager().getBundles()) {
            serverGenerators.add(new BundleServerGenerator(bundle, lookupProvider));

            if (FMLEnvironment.getDist().isClient()) {
                clientGenerators.add(new BundleClientGenerator(bundle, lookupProvider));
            }
        }
    }

    public static EngineGenerator fromServer(MinecraftServer server) {
        CompletableFuture<HolderLookup.Provider> lookupProvider =
                CompletableFuture.completedFuture(server.registryAccess());
        return new EngineGenerator(lookupProvider);
    }

    public static EngineGenerator fromRegistryAccess(RegistryAccess registryAccess) {
        CompletableFuture<HolderLookup.Provider> lookupProvider =
                CompletableFuture.completedFuture(registryAccess);
        return new EngineGenerator(lookupProvider);
    }

    public void run() throws IOException {
        // Run client generation (only on client)
        for (BundleClientGenerator generator : clientGenerators) {
            generator.run();
        }

        // Run server generation (on both sides)
        for (BundleServerGenerator generator : serverGenerators) {
            generator.run();
        }
    }

    public void runClientOnly() throws IOException {
        if (FMLEnvironment.getDist() != Dist.CLIENT) {
            throw new IllegalStateException("Cannot run client generators on the server!");
        }

        for (BundleClientGenerator generator : clientGenerators) {
            generator.run();
        }
    }

    public void runServerOnly() throws IOException {
        for (BundleServerGenerator generator : serverGenerators) {
            generator.run();
        }
    }

    public List<BundleClientGenerator> getClientGenerators() {
        return clientGenerators;
    }

    public List<BundleServerGenerator> getServerGenerators() {
        return serverGenerators;
    }
}