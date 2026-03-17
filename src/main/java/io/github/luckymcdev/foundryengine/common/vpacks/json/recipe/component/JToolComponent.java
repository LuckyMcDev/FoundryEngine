package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import net.minecraft.world.item.component.Tool;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JToolComponent extends JCodecBuilderComponent<Tool> {
    private final List<Tool.Rule> rules = new ArrayList<>();
    private float defaultMiningSpeed;
    private int damagePerBlock;
    private boolean canDestroyBlocksInCreative;

    /**
     * Creates a {@link Tool} through the builder pattern. There shouldn't be a reason why you need
     * to call {@link #manuallyBuild()}, but it is public if you need to do so.
     * <br />
     * If you're going to be using all the fields on {@link Tool}, you are encouraged to use the
     * constructor overload that takes a {@link Tool} instance. This will merge the rules from
     * {@link JToolComponent} and the provided {@link Tool} together, while keeping all the other values
     * from the {@link Tool}.
     */
    public JToolComponent() {
        super(Tool.CODEC);
        this.defaultMiningSpeed = 1.0f;
        this.damagePerBlock = 1;
        this.canDestroyBlocksInCreative = true;
    }

    /**
     * This overload should only be called if you want to merge {@link Tool.Rule}s provided to
     * {@link JToolComponent} with an existing {@link Tool}.
     *
     * @param tool The {@link Tool} rules will be merged with.
     */
    public JToolComponent(@NotNull Tool tool) {
        super(Tool.CODEC);
        this.object = tool;
    }

    public JToolComponent defaultMiningSpeed(float speed) {
        this.defaultMiningSpeed = speed;
        return this;
    }

    public JToolComponent damagePerBlock(int damage) {
        this.damagePerBlock = damage;
        return this;
    }

    public JToolComponent canDestroyBlocksInCreative(boolean canDestroy) {
        this.canDestroyBlocksInCreative = canDestroy;
        return this;
    }

    public JToolComponent rule(Tool.Rule rule) {
        this.rules.add(rule);
        return this;
    }

    @Override
    protected Tool build() {
        if (this.object != null) {
            this.object.rules().addAll(this.rules);
        }
        return super.build();
    }

    @Override
    public Tool manuallyBuild() {
        return new Tool(this.rules, this.defaultMiningSpeed, this.damagePerBlock, this.canDestroyBlocksInCreative);
    }
}
