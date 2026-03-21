package de.luckymcdev.foundryengine.client.debug.screen;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Debug Entry to display info about owned Game Stages.
 */
public class GameStagesDebugEntry implements DebugScreenEntry {
    public static final Identifier GROUP = Common.id("gamestages");

    @Override
    public void display(@NonNull DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        List<String> infoLines = new ArrayList<>();
        Player player = Client.getPlayer();
        if (null == player) {
            infoLines.add("None, no Player exists.");
            displayer.addToGroup(GROUP, infoLines);
            return;
        }

        Set<String> stages = player.getData(Common.getGameStageHandler().ATTACHMENT);

        infoLines.addFirst("Stages:");
        infoLines.add(Arrays.toString(stages.toArray()));

        infoLines.forEach(displayer::addLine);
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