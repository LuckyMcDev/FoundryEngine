package de.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import java.util.ArrayList;
import java.util.List;

public class JCustomModelDataComponent extends AbstractJComponent {
    private final List<Float> floats = new ArrayList<>();
    private final List<Boolean> flags = new ArrayList<>();
    private final List<String> strings = new ArrayList<>();
    private final List<Integer> colors = new ArrayList<>();

    public JCustomModelDataComponent addFloat(float value) {
        this.floats.add(value);
        return this;
    }

    public JCustomModelDataComponent flag(boolean flag) {
        this.flags.add(flag);
        return this;
    }

    public JCustomModelDataComponent string(String string) {
        this.strings.add(string);
        return this;
    }

    public JCustomModelDataComponent color(int color) {
        this.colors.add(color);
        return this;
    }
}
