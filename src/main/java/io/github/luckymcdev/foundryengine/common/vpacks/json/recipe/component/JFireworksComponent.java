package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import java.util.ArrayList;
import java.util.List;

public class JFireworksComponent extends AbstractJComponent {
    public List<JFireworkExplosionComponent> explosions;
    public Integer flightDuration;

    public JFireworksComponent explosion(JFireworkExplosionComponent explosion) {
        if (this.explosions == null) {
            this.explosions = new ArrayList<>();
        }
        this.explosions.add(explosion);
        return this;
    }

    public JFireworksComponent flightDuration(int flightDuration) {
        this.flightDuration = flightDuration;
        return this;
    }
}
