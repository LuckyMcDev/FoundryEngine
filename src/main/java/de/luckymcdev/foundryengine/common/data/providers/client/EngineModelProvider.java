package de.luckymcdev.foundryengine.common.data.providers.client;

import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;

import java.util.List;

public class EngineModelProvider extends ModelProvider {
    private final String namespace;
    private final List<BlockBuilder> blockBuilders;
    private final List<ItemBuilder> itemBuilders;

    public EngineModelProvider(PackOutput output, String namespace, List<BlockBuilder> blockBuilders, List<ItemBuilder> itemBuilders) {
        super(output, namespace);
        this.namespace = namespace;
        this.blockBuilders = blockBuilders;
        this.itemBuilders = itemBuilders;
    }

    @Override
    public void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        for (BlockBuilder builder : blockBuilders) {
            blockModels.createTrivialCube(builder.get());
        }

        for (ItemBuilder builder : itemBuilders) {
            var item = builder.get();
            if (item instanceof BlockItem) continue;
            itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }
    }
}
