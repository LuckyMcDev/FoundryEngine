package io.github.luckymcdev.foundryengine.client.gui.debug;

import io.github.luckymcdev.foundryengine.common.Common;
import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import io.github.luckymcdev.foundryengine.common.bundle.BundleManager;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BundleDebugEntry implements DebugScreenEntry {
    public static final Identifier GROUP = Common.id("bundles");
    private final BundleManager manager;

    public BundleDebugEntry(BundleManager manager) {
        this.manager = manager;
    }

    @Override
    public void display(@NonNull DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        List<String> infoLines = new ArrayList<>();

        int totalBundles = 0;
        int totalScripts = 0;

        for (Bundle bundle : manager.getBundles()) {
            totalBundles++;
            int scriptCount = bundle.entrypoints().size();
            totalScripts += scriptCount;

            infoLines.add(String.format(" > %s: %d scripts", bundle.info().id(), scriptCount));
        }

        if (totalBundles == 0) {
            infoLines.addFirst("Bundles: None loaded");
        } else {
            infoLines.addFirst(String.format("Bundles: %d loaded (%d total scripts)", totalBundles, totalScripts));
        }

        displayer.addToGroup(GROUP, infoLines);
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return true;
    }

    @Override
    public @NonNull DebugEntryCategory category() {
        return DebugEntryCategory.SCREEN_TEXT;
    }
}