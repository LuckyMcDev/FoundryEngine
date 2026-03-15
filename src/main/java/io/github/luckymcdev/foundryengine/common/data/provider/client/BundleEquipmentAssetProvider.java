package io.github.luckymcdev.foundryengine.common.data.provider.client;

import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

import java.util.function.BiConsumer;

public class BundleEquipmentAssetProvider extends EquipmentAssetProvider {

    public BundleEquipmentAssetProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
        // This is empty, as there is currently no easy way to generate this for each bundle / i haven't gotten around to it.
    }
}
