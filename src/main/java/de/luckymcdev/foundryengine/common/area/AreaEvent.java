package de.luckymcdev.foundryengine.common.area;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

import java.util.List;

public class AreaEvent extends Event {

    private final Area area;
    private final List<Entity> entities;

    public AreaEvent(Area area, List<Entity> entities) {
        this.area = area;
        this.entities = entities;
    }

    public Area getArea() {
        return area;
    }

    public List<Entity> getEntities() {
        return List.copyOf(entities);
    }

    public static class AreaEnterEvent extends AreaEvent {
        public AreaEnterEvent(Area area, List<Entity> entities) {
            super(area, entities);
        }
    }

    public static class AreaLeaveEvent extends AreaEvent {
        public AreaLeaveEvent(Area area, List<Entity> entities) {
            super(area, entities);
        }
    }

    public static class AreaTickEvent extends AreaEvent {
        public AreaTickEvent(Area area, List<Entity> entities) {
            super(area, entities);
        }
    }
}