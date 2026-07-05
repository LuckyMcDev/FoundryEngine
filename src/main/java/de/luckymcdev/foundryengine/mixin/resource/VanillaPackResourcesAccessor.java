package de.luckymcdev.foundryengine.mixin.resource;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Mixin(VanillaPackResources.class)
public interface VanillaPackResourcesAccessor {
	@Accessor("pathsForType")
	Map<PackType, List<Path>> engine$getPathsForType();
}
