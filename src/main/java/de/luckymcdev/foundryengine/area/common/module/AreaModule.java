package de.luckymcdev.foundryengine.area.common.module;

import de.luckymcdev.foundryengine.area.common.Area;
import net.minecraft.resources.Identifier;

public interface AreaModule {
	Identifier id();

	default void onAttach(Area area) {
	}

	default void onDetach(Area area) {
	}
}
