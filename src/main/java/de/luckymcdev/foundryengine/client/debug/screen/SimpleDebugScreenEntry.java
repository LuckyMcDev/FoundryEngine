package de.luckymcdev.foundryengine.client.debug.screen;

import de.luckymcdev.foundryengine.client.debug.renderer.SimpleDebugScreenRenderer;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * An Inline Implementation of a DebugScreenEntry.
 * For Rendering see {@link SimpleDebugScreenRenderer}
 */
public class SimpleDebugScreenEntry implements DebugScreenEntry {

    private final DebugEntryRenderer renderer;

    public SimpleDebugScreenEntry(DebugEntryRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void display(@NonNull DebugScreenDisplayer displayer,
                        @Nullable Level level,
                        @Nullable LevelChunk clientChunk,
                        @Nullable LevelChunk serverChunk) {

        if (level != null) {
            renderer.render(displayer, level, clientChunk, serverChunk);
        }
    }

    @Override
    public @NonNull DebugEntryCategory category() {
        return DebugEntryCategory.SCREEN_TEXT;
    }

    @FunctionalInterface
    public interface DebugEntryRenderer {
        void render(
                @NonNull DebugScreenDisplayer displayer,
                @Nullable Level level,
                @Nullable LevelChunk clientChunk,
                @Nullable LevelChunk serverChunk
        );
    }
}

