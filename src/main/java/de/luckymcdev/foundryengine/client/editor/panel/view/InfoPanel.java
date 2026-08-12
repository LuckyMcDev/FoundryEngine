package de.luckymcdev.foundryengine.client.editor.panel.view;

import com.mojang.blaze3d.systems.RenderSystem;
import de.luckymcdev.foundryengine.FoundryEngineMod;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import foundry.imgui.impl.ImGuiWindowHandler;
import imgui.ImGui;
import net.minecraft.SharedConstants;

import java.util.Locale;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS;

public class InfoPanel extends EditorPanel {
	public static final InfoPanel INSTANCE = new InfoPanel();

	protected InfoPanel() {
		super(new Builder(Common.id("info"))
			.icon(ImIcons.INFO_CIRCLE)
			.category(PanelCategory.VIEW));
	}

	@Override
	public void content(ImGraphicsExtractor g) {
		g.cardBegin("##about");
		g.labeledValue("Foundry Engine", FoundryEngineMod.modVersion.toString());
		g.cardEnd();

		ImGui.spacing();
		g.treeSection(ImGraphicsExtractor.icon(ImIcons.MINUS_SQUARE) + "  System", this::renderSystemInfo);
		g.treeSection(ImGraphicsExtractor.icon(ImIcons.JAVA) + "  Java & Memory", () -> renderJavaMemoryInfo(g));
		g.treeSection(ImGraphicsExtractor.icon(ImIcons.MICROCHIP) + "  Graphics", this::renderGraphicsInfo);
		g.treeSection(ImGraphicsExtractor.icon(ImIcons.FILE_CONTRACT) + "  Licenses", this::renderLicenses);
	}

	private void renderSystemInfo() {
		String os = System.getProperty("os.name") + " " + System.getProperty("os.version");
		String arch = System.getProperty("os.arch");
		int cores = Runtime.getRuntime().availableProcessors();
		String mcVersion = SharedConstants.getCurrentVersion().toString();
		//? if 26.1 {
		String backendName = RenderSystem.getDevice().getBackendName();
		 //?} elif 26.2 {
		/*String backendName = RenderSystem.getDevice().getDeviceInfo().backendName();
		*///?}

		formatted(ImGraphicsExtractor.icon(ImIcons.WINDOWS) + "  OS", "%s (%s)", os, arch);
		formatted(ImGraphicsExtractor.icon(ImIcons.MICROCHIP) + "  CPU Cores", "%d", cores);
		formatted(ImGraphicsExtractor.icon(ImIcons.CUBE) + "  Minecraft", "%s", mcVersion);
		formatted(ImGraphicsExtractor.icon(ImIcons.VUEJS) + "  Graphics backend", "%s", backendName);
	}

	private void renderJavaMemoryInfo(ImGraphicsExtractor g) {
		Runtime rt = Runtime.getRuntime();
		long maxMem = rt.maxMemory() / (1024 * 1024);
		long totalMem = rt.totalMemory() / (1024 * 1024);
		long usedMem = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);

		String javaVer = System.getProperty("java.version");
		String javaVm = System.getProperty("java.vm.name");

		formatted(ImGraphicsExtractor.icon(ImIcons.MEMORY) + "  Memory", "%d MB used / %d MB max", usedMem, maxMem);
		float usage = (float) usedMem / maxMem;
		ImGui.progressBar(usage, -1, 0, "");
		g.helpTooltip(String.format("Allocated: %d MB", totalMem));

		formatted(ImGraphicsExtractor.icon(ImIcons.JAVA) + "  Java", "%s", javaVer);
		formatted(ImGraphicsExtractor.icon(ImIcons.COG) + "  JVM", "%s", javaVm);
	}

	private void renderGraphicsInfo() {
		String vendor = glGetString(GL_VENDOR);
		String renderer = glGetString(GL_RENDERER);
		String version = glGetString(GL_VERSION);
		int maxTextureSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
		int maxTextureUnits = glGetInteger(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);

		formatted(ImGraphicsExtractor.icon(ImIcons.TAG) + "  Vendor", "%s", vendor);
		formatted(ImGraphicsExtractor.icon(ImIcons.TAG) + "  GPU", "%s", renderer);
		formatted(ImGraphicsExtractor.icon(ImIcons.TAG) + "  OpenGL", "%s", version);
		formatted(ImGraphicsExtractor.icon(ImIcons.EXPAND) + "  Max Texture Size", "%d", maxTextureSize);
		formatted(ImGraphicsExtractor.icon(ImIcons.LAYER_GROUP) + "  Max Texture Units", "%d", maxTextureUnits);
	}

	private void renderLicenses() {
		if (ImGui.collapsingHeader(ImGraphicsExtractor.icon(ImIcons.COPYRIGHT) + " FoundryEngine (All Rights Reserved)")) {
			ImGui.beginChild("##license_fe", 0, 60, true);
			ImGui.textWrapped("All Rights Reserved — This software is proprietary and may not be copied, distributed, or modified without explicit permission.");
			ImGui.endChild();
		}

		if (ImGui.collapsingHeader(ImGraphicsExtractor.icon(ImIcons.COPYRIGHT) + " ImGui (MIT)")) {
			ImGui.beginChild("##license_imgui", 0, 120, true);
			ImGui.textWrapped("""
				MIT License — Copyright (c) 2019-present, Ilya "SpaiR" Prymshyts
				Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so.""");
			ImGui.endChild();
		}

		if (ImGui.collapsingHeader(ImGraphicsExtractor.icon(ImIcons.COPYRIGHT) + " Apache Groovy (Apache 2.0)")) {
			ImGui.beginChild("##license_groovy", 0, 80, true);
			ImGui.textWrapped("""
				Apache License, Version 2.0 — You may use, modify, and distribute this software under the terms of the Apache 2.0 license. A copy is included in the distribution.""");
			ImGui.endChild();
		}
	}

	private void formatted(String icon, String fmt, Object... args) {
		ImGui.text(icon);
		ImGui.sameLine();
		ImGui.textDisabled(String.format(fmt, args));
	}
}
