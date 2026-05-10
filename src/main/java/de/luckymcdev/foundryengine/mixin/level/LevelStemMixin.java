package de.luckymcdev.foundryengine.mixin.level;

import de.luckymcdev.foundryengine.interfaces.EngineDimensionOptions;
import net.minecraft.world.level.dimension.LevelStem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelStem.class)
public class LevelStemMixin implements EngineDimensionOptions {
    @Unique
    private boolean engine$save = true;
    @Unique
    private boolean engine$saveProperties = true;

    @Override
    public void engine$setSave(boolean value) {
        this.engine$save = value;
    }

    @Override
    public boolean engine$getSave() {
        return this.engine$save;
    }

    @Override
    public void engine$setSaveProperties(boolean value) {
        this.engine$saveProperties = value;
    }

    @Override
    public boolean engine$getSaveProperties() {
        return this.engine$saveProperties;
    }
}
