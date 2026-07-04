package de.luckymcdev.foundryengine.common.registry;

import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder;
import de.luckymcdev.foundryengine.common.builder.sound.SoundBuilder;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistryCollectorTest {

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("test", path);
    }

    @Test
    void addItem_GetItems_ContainsBuilder() {
        RegistryCollector collector = new RegistryCollector();
        ItemBuilder builder = ItemBuilder.create(id("test_item"));
        collector.addItem(builder);
        assertTrue(collector.getItems().contains(builder));
        assertEquals(1, collector.getItems().size());
    }

    @Test
    void addBlock_GetBlocks_ContainsBuilder() {
        RegistryCollector collector = new RegistryCollector();
        BlockBuilder builder = BlockBuilder.create(id("test_block"));
        collector.addBlock(builder);
        assertTrue(collector.getBlocks().contains(builder));
    }

    @Test
    void addParticle_GetParticles_ContainsBuilder() {
        RegistryCollector collector = new RegistryCollector();
        ParticleBuilder builder = new ParticleBuilder(id("test_particle"));
        collector.addParticle(builder);
        assertTrue(collector.getParticles().contains(builder));
    }

    @Test
    void addSound_GetSounds_ContainsBuilder() {
        RegistryCollector collector = new RegistryCollector();
        SoundBuilder builder = new SoundBuilder(id("test_sound"));
        collector.addSound(builder);
        assertTrue(collector.getSounds().contains(builder));
    }

    @Test
    void addRecipe_GetRecipes_ContainsBuilder() {
        RegistryCollector collector = new RegistryCollector();
        RecipeBuilder builder = RecipeBuilder.shaped(id("test_recipe"), null);
        collector.addRecipe(builder);
        assertTrue(collector.getRecipes().contains(builder));
    }

    @Test
    void getSoundBuilder_Existing_Returns() {
        RegistryCollector collector = new RegistryCollector();
        SoundBuilder builder = new SoundBuilder(id("my_sound"));
        collector.addSound(builder);
        assertSame(builder, collector.getSoundBuilder(id("my_sound")));
    }

    @Test
    void getSoundBuilder_Missing_ReturnsNull() {
        RegistryCollector collector = new RegistryCollector();
        assertNull(collector.getSoundBuilder(id("missing")));
    }

    @Test
    void getItems_ReturnsUnmodifiable() {
        RegistryCollector collector = new RegistryCollector();
        assertThrows(UnsupportedOperationException.class,
                () -> collector.getItems().add(ItemBuilder.create(id("x"))));
    }

    @Test
    void getBlocks_ReturnsUnmodifiable() {
        RegistryCollector collector = new RegistryCollector();
        assertThrows(UnsupportedOperationException.class,
                () -> collector.getBlocks().add(BlockBuilder.create(id("x"))));
    }

    @Test
    void emptyCollector_AllCollections_Empty() {
        RegistryCollector collector = new RegistryCollector();
        assertTrue(collector.getItems().isEmpty());
        assertTrue(collector.getBlocks().isEmpty());
        assertTrue(collector.getParticles().isEmpty());
        assertTrue(collector.getSounds().isEmpty());
        assertTrue(collector.getRecipes().isEmpty());
    }

    @Test
    void addItem_DuplicateId_Replaces() {
        RegistryCollector collector = new RegistryCollector();
        ItemBuilder first = ItemBuilder.create(id("dup"));
        ItemBuilder second = ItemBuilder.create(id("dup"));
        collector.addItem(first);
        collector.addItem(second);
        assertEquals(1, collector.getItems().size());
        assertTrue(collector.getItems().contains(second));
    }
}
