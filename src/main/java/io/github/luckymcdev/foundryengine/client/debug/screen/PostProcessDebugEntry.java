package io.github.luckymcdev.foundryengine.client.debug.screen;

import groovyjarjarantlr4.v4.runtime.misc.Nullable;
import io.github.luckymcdev.foundryengine.client.post.PostProcessManager;
import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class PostProcessDebugEntry implements DebugScreenEntry {
    public static final Identifier GROUP = Common.id("post_processing");
    private final PostProcessManager manager;

    public PostProcessDebugEntry(PostProcessManager manager) {
        this.manager = manager;
    }

    @Override
    public void display(@NonNull DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        List<String> lines = new ArrayList<>();
        List<PostProcessPipeline> active = manager.getEnabledPipelines();

        lines.add(String.format("Active Post Processing: %d", active.size()));
        for (PostProcessPipeline p : active) {
            lines.add(String.format(" > %s [%s]", p.getName(), p.getStage()));
        }

        displayer.addToGroup(GROUP, lines);
    }

    @Override
    public @NonNull DebugEntryCategory category() {
        return DebugEntryCategory.SCREEN_TEXT;
    }
}
