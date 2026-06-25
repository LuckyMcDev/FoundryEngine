package de.luckymcdev.foundryengine.common.dialogue;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus;

/**
 * Events fired during dialogue lifecycle on the server.
 * Subscribe via the static {@code started/advanced/optionSelected/ended} methods
 * or call {@link Internal#register(IEventBus)} for NeoForge bus integration.
 */
public class DialogueEvents {
    public static final EventGroupHolder<Started> STARTED = new EventGroupHolder<>();
    public static final EventGroupHolder<Advanced> ADVANCED = new EventGroupHolder<>();
    public static final EventGroupHolder<OptionSelected> OPTION_SELECTED = new EventGroupHolder<>();
    public static final EventGroupHolder<Ended> ENDED = new EventGroupHolder<>();

    public static void started(EventCallback<Started> cb) { STARTED.register(cb); }
    public static void advanced(EventCallback<Advanced> cb) { ADVANCED.register(cb); }
    public static void optionSelected(EventCallback<OptionSelected> cb) { OPTION_SELECTED.register(cb); }
    public static void ended(EventCallback<Ended> cb) { ENDED.register(cb); }

    public record Started(ServerPlayer player, DialogueSession session) {}
    public record Advanced(ServerPlayer player, DialogueSession session, String fromNodeId, String toNodeId) {}
    public record OptionSelected(ServerPlayer player, DialogueSession session, DialogueOption option) {}
    public record Ended(ServerPlayer player, DialogueSession session) {}

    @ApiStatus.Internal
    public static class Internal {
        static {
            Common.registerEventClear(Internal::clear);
        }

        public static void register(IEventBus bus) {
        }

        public static void postStarted(Started e) { STARTED.post(e); }
        public static void postAdvanced(Advanced e) { ADVANCED.post(e); }
        public static void postOptionSelected(OptionSelected e) { OPTION_SELECTED.post(e); }
        public static void postEnded(Ended e) { ENDED.post(e); }

        public static void clear() {
            STARTED.clear();
            ADVANCED.clear();
            OPTION_SELECTED.clear();
            ENDED.clear();
        }
    }
}
