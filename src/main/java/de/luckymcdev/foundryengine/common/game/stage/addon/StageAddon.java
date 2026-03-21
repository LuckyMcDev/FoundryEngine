package de.luckymcdev.foundryengine.common.game.stage.addon;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public abstract class StageAddon<T> {

    protected final GenericRegistry<T, Set<String>> requiredStages = new GenericRegistry<>();
    protected final GenericRegistry<T, Component> lockedMessages = new GenericRegistry<>();

    protected Component getMissingStagesMessage(Player player, T object) {
        Set<String> missing = getMissingStages(player, object);
        if (missing.isEmpty()) {
            return Component.empty();
        }

        Component baseMessage = getLockedMessage(object);
        String stagesString = String.join(", ", missing);

        Component stageList = Component.literal(" Required: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(stagesString).withStyle(ChatFormatting.YELLOW));

        return baseMessage.copy().append(stageList);
    }

    public void requireStages(T object, String... stages) {
        requiredStages.register(object, new HashSet<>(List.of(stages)));
    }

    public void requireStages(T object, Component message, String... stages) {
        requireStages(object, stages);
        lockedMessages.register(object, message);
    }

    public boolean canAccess(Player player, T object) {
        Set<String> required = requiredStages.get(object);
        if (required == null || required.isEmpty()) return true;

        for (String stage : required) {
            if (!Common.getGameStageHandler().hasStage(player, stage)) {
                return false;
            }
        }
        return true;
    }

    public Set<String> getMissingStages(Player player, T object) {
        Set<String> required = requiredStages.get(object);
        if (required == null || required.isEmpty()) return Collections.emptySet();

        Set<String> missing = new HashSet<>();
        for (String stage : required) {
            if (!Common.getGameStageHandler().hasStage(player, stage)) {
                missing.add(stage);
            }
        }
        return missing;
    }

    protected Component getLockedMessage(T object) {
        return lockedMessages.getRef(object).orElseGet(this::getDefaultLockedMessage);
    }

    public Collection<T> getGatedObjects() {
        return Collections.unmodifiableCollection(requiredStages.keys());
    }

    public boolean isAccessible(T object) {
        return getRequiredStages(object).isEmpty();
    }

    public Set<String> getRequiredStages(T object) {
        Set<String> stages = requiredStages.get(object);
        return stages == null ? Collections.emptySet() : stages;
    }

    public void clear(T object) {
        requiredStages.remove(object);
        lockedMessages.remove(object);
    }

    protected abstract String getObjectType();

    protected Component getDefaultLockedMessage() {
        return Component.literal("You don't have the required stages to interact with this " + getObjectType() + "!")
                .withStyle(ChatFormatting.RED);
    }
}