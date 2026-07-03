package de.luckymcdev.foundryengine.common.registry;

import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder;
import de.luckymcdev.foundryengine.common.builder.sound.SoundBuilder;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegistryCollector {
    private final Map<Identifier, ItemBuilder> items = new LinkedHashMap<>();
    private final Map<Identifier, BlockBuilder> blocks = new LinkedHashMap<>();
    private final Map<Identifier, ParticleBuilder> particles = new LinkedHashMap<>();
    private final Map<Identifier, SoundBuilder> sounds = new LinkedHashMap<>();
    private final Map<Identifier, RecipeBuilder> recipes = new LinkedHashMap<>();

    public void addItem(ItemBuilder builder) { items.put(builder.getId(), builder); }
    public void addBlock(BlockBuilder builder) { blocks.put(builder.getId(), builder); }
    public void addParticle(ParticleBuilder builder) { particles.put(builder.getId(), builder); }
    public void addSound(SoundBuilder builder) { sounds.put(builder.getId(), builder); }
    public void addRecipe(RecipeBuilder builder) { recipes.put(builder.getId(), builder); }

    public Collection<ItemBuilder> getItems() { return Collections.unmodifiableCollection(items.values()); }
    public Collection<BlockBuilder> getBlocks() { return Collections.unmodifiableCollection(blocks.values()); }
    public Collection<ParticleBuilder> getParticles() { return Collections.unmodifiableCollection(particles.values()); }
    public Collection<SoundBuilder> getSounds() { return Collections.unmodifiableCollection(sounds.values()); }
    public Collection<RecipeBuilder> getRecipes() { return Collections.unmodifiableCollection(recipes.values()); }

    @Nullable
    public SoundBuilder getSoundBuilder(Identifier id) { return sounds.get(id); }
}
