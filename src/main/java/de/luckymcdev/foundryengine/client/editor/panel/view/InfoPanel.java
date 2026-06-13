package de.luckymcdev.foundryengine.client.editor.panel.view;

import de.luckymcdev.foundryengine.FoundryEngineMod;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import net.minecraft.SharedConstants;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS;

public class InfoPanel extends EditorPanel {
    public static final InfoPanel INSTANCE = new InfoPanel();

    protected InfoPanel() {
        super(Common.id("info"), "Info", ImIcons.FA.FA_INFO, PanelCategory.VIEW);
    }

    @Override
    public void content() {
        ImGuiUtils.labeledValue("Foundry Engine", FoundryEngineMod.modVersion.toString());
        ImGui.separator();

        ImGuiUtils.collapse(ImIcons.FA.FA_MINUS_SQUARE + "  System", ImGuiTreeNodeFlags.DefaultOpen, this::renderSystemInfo);
        ImGuiUtils.collapse(ImIcons.FA.FA_JAVA + "  Java & Memory", this::renderJavaMemoryInfo);
        ImGuiUtils.collapse(ImIcons.FA.FA_MICROCHIP + "  Graphics", this::renderGraphicsInfo);
        ImGuiUtils.collapse(ImIcons.FA.FA_FILE_CONTRACT + "  Licenses", this::renderLicenses);
    }

    private void renderSystemInfo() {
        String os = System.getProperty("os.name") + " " + System.getProperty("os.version");
        String arch = System.getProperty("os.arch");
        int cores = Runtime.getRuntime().availableProcessors();
        String mcVersion = SharedConstants.getCurrentVersion().toString();

        formatted(ImIcons.FA.FA_WINDOWS + "  OS: %s (%s)", os, arch);
        formatted(ImIcons.FA.FA_MICROCHIP + "  CPU Cores: %d", cores);
        formatted(ImIcons.FA.FA_CUBE + "  Minecraft: %s", mcVersion);
    }

    private void renderJavaMemoryInfo() {
        Runtime rt = Runtime.getRuntime();
        long maxMem = rt.maxMemory() / (1024 * 1024);
        long usedMem = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);

        String javaVer = System.getProperty("java.version");
        String javaVm = System.getProperty("java.vm.name");

        formatted(ImIcons.FA.FA_MEMORY + "  Memory: %d MB used / %d MB max", usedMem, maxMem);
        formatted(ImIcons.FA.FA_JAVA + "  Java: %s", javaVer);
        formatted(ImIcons.FA.FA_COG + "  JVM: %s", javaVm);
    }

    private void renderGraphicsInfo() {
        String vendor = glGetString(GL_VENDOR);
        String renderer = glGetString(GL_RENDERER);
        String version = glGetString(GL_VERSION);
        int maxTextureSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
        int maxTextureUnits = glGetInteger(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);

        formatted(ImIcons.FA.FA_TAG + "  Vendor: %s", vendor);
        formatted(ImIcons.FA.FA_TAG + "  GPU: %s", renderer);
        formatted(ImIcons.FA.FA_TAG + "  OpenGL: %s", version);
        formatted(ImIcons.FA.FA_EXPAND + "  Max Texture Size: %d", maxTextureSize);
        formatted(ImIcons.FA.FA_LAYER_GROUP + "  Max Texture Units: %d", maxTextureUnits);
    }

    private void renderLicenses() {
        if (ImGui.collapsingHeader("FoundryEngine (All Rights Reserved)")) {
            renderScrollableText("All Rights Reserved\n\nThis software is proprietary and may not be copied, distributed, or modified without explicit permission.");
        }

        if (ImGui.collapsingHeader("ImGui (MIT)")) {
            renderScrollableText("""
                    MIT License
                    
                    Copyright (c) 2019-present, Ilya "SpaiR" Prymshyts
                    
                    Permission is hereby granted, free of charge, to any person obtaining a copy
                    of this software and associated documentation files (the "Software"), to deal
                    in the Software without restriction, including without limitation the rights
                    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
                    copies of the Software, and to permit persons to whom the Software is
                    furnished to do so, subject to the following conditions:
                    
                    The above copyright notice and this permission notice shall be included in all
                    copies or substantial portions of the Software.
                    
                    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
                    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
                    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
                    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
                    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
                    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
                    SOFTWARE.
                    """);
        }

        if (ImGui.collapsingHeader("Apache Groovy (Apache 2.0)")) {
            renderScrollableText("""
                    Apache License
                    Version 2.0, January 2004
                    https://www.apache.org/licenses/
                    
                    (Full license text available at the above URL)
                    
                    Summary: You may use, modify, and distribute this software under the terms
                    of the Apache 2.0 license. A copy of the license is included in the
                    distribution.
                    """);
        }
    }

    private void renderScrollableText(String text) {
        ImGui.beginChild("##license_text", 0, 200, true);
        ImGui.textWrapped(text);
        ImGui.endChild();
    }

    private void formatted(String text, Object... args) {
        ImGui.text(String.format(text, args));
    }
}