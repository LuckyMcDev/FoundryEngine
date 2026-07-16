package de.luckymcdev.foundryengine.common.util;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NBTIO {

	public static CompoundTag read(Path path) throws IOException {
		Path resolved = Common.resolveAndValidate(path);
		return NbtIo.read(resolved);
	}

	public static void write(CompoundTag tag, Path path) throws IOException {
		Path resolved = Common.resolveAndValidate(path);
		Files.createDirectories(resolved.getParent());
		NbtIo.write(tag, resolved);
	}
}