package de.luckymcdev.foundryengine.client.editor.panel.tools;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundChangeWeatherPacket;
import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundSetTimePacket;
import imgui.ImGui;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.network.protocol.game.ServerboundSetGameRulePacket;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.List;

public class MinecraftToolsPanel extends EditorPanel {
    public static final MinecraftToolsPanel INSTANCE = new MinecraftToolsPanel();

    private MinecraftToolsPanel() {
        super(new Builder(Common.id("minecraft_tools"))
                .icon(ImIcons.TOOLBOX)
                .category(PanelCategory.TOOLS));
    }

    @Override
    public void content(ImGraphicsExtractor g) {
        if (!requireWorld()) {
            return;
        }

        g.section("Permissions");
        PermissionSet pSet = Client.getPlayer().permissions();
        if (pSet instanceof LevelBasedPermissionSet levelSet) {
            ImGui.text("Access Level: " + levelSet.level().name());
        } else {
            ImGui.text("Permissions: Custom/Unknown Set");
        }

        g.section("Game Mode");
        gameModeSelector(g);

        g.section("Time");
        timeSelector(g);

        g.section("Weather");
        weatherSelector(g);

        g.section("Metrics");
        metrics(g);
    }

    private void metrics(ImGraphicsExtractor g) {
        if (ImGui.collapsingHeader("Minecraft Metrics")) {
            memory();
            ImGui.separator();
            particles();
        }
    }

    private void memory() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;

        ImGui.text("FPS: " + Client.getMc().getFps());
        ImGui.separator();

        ImGui.text(String.format("Used Memory: %dMB (%d%%)",
                toMB(usedMemory),
                (usedMemory * 100 / maxMemory)));

        ImGui.text(String.format("Allocated: %dMB", toMB(totalMemory)));
        ImGui.text(String.format("Max: %dMB", toMB(maxMemory)));

        float usageFraction = (float) usedMemory / maxMemory;
        ImGui.progressBar(usageFraction, -1, 20, "Usage");
    }

    private void particles() {
        ImGui.text("Particles");
        ImGui.text("Count: " + Client.getMc().particleEngine.countParticles());
    }

    private long toMB(long bytes) {
        return bytes / 1024L / 1024L;
    }

    private void timeSelector(ImGraphicsExtractor g) {
        if (Client.getConnection() == null) return;

        ImGui.text("Time Selector");

        if (ImGui.button("Day")) {
            Client.getConnection().send(new ServerBoundSetTimePacket(1000));
        }
        ImGui.sameLine();
        if (ImGui.button("Noon")) {
            Client.getConnection().send(new ServerBoundSetTimePacket(6000));
        }
        ImGui.sameLine();
        if (ImGui.button("Night")) {
            Client.getConnection().send(new ServerBoundSetTimePacket(13000));
        }
        ImGui.sameLine();
        if (ImGui.button("Midnight")) {
            Client.getConnection().send(new ServerBoundSetTimePacket(18000));
        }
        ImGui.sameLine();
        if (ImGui.button("Lock " + ImGraphicsExtractor.icon(ImIcons.LOCK) + "##time")) {
            BuiltInRegistries.GAME_RULE.getResourceKey(GameRules.ADVANCE_TIME).ifPresent(key -> {
                var entry = new ServerboundSetGameRulePacket.Entry(key, "false");
                Client.getConnection().send(new ServerboundSetGameRulePacket(List.of(entry)));
            });
        }
        if (ImGui.isItemHovered()) ImGui.setTooltip("Stops the daylight cycle");
    }

    private void weatherSelector(ImGraphicsExtractor g) {
        if (Client.getConnection() == null) return;
        ImGui.text("Weather");

        if (ImGui.button("Clear")) {
            Client.getConnection().send(new ServerBoundChangeWeatherPacket("clear"));
        }
        ImGui.sameLine();
        if (ImGui.button("Rain")) {
            Client.getConnection().send(new ServerBoundChangeWeatherPacket("rain"));
        }
        ImGui.sameLine();
        if (ImGui.button("Thunder")) {
            Client.getConnection().send(new ServerBoundChangeWeatherPacket("thunder"));
        }
        ImGui.sameLine();
        if (ImGui.button("Lock " + ImGraphicsExtractor.icon(ImIcons.LOCK) + "##weather")) {
            BuiltInRegistries.GAME_RULE.getResourceKey(GameRules.ADVANCE_WEATHER).ifPresent(key -> {
                var entry = new ServerboundSetGameRulePacket.Entry(key, "false");
                Client.getConnection().send(new ServerboundSetGameRulePacket(List.of(entry)));
            });
        }
        if (ImGui.isItemHovered()) ImGui.setTooltip("Stops the weather cycle");
    }

    private void gameModeSelector(ImGraphicsExtractor g) {
        if (Client.getConnection() == null) return;

        ImGui.text("Game mode");

        if (ImGui.button("Creative")) {
            Client.getConnection().send(new ServerboundChangeGameModePacket(GameType.CREATIVE));
        }
        ImGui.sameLine();
        if (ImGui.button("Survival")) {
            Client.getConnection().send(new ServerboundChangeGameModePacket(GameType.SURVIVAL));
        }
        ImGui.sameLine();
        if (ImGui.button("Adventure")) {
            Client.getConnection().send(new ServerboundChangeGameModePacket(GameType.ADVENTURE));
        }
        ImGui.sameLine();
        if (ImGui.button("Spectator")) {
            Client.getConnection().send(new ServerboundChangeGameModePacket(GameType.SPECTATOR));
        }
    }
}
