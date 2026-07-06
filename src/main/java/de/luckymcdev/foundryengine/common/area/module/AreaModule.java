package de.luckymcdev.foundryengine.common.area.module;

import de.luckymcdev.foundryengine.common.area.Area;
import net.minecraft.resources.Identifier;

public interface AreaModule {
	Identifier id();

	default void onAttach(Area area) {
	}

	default void onDetach(Area area) {
	}
}
