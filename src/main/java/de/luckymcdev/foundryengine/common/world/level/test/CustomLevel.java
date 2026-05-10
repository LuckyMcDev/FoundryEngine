package de.luckymcdev.foundryengine.common.world.level.test;

import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeLevel;
import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeLevelConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.function.BooleanSupplier;

public class CustomLevel extends RuntimeLevel {
    private final RecipeManager recipeManager;
    private long dynSeed;

    public CustomLevel(MinecraftServer server, ResourceKey<Level> registryKey, RuntimeLevelConfig config, RuntimeLevel.Style style) {
        super(server, registryKey, config, style);
        this.recipeManager = new RecipeManager(server.registryAccess());
    }

    @Override
    public void tick(@NonNull BooleanSupplier shouldKeepTicking) {
        this.dynSeed = this.random.nextLong();
        super.tick(shouldKeepTicking);
    }

    @Override
    public @NonNull RecipeManager recipeAccess() {
        return this.recipeManager;
    }

    @Override
    public long getSeed() {
        return this.dynSeed;
    }
}
