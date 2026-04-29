package de.luckymcdev.foundryengine.mixin.data;

import de.luckymcdev.foundryengine.common.vpacks.event.RegisterVirtualPackEvent;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Mixin(FolderRepositorySource.class)
public class FolderRepositorySourceMixin {
    @Shadow
    @Final
    private PackType packType;

    @Inject(at = @At("HEAD"), method = "loadPacks")
    private void engine$loadPacks(Consumer<Pack> pOnLoad, CallbackInfo ci) {
        List<PackResources> packs = NeoForge.EVENT_BUS.post(new RegisterVirtualPackEvent.BeforeUser()).getPacks();
        for (PackResources pack : packs) {
            pOnLoad.accept(Objects.requireNonNull(Pack.readMetaAndCreate(pack.location(), new PackResourcesSupplier(pack),
                    this.packType,
                    new PackSelectionConfig(true, Pack.Position.TOP, false)
            )));
        }
    }

    private record PackResourcesSupplier(PackResources packResources) implements Pack.ResourcesSupplier {
        @Override
        public PackResources openPrimary(PackLocationInfo pLocation) {
            return this.packResources;
        }

        @Override
        public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
            return this.packResources;
        }
    }
}
