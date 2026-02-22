package io.github.luckymcdev.foundryengine.common.script;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

import java.nio.file.Path;

public class ScriptEventBusFactory {
    private static final IEventBus customEventBus = BusBuilder.builder().startShutdown().build();

    public IEventBus getEventBusFor(Path bundleRoot) {
        return NeoForge.EVENT_BUS;
    }
}