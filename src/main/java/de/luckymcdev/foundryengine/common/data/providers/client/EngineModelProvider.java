package de.luckymcdev.foundryengine.common.data.providers.client;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;

public class EngineModelProvider extends ModelProvider {

    public EngineModelProvider(PackOutput output, String modId) {
        super(output, modId);
    }

    @Override
    public void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    }
}
