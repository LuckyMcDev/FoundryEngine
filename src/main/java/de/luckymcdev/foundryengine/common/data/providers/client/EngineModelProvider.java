package de.luckymcdev.foundryengine.common.data.providers.client;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.bundle.registry.BundleRegistryQuery;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class EngineModelProvider extends ModelProvider {
    private final String bundleId;

    public EngineModelProvider(PackOutput output, String modId) {
        super(output, modId);
        this.bundleId = modId;
    }

    @Override
    public void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Bundle bundle = Common.getBundleManager().getBundle(bundleId);
        BundleRegistryQuery query = bundle.registryQuery();

        for (Block block : query.getBlocks()) {
            blockModels.createTrivialCube(block);
        }

        for (Item item : query.getItems()) {
            if (item instanceof BlockItem bi && query.getBlocks().contains(bi.getBlock())) {
                continue;
            }
            itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }
    }
}
