package io.github.luckymcdev.foundryengine.common.bundle.info;

import groovy.util.GroovyScriptEngine;
import net.neoforged.bus.api.IEventBus;

public record Bundle(BundleInfo info, BundleFiles bundleFiles, GroovyScriptEngine scriptEngine, IEventBus eventBus) {

}