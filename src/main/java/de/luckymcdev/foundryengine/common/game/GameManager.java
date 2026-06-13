package de.luckymcdev.foundryengine.common.game;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.bundle.BundleLifecycleListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager implements BundleLifecycleListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<Identifier, GameSession> sessions = new ConcurrentHashMap<>();

    public void register(GameSession session) {
        Identifier id = session.id();
        if (sessions.putIfAbsent(id, session) != null) {
            LOGGER.warn("Game session [{}] is already registered", id);
            return;
        }
        LOGGER.debug("Registered game session [{}]", id);
    }

    public void unregister(Identifier id) {
        GameSession session = sessions.remove(id);
        if (session != null && session.isRunning()) {
            stopSession(id);
        }
        LOGGER.debug("Unregistered game session [{}]", id);
    }

    public GameSession getSession(Identifier id) {
        return sessions.get(id);
    }

    public Collection<GameSession> getActiveSessions() {
        return Collections.unmodifiableCollection(sessions.values());
    }

    public boolean hasSession(Identifier id) {
        return sessions.containsKey(id);
    }

    public boolean startSession(Identifier id) {
        GameSession session = sessions.get(id);
        if (session == null) {
            LOGGER.warn("Cannot start unknown session [{}]", id);
            return false;
        }
        if (session.state() != GameState.STOPPED) return false;

        GameSessionEvent.Starting startingEvent = new GameSessionEvent.Starting(id);
        if (NeoForge.EVENT_BUS.post(startingEvent).isCanceled()) return false;

        session.state(GameState.STARTING);
        session.load();
        session.onStarting();
        session.state(GameState.RUNNING);

        NeoForge.EVENT_BUS.post(new GameSessionEvent.Started(id));
        LOGGER.debug("Started game session [{}]", id);
        return true;
    }

    public boolean stopSession(Identifier id) {
        GameSession session = sessions.get(id);
        if (session == null) return false;
        if (session.state() != GameState.RUNNING && session.state() != GameState.STARTING) return false;

        GameSessionEvent.Stopping stoppingEvent = new GameSessionEvent.Stopping(id);
        if (NeoForge.EVENT_BUS.post(stoppingEvent).isCanceled()) return false;

        session.state(GameState.STOPPING);
        session.onStopping();
        session.save();
        session.state(GameState.STOPPED);

        NeoForge.EVENT_BUS.post(new GameSessionEvent.Stopped(id));
        LOGGER.debug("Stopped game session [{}]", id);
        return true;
    }

    public void stopAll() {
        for (Identifier id : sessions.keySet()) {
            stopSession(id);
        }
    }

    public void tickCommon(Level level) {
        for (GameSession session : sessions.values()) {
            if (session.state().isRunning()) {
                session.onCommonTick(level);
            }
        }
    }

    public void tickClient(Minecraft client, ClientLevel level) {
        for (GameSession session : sessions.values()) {
            if (session.state().isRunning()) {
                session.onClientTick(client, level);
            }
        }
    }

    public void tickServer(MinecraftServer server, ServerLevel level) {
        for (GameSession session : sessions.values()) {
            if (session.state().isRunning()) {
                session.onServerTick(server, level);
            }
        }
    }

    @Override
    public void onBundlePreUnload(Bundle bundle) {
        for (Map.Entry<Identifier, GameSession> entry : sessions.entrySet()) {
            Identifier id = entry.getKey();
            if (id.getNamespace().equals(bundle.info().id())) {
                stopSession(id);
            }
        }
    }

    @Override
    public void onBundleReloadStarted() {
        stopAll();
        sessions.clear();
    }
}
