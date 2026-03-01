package io.github.luckymcdev.foundryengine.common.data.provider.client;

import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;

public class BundleModelProvider extends ModelProvider {
    private final Bundle bundle;

    public BundleModelProvider(PackOutput output, Bundle bundle) {
        super(output, bundle.info().id());
        this.bundle = bundle;
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        bundle.registryQuery().getBlocks().forEach(blockModels::createTrivialCube);

        bundle.registryQuery().getItems().forEach(item -> {
            if (!(item instanceof BlockItem)) {
                itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
            }
        });
    }
}