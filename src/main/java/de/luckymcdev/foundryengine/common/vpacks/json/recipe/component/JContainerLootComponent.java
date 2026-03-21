package de.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.Identifier;

public class JContainerLootComponent extends AbstractJComponent {
    @SerializedName("loot_table")
    private final Identifier lootTable;
    private Integer seed;

    public JContainerLootComponent(Identifier lootTable) {
        this.lootTable = lootTable;
    }

    public JContainerLootComponent seed(int seed) {
        this.seed = seed;
        return this;
    }
}
