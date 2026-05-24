package de.luckymcdev.foundryengine.common.data.providers.client;

import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.data.providers.EngineProviderExtension;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

import java.util.function.BiConsumer;

public class EngineEquipmentAssetProvider extends EquipmentAssetProvider implements EngineProviderExtension {
    private final Bundle bundle;

    public EngineEquipmentAssetProvider(PackOutput output, Bundle bundle) {
        super(output);
        this.bundle = bundle;
    }

    @Override
    protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
    }

    @Override
    public Bundle bundle() {
        return bundle;
    }
}
