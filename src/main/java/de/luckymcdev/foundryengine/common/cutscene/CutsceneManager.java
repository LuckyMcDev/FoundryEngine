package de.luckymcdev.foundryengine.common.cutscene;

import de.luckymcdev.foundryengine.client.editor.feature.CutsceneEditorFeature;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.cutscene.storage.CutsceneSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Canonical cutscene definition store.
 * <p>
 * Server-side: loads/persists via {@link CutsceneSavedData} and acts as the single API for modifying cutscenes.
 * Client-side: receives synced NBT via {@link de.luckymcdev.foundryengine.common.savedata.SavedDataManager} and
 * keeps the current dimension's cutscene list for rendering/editor tooling.
 */
public class CutsceneManager {
    private final Map<ResourceKey<Level>, List<Cutscene>> cutscenesByDimension = new HashMap<>();

    public boolean isLoaded(ResourceKey<Level> dimension) {
        return cutscenesByDimension.containsKey(dimension);
    }

    public void loadFromLevel(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();
        replaceAll(dimension, CutsceneSavedData.get(level).getCutscenes());
    }

    public void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            loadFromLevel(level);
        }
    }

    /**
     * Returns the current cutscene list for {@code dimension}. The returned list should be treated as read-only.
     */
    public List<Cutscene> getCutscenes(ResourceKey<Level> dimension) {
        return cutscenesByDimension.getOrDefault(dimension, Collections.emptyList());
    }

    public void replaceAll(ResourceKey<Level> dimension, List<Cutscene> cutscenes) {
        cutscenesByDimension.put(dimension, new ArrayList<>(cutscenes));
    }

    public @Nullable Cutscene find(ResourceKey<Level> dimension, String name) {
        for (Cutscene c : getCutscenes(dimension)) {
            if (c.getName().equals(name)) return c;
        }
        return null;
    }

    public Collection<Identifier> getSuggestions(ServerLevel level) {
        if (!isLoaded(level.dimension())) {
            loadFromLevel(level);
        }
        ArrayList<Identifier> out = new ArrayList<>();
        for (Cutscene c : getCutscenes(level.dimension())) {
            out.add(Common.id(c.getName()));
        }
        return out;
    }

    /**
     * Applies a full NBT replacement (the same shape as {@link CutsceneEditorFeature#toNbt()}).
     */
    public void applyFullNbt(ServerLevel level, CompoundTag tag) {
        CutsceneSavedData.get(level).setData(tag);
        replaceAll(level.dimension(), CutsceneSavedData.makeList(tag));
    }

    /**
     * Client-side: applies a synced cutscene list for {@code dimension}.
     */
    public void applySync(ResourceKey<Level> dimension, CompoundTag tag) {
        replaceAll(dimension, CutsceneSavedData.makeList(tag));
    }

    /**
     * Serializes the current cutscenes for {@code level} into the canonical "CutsceneList" format.
     */
    public CompoundTag toNbt(ServerLevel level) {
        if (!isLoaded(level.dimension())) {
            loadFromLevel(level);
        }
        CompoundTag tag = new CompoundTag();
        var list = new net.minecraft.nbt.ListTag();
        for (Cutscene cutscene : getCutscenes(level.dimension())) {
            list.add(cutscene.toNbt());
        }
        tag.put("CutsceneList", list);
        return tag;
    }

    public boolean add(ServerLevel level, Cutscene cutscene) {
        ResourceKey<Level> dim = level.dimension();
        var list = new ArrayList<>(getCutscenes(dim));
        if (list.stream().anyMatch(c -> c.getName().equals(cutscene.getName()))) return false;
        list.add(cutscene);
        CutsceneSavedData.get(level).setCutscenes(list);
        replaceAll(dim, list);
        return true;
    }

    public boolean remove(ServerLevel level, String name) {
        ResourceKey<Level> dim = level.dimension();
        var list = new ArrayList<>(getCutscenes(dim));
        boolean removed = list.removeIf(c -> c.getName().equals(name));
        if (!removed) return false;
        CutsceneSavedData.get(level).setCutscenes(list);
        replaceAll(dim, list);
        return true;
    }

    public void clear(ServerLevel level) {
        CutsceneSavedData.get(level).setData(new CompoundTag());
        replaceAll(level.dimension(), List.of());
    }

    public void syncToPlayer(ServerPlayer player) {
        Common.getSavedDataManager().syncToPlayer(player);
    }

    public void syncToDimension(ServerLevel level) {
        Common.getSavedDataManager().syncToDimension(level);
    }

    public void persist(ServerLevel level) {
        ResourceKey<Level> dim = level.dimension();
        CutsceneSavedData.get(level).setCutscenes(new ArrayList<>(getCutscenes(dim)));
    }
}
