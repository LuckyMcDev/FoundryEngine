package de.luckymcdev.foundryengine.mixin.data;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.luckymcdev.foundryengine.server.packs.VirtualPackImpl;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Pack.class)
public class PackMixin {
    @WrapOperation(
            method = "readPackMetadata",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/PackResources;getMetadataSection(Lnet/minecraft/server/packs/metadata/MetadataSectionType;)Ljava/lang/Object;")
    )
    private static <T> @Nullable T engine$readPackMetadata(PackResources packResources,
                                                           MetadataSectionType<@NotNull T> metadataSectionType,
                                                           Operation<PackMetadataSection> original,
                                                           PackLocationInfo packLocationInfo,
                                                           Pack.ResourcesSupplier resourcesSupplier,
                                                           PackFormat packFormat,
                                                           PackType packType
    ) {
        if (packResources instanceof VirtualPackImpl virtualPack && metadataSectionType.name().equals("pack")) {
            return virtualPack.getMetadataSection(metadataSectionType, packFormat);
        } else {
            return (T) original.call(packResources, metadataSectionType);
        }
    }
}
