package de.luckymcdev.foundryengine.mixin.resource;

import net.minecraft.server.packs.FilePackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FilePackResources.class)
public interface FilePackResourcesAccessor {
	@Accessor("prefix")
	String engine$prefix();

	@Accessor("zipFileAccess")
	FilePackResources.SharedZipFileAccess engine$zipFileAccess();
}
