package de.luckymcdev.foundryengine.client.post;

import net.minecraft.resources.Identifier;

public record PrioritizedEffect(Identifier id, int priority) implements Comparable<PrioritizedEffect> {
    @Override
    public int compareTo(PrioritizedEffect other) {
        int res = Integer.compare(this.priority, other.priority);
        return res == 0 ? this.id.compareTo(other.id) : res;
    }
}
