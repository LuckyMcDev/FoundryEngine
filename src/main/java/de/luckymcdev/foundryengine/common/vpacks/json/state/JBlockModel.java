package de.luckymcdev.foundryengine.common.vpacks.json.state;

import net.minecraft.resources.Identifier;

public class JBlockModel implements Cloneable {
    private final Identifier model;
    private Integer x;
    private Integer y;
    private Boolean uvlock;
    private Integer weight;

    public JBlockModel(String model) {
        this.model = Identifier.withDefaultNamespace(model);
    }

    public JBlockModel(Identifier model) {
        this.model = model;
    }

    /**
     * Rotates the model along the x-axis
     *
     * @param x The amount, in degrees, to rotate. Minecraft only allows rotation
     *          in increments of 90 degrees.
     * @return The current {@link JBlockModel} instance
     */
    public JBlockModel x(int x) {
        this.x = x;
        return this;
    }

    /**
     * Rotates the model along the y-axis
     *
     * @param y The amount, in degrees, to rotate. Minecraft only allows rotation
     *          in increments of 90 degrees.
     * @return The current {@link JBlockModel} instance
     */
    public JBlockModel y(int y) {
        this.y = y;
        return this;
    }

    /**
     * Enables "uvlock"
     * <br />
     * According to the <a href="https://minecraft.wiki/w/Tutorials/Models">Minecraft Wiki</a>
     * uvlock:
     * <blockquote>
     * Locks the rotation of the texutre of a block, if set to true. This way the texture does not rotate with the block when using the x and y-tags[.]
     * </blockquote>
     *
     * @return The current {@link JBlockModel} instance
     */
    public JBlockModel uvlock() {
        this.uvlock = true;
        return this;
    }

    /**
     * Sets the weight
     *
     * @param weight The relative chance the model will be chosen
     * @return The current {@link JBlockModel} instance
     */
    public JBlockModel weight(int weight) {
        this.weight = weight;
        return this;
    }

    @Override
    public JBlockModel clone() {
        try {
            return (JBlockModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}
