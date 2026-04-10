package de.luckymcdev.foundryengine.mixin.screen;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.bundle.compat.BundleListEntry;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforge.client.gui.widget.ModListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModListWidget.class)
public abstract class ModListWidgetMixin {

    @Shadow
    private ModListScreen parent;

    @Inject(method = "refreshList", at = @At("TAIL"))
    private void foundry$appendBundleEntries(CallbackInfo ci) {
        @SuppressWarnings("unchecked")
        AbstractSelectionListAccessor<BundleListEntry> accessor = (AbstractSelectionListAccessor<BundleListEntry>) this;
        for (Bundle bundle : Common.getBundleManager().getBundles()) {
            accessor.invokeAddEntry(new BundleListEntry(bundle, parent));
        }
    }
}