package de.luckymcdev.foundryengine.common.vpacks.json.recipe.component.consumable;

public class JTeleportRandomly extends JConsumeEffect {
    private Integer diameter;

    public JTeleportRandomly() {
        super("minecraft:teleport_randomly");
    }

    public JTeleportRandomly diameter(int diameter) {
        this.diameter = diameter;
        return this;
    }
}
