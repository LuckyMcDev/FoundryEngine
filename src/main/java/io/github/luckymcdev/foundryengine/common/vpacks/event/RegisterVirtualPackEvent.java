
package io.github.luckymcdev.foundryengine.common.vpacks.event;

import net.minecraft.server.packs.PackResources;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;

public abstract class RegisterVirtualPackEvent extends Event {
    private final List<PackResources> packs = new ArrayList<>();

    public void addPack(PackResources pack) {
        this.packs.add(pack);
    }

    public List<PackResources> getPacks() {
        return this.packs;
    }

    public static class BeforeUser extends RegisterVirtualPackEvent {
    }
}
