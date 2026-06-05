package de.luckymcdev.foundryengine.common.bundle;

import de.luckymcdev.foundryengine.interfaces.EngineLevelStorageSource;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.loading.FMLEnvironment;

import java.nio.file.Path;

public class BundleSavePathListener implements BundleLifecycleListener {
    @Override
    public void onBundleLoaded(Bundle bundle) {
        Path saves = bundle.bundleFiles().saves();
        EngineLevelStorageSource.GLOBAL_ADDITIONAL_PATHS.add(saves);
        if (FMLEnvironment.getDist().isClient()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                ((EngineLevelStorageSource) mc.getLevelSource()).engine$addAdditionalPath(saves);
            }
        }
    }

    @Override
    public void onBundleUnloaded(Bundle bundle) {
        Path saves = bundle.bundleFiles().saves();
        EngineLevelStorageSource.GLOBAL_ADDITIONAL_PATHS.remove(saves);
        if (FMLEnvironment.getDist().isClient()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                ((EngineLevelStorageSource) mc.getLevelSource()).engine$removeAdditionalPath(saves);
            }
        }
    }
}
