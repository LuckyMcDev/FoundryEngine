package de.luckymcdev.foundryengine.common.game.stage.addon;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class StageAddon<T> {

    protected final GenericRegistry<T, Set<Identifier>> requiredStages = new GenericRegistry<>();
    protected final GenericRegistry<T, Component> lockedMessages = new GenericRegistry<>();

    protected Component getMissingStagesMessage(Player player, T object) {
        var missing = getMissingStages(player, object);
        if (missing.isEmpty()) {
            return Component.empty();
        }

        var baseMessage = getLockedMessage(object);
        var stagesString = String.join(", ", missing.stream().map(Identifier::toString).toList());

        var stageList = Component.translatable("foundryengine.stage.required_prefix")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(stagesString).withStyle(ChatFormatting.YELLOW));

        return baseMessage.copy().append(stageList);
    }

    public void requireStages(T object, Identifier... stages) {
        requiredStages.register(object, new HashSet<>(List.of(stages)));
    }

    public void requireStages(T object, Component message, Identifier... stages) {
        requireStages(object, stages);
        lockedMessages.register(object, message);
    }

    public boolean canAccess(Player player, T object) {
        var required = requiredStages.get(object);
        if (required == null || required.isEmpty()) {
            return true;
        }

        for (var stage : required) {
            if (!Common.getGameStageHandler().hasStage(player, stage)) {
                return false;
            }
        }
        return true;
    }

    public Set<Identifier> getMissingStages(Player player, T object) {
        var required = requiredStages.get(object);
        if (required == null || required.isEmpty()) {
            return Collections.emptySet();
        }

        var missing = new HashSet<Identifier>();
        for (var stage : required) {
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

    public Set<Identifier> getRequiredStages(T object) {
        var stages = requiredStages.get(object);
        return stages == null ? Collections.emptySet() : stages;
    }

    public void clear(T object) {
        requiredStages.remove(object);
        lockedMessages.remove(object);
    }

    protected abstract String getObjectType();

    protected Component getDefaultLockedMessage() {
        return Component.translatable("foundryengine.stage.default_locked", getObjectType())
                .withStyle(ChatFormatting.RED);
    }
}
