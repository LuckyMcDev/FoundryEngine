package io.github.luckymcdev.foundryengine.common.game.stage;

import com.mojang.serialization.Codec;
import groovyjarjarantlr4.v4.runtime.misc.Nullable;
import io.github.luckymcdev.foundryengine.common.Common;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class GameStageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameStageHandler.class);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Common.MODID);
    private static final Codec<Set<String>> STRING_SET_CODEC = Codec.STRING.listOf().xmap(
            HashSet::new,
            ArrayList::new
    );
    public static final Supplier<AttachmentType<Set<String>>> PLAYER_STAGES = ATTACHMENT_TYPES.register(
            "player_stages",
            () -> AttachmentType.builder(() -> (Set<String>) new HashSet<String>())
                    .serialize(STRING_SET_CODEC.fieldOf("stages"))
                    .copyOnDeath()
                    .sync(new AttachmentSyncHandler<>() {
                        @Override
                        public void write(@NonNull RegistryFriendlyByteBuf buf, Set<String> attachment, boolean initialSync) {
                            buf.writeCollection(attachment, FriendlyByteBuf::writeUtf);
                        }

                        @Override
                        public Set<String> read(@NonNull IAttachmentHolder holder, @NonNull RegistryFriendlyByteBuf buf, @Nullable Set<String> previousValue) {
                            return new HashSet<>(buf.readCollection(HashSet::new, FriendlyByteBuf::readUtf));
                        }
                    })
                    .build()
    );

    public static void register(IEventBus modEventbus) {
        LOGGER.debug("Registered {} Attachments", Common.MODNAME);
        ATTACHMENT_TYPES.register(modEventbus);
    }

    /**
     * Adds a stage to the player.
     *
     * @return true if the stage was added, false if they already had it.
     */
    public static boolean addStage(Player player, String stage) {
        if (hasStage(player, stage)) return false;
        GameStageEvent.Add event = new GameStageEvent.Add(player, stage);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return false;

        Set<String> newStages = new HashSet<>(player.getData(PLAYER_STAGES));
        if (newStages.add(stage)) {
            player.setData(PLAYER_STAGES, newStages);

            NeoForge.EVENT_BUS.post(new GameStageEvent.Added(player, stage));
            return true;
        }
        return false;
    }

    /**
     * Removes a stage from the player.
     *
     * @return true if the stage was removed, false if they didn't have it.
     */
    public static boolean removeStage(Player player, String stage) {
        if (!hasStage(player, stage)) return false;

        GameStageEvent.Remove event = new GameStageEvent.Remove(player, stage);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return false;

        Set<String> newStages = new HashSet<>(player.getData(PLAYER_STAGES));
        if (newStages.remove(stage)) {
            player.setData(PLAYER_STAGES, newStages);

            NeoForge.EVENT_BUS.post(new GameStageEvent.Removed(player, stage));
            return true;
        }
        return false;
    }

    public static boolean hasStage(Player player, String stage) {
        return player.getData(PLAYER_STAGES).contains(stage);
    }

    public static Set<String> getStages(Player player) {
        return Collections.unmodifiableSet(player.getData(PLAYER_STAGES));
    }
}