package de.luckymcdev.foundryengine.common.util;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NBTIO {

	public static CompoundTag read(Path path) throws IOException {
		path = validate(path);
		return NbtIo.read(path);
	}

	public static void write(CompoundTag tag, Path path) throws IOException {
		path = validate(path);
		Files.createDirectories(path.getParent());
		NbtIo.write(tag, path);
	}

	private static Path validate(Path path) {
		path = path.normalize().toAbsolutePath();
		if (!path.startsWith(Common.GAMEDIR)) {
			throw new SecurityException("Path " + path + " is outside the Minecraft directory " + Common.GAMEDIR);
		}
		return path;
	}
}
