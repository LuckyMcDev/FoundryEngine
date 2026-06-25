package de.luckymcdev.foundryengine.common.dialogue;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-dimension {@link SavedData} for dialogue trees.
 * Persists and restores the full tree set to disk.
 */
public class DialogueSavedData extends SavedData {
    public static final SavedDataType<DialogueSavedData> TYPE = new SavedDataType<>(
            Common.id("dialogue_trees"),
            DialogueSavedData::new,
            CompoundTag.CODEC.xmap(DialogueSavedData::new, DialogueSavedData::getData),
            DataFixTypes.LEVEL
    );

    private CompoundTag data = new CompoundTag();

    public DialogueSavedData() {
    }

    public DialogueSavedData(CompoundTag tag) {
        this.data = tag;
    }

    public static DialogueSavedData get(ServerLevel level) {
        SavedDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }

    public static List<DialogueTree> makeTrees(CompoundTag tag) {
        List<DialogueTree> trees = new ArrayList<>();
        var list = tag.getListOrEmpty("DialogueTrees");
        for (int i = 0; i < list.size(); i++) {
            var entry = list.getCompoundOrEmpty(i);
            var id = Identifier.parse(entry.getStringOr("Id", "foundryengine:empty"));
            trees.add(DialogueTree.fromNbt(id, entry));
        }
        return trees;
    }

    public CompoundTag getData() {
        return data;
    }

    public void setData(CompoundTag tag) {
        this.data = tag;
        this.setDirty();
    }

    public List<DialogueTree> getTrees() {
        return makeTrees(this.data);
    }

    public void setTrees(List<DialogueTree> trees) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (var tree : trees) {
            var entry = tree.toNbt();
            entry.putString("Id", tree.getId().toString());
            list.add(entry);
        }
        tag.put("DialogueTrees", list);
        this.data = tag;
        this.setDirty();
    }
}
