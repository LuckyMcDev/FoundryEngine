package de.luckymcdev.foundryengine.common.event;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.dialogue.DialogueEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

public final class DialogueEvents {
    public static final EventGroupHolder<DialogueEvent.Started> STARTED = new EventGroupHolder<>();
    public static final EventGroupHolder<DialogueEvent.Advanced> ADVANCED = new EventGroupHolder<>();
    public static final EventGroupHolder<DialogueEvent.OptionSelected> OPTION_SELECTED = new EventGroupHolder<>();
    public static final EventGroupHolder<DialogueEvent.Ended> ENDED = new EventGroupHolder<>();

    private DialogueEvents() {
    }

    public static void onStarted(EventCallback<DialogueEvent.Started> cb) {
        STARTED.register(cb);
    }

    public static void onAdvanced(EventCallback<DialogueEvent.Advanced> cb) {
        ADVANCED.register(cb);
    }

    public static void onOptionSelected(EventCallback<DialogueEvent.OptionSelected> cb) {
        OPTION_SELECTED.register(cb);
    }

    public static void onEnded(EventCallback<DialogueEvent.Ended> cb) {
        ENDED.register(cb);
    }

    @ApiStatus.Internal
    public static class Internal {
        static {
            Common.registerEventClear(Internal::clear);
        }

        public static void postOnStarted(DialogueEvent.Started event) {
            STARTED.post(event);
        }

        public static void postOnAdvanced(DialogueEvent.Advanced event) {
            ADVANCED.post(event);
        }

        public static void postOnOptionSelected(DialogueEvent.OptionSelected event) {
            OPTION_SELECTED.post(event);
        }

        public static void postOnEnded(DialogueEvent.Ended event) {
            ENDED.post(event);
        }

        public static void register() {
            NeoForge.EVENT_BUS.addListener(Internal::postOnStarted);
            NeoForge.EVENT_BUS.addListener(Internal::postOnAdvanced);
            NeoForge.EVENT_BUS.addListener(Internal::postOnEnded);
            NeoForge.EVENT_BUS.addListener(Internal::postOnOptionSelected);
        }

        public static void clear() {
            STARTED.clear();
            ADVANCED.clear();
            OPTION_SELECTED.clear();
            ENDED.clear();
        }
    }
}
