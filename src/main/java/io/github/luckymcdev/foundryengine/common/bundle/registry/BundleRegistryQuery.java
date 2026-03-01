package io.github.luckymcdev.foundryengine.common.bundle.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BundleRegistryQuery {
    private final String bundleId;

    public BundleRegistryQuery(String bundleId) {
        this.bundleId = bundleId;
    }

    public <T> List<T> getFromRegistry(Registry<T> registry) {
        return StreamSupport.stream(registry.spliterator(), false)
                .filter(entry -> {
                    Identifier id = registry.getKey(entry);
                    return id != null && id.getNamespace().equals(bundleId);
                })
                .collect(Collectors.toList());
    }

    public List<Block> getBlocks() {
        return getFromRegistry(BuiltInRegistries.BLOCK);
    }

    public List<Item> getItems() {
        return getFromRegistry(BuiltInRegistries.ITEM);
    }

    public List<Fluid> getFluids() {
        return getFromRegistry(BuiltInRegistries.FLUID);
    }

    public List<EntityType<?>> getEntityTypes() {
        return getFromRegistry(BuiltInRegistries.ENTITY_TYPE);
    }

    public List<BlockEntityType<?>> getBlockEntityTypes() {
        return getFromRegistry(BuiltInRegistries.BLOCK_ENTITY_TYPE);
    }

    public List<SoundEvent> getSoundEvents() {
        return getFromRegistry(BuiltInRegistries.SOUND_EVENT);
    }

    public List<ParticleType<?>> getParticleTypes() {
        return getFromRegistry(BuiltInRegistries.PARTICLE_TYPE);
    }

    public List<MobEffect> getMobEffects() {
        return getFromRegistry(BuiltInRegistries.MOB_EFFECT);
    }

    public List<Potion> getPotions() {
        return getFromRegistry(BuiltInRegistries.POTION);
    }

    public List<Attribute> getAttributes() {
        return getFromRegistry(BuiltInRegistries.ATTRIBUTE);
    }

    public List<net.minecraft.stats.StatType<?>> getStatTypes() {
        return getFromRegistry(BuiltInRegistries.STAT_TYPE);
    }

    public List<Feature<?>> getFeatures() {
        return getFromRegistry(BuiltInRegistries.FEATURE);
    }

    public List<StructureType<?>> getStructureTypes() {
        return getFromRegistry(BuiltInRegistries.STRUCTURE_TYPE);
    }

    public List<RecipeSerializer<?>> getRecipeSerializers() {
        return getFromRegistry(BuiltInRegistries.RECIPE_SERIALIZER);
    }
}