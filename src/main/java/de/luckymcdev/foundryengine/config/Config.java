package de.luckymcdev.foundryengine.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class Config {
    public static final CommonConfig COMMON = new CommonConfig();
    public static final ClientConfig CLIENT = new ClientConfig();
    public static final ServerConfig SERVER = new ServerConfig();
    public static final StartupConfig STARTUP = new StartupConfig();

    public static void registerClient(ModContainer container) {
        container.registerConfig(CLIENT.type(), CLIENT.spec());
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    public static void registerServer(ModContainer container) {
        container.registerConfig(SERVER.type(), SERVER.spec());
    }

    public static void registerCommon(ModContainer container) {
        container.registerConfig(COMMON.type(), COMMON.spec());
    }

    public static void registerStartup(ModContainer container) {
        container.registerConfig(STARTUP.type(), STARTUP.spec());
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
    }
}