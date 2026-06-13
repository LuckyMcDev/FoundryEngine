package de.luckymcdev.foundryengine.common.game;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GameSession {
    final GameData data;
    private final Identifier id;
    private GameState state = GameState.STOPPED;

    private Consumer<Level> commonTickHandler = level -> {
    };
    private BiConsumer<Minecraft, ClientLevel> clientTickHandler = (mc, level) -> {
    };
    private BiConsumer<MinecraftServer, ServerLevel> serverTickHandler = (server, level) -> {
    };
    private Runnable startingHandler = () -> {
    };
    private Runnable stoppingHandler = () -> {
    };

    public GameSession(Identifier id, GameData data) {
        this.id = id;
        this.data = data;
    }

    public Identifier id() {
        return id;
    }

    public GameData data() {
        return data;
    }

    public GameState state() {
        return state;
    }

    void state(GameState state) {
        this.state = state;
    }

    public void onCommonTick(Level level) {
        commonTickHandler.accept(level);
    }

    public void onClientTick(Minecraft client, ClientLevel level) {
        clientTickHandler.accept(client, level);
    }

    public void onServerTick(MinecraftServer server, ServerLevel level) {
        serverTickHandler.accept(server, level);
    }

    public void onStarting() {
        startingHandler.run();
    }

    public void onStopping() {
        stoppingHandler.run();
    }

    public GameSession onCommonTick(Consumer<Level> handler) {
        this.commonTickHandler = handler;
        return this;
    }

    public GameSession onClientTick(BiConsumer<Minecraft, ClientLevel> handler) {
        this.clientTickHandler = handler;
        return this;
    }

    public GameSession onServerTick(BiConsumer<MinecraftServer, ServerLevel> handler) {
        this.serverTickHandler = handler;
        return this;
    }

    public GameSession onStarting(Runnable handler) {
        this.startingHandler = handler;
        return this;
    }

    public GameSession onStopping(Runnable handler) {
        this.stoppingHandler = handler;
        return this;
    }

    public void load() {
        data.loadFrom(Common.GAME);
    }

    public void save() {
        data.saveTo(Common.GAME);
    }

    public boolean isRunning() {
        return state.isRunning();
    }
}
