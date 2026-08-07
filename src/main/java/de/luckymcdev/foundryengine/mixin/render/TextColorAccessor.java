package de.luckymcdev.foundryengine.mixin.render;

import de.luckymcdev.foundryengine.common.exceptions.NoMixinException;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TextColor.class)
public interface TextColorAccessor {
	@Accessor("NAMED_COLORS")
	static Map<String, TextColor> engine$getNamedColors() {
		throw new NoMixinException("TextColorAccessor");
	}
}
