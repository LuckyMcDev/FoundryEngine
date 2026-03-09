package io.github.luckymcdev.foundryengine.client.debug.screen;

import io.github.luckymcdev.foundryengine.common.util.QuadConsumer;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * An Inline Implementation of a DebugScreenEntry.
 * For Rendering see {@link io.github.luckymcdev.foundryengine.client.debug.renderer.SimpleDebugScreenRenderer}
 */
public class SimpleDebugScreenEntry implements DebugScreenEntry {

    private final QuadConsumer<DebugScreenDisplayer, Level, LevelChunk, LevelChunk> renderer;

    public SimpleDebugScreenEntry(QuadConsumer<DebugScreenDisplayer, Level, LevelChunk, LevelChunk> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void display(@NonNull DebugScreenDisplayer displayer,
                        @Nullable Level level,
                        @Nullable LevelChunk clientChunk,
                        @Nullable LevelChunk serverChunk) {

        if (level != null) {
            renderer.accept(displayer, level, clientChunk, serverChunk);
        }
    }

    @Override
    public @NonNull DebugEntryCategory category() {
        return DebugEntryCategory.SCREEN_TEXT;
    }
}

