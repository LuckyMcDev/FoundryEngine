package io.github.luckymcdev.foundryengine.client.editor.builtin;

import com.cinemamod.mcef.MCEF;
import com.cinemamod.mcef.MCEFBrowser;
import com.mojang.blaze3d.textures.GpuTexture;
import imgui.ImGui;
import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class BrowserPanel extends Panel {
    public static final BrowserPanel INSTANCE = new BrowserPanel();
    private final Minecraft minecraft = Minecraft.getInstance();
    private MCEFBrowser browser;
    private String docType = "javadoc";

    private BrowserPanel() {
        super(Common.id("browser_panel"), "Web Browser");
    }

    @Override
    public void onOpened() {
        if (browser == null) {
            browser = MCEF.createBrowser("https://google.com", true);
        }
    }

    @Override
    public void content() {
        if (browser == null) return;

        if (!browser.isTextureReady()) {
            ImGui.text("Waiting for MCEF texture...");
            return;
        }

        browser.setZoomLevel(3);

        Identifier loc = browser.getTextureIdentifier();
        int glId = -1;

        if (loc != null) {
            GpuTexture texture = minecraft.getTextureManager().getTexture(loc).getTexture();
            glId = Client.unwrapTexture(texture).glId();
        }

        float availX = ImGui.getContentRegionAvailX();
        float availY = ImGui.getContentRegionAvailY();
        float startX = ImGui.getCursorScreenPosX();
        float startY = ImGui.getCursorScreenPosY();

        if (glId != -1) {
            // We use the full available area.
            // Tip: If it's still blurry, try: Math.round(availX)
            ImGui.image(glId, availX, availY, 0, 0, 1, 1);
        }

        // Sync Resolution - Critical for blurriness
        int scaledWidth = (int) (availX * minecraft.getWindow().getGuiScale());
        int scaledHeight = (int) (availY * minecraft.getWindow().getGuiScale());

        if (scaledWidth > 0 && scaledHeight > 0) {
            browser.resize(scaledWidth, scaledHeight);
        }

        // Input Handling
        if (ImGui.isWindowHovered()) {
            handleMouse(startX, startY, availX, availY);
        }

        // Only handle keyboard if the ImGui window itself is focused
        if (ImGui.isWindowFocused()) {
            handleKeyboard();
        }
    }

    private void handleKeyboard() {
    }

    private void handleMouse(float startX, float startY, float width, float height) {
        // Calculate mouse position relative to the image start
        float mouseX = ImGui.getMousePosX() - startX;
        float mouseY = ImGui.getMousePosY() - startY;

        // Only send input if the mouse is actually within the browser bounds
        if (mouseX >= 0 && mouseX <= width && mouseY >= 0 && mouseY <= height) {
            int bX = (int) (mouseX * minecraft.getWindow().getGuiScale());
            int bY = (int) (mouseY * minecraft.getWindow().getGuiScale());

            browser.sendMouseMove(bX, bY);

            // Left Click
            if (ImGui.isMouseClicked(0)) {
                browser.sendMousePress(bX, bY, 0);
                browser.setFocus(true); // Ensure CEF knows it's active
            }
            if (ImGui.isMouseReleased(0)) {
                browser.sendMouseRelease(bX, bY, 0);
            }

            // Right Click (Button 1)
            if (ImGui.isMouseClicked(1)) {
                browser.sendMousePress(bX, bY, 1);
            }
            if (ImGui.isMouseReleased(1)) {
                browser.sendMouseRelease(bX, bY, 1);
            }

            // Scroll
            float scrollY = ImGui.getIO().getMouseWheel();
            if (scrollY != 0) {
                browser.sendMouseWheel(bX, bY, scrollY, 0);
            }
        }
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String newType) {
        docType = newType;
        if (docType.matches("javadoc"))
            browser.loadURL("C:\\Data\\Projects\\FoundryEngine\\build\\docs\\javadoc\\index.html");
        else if (docType.matches("doxygen"))
            browser.loadURL("C:\\Data\\Projects\\FoundryEngine\\.docs\\html\\index.html");
    }

    public void reload() {
        browser.reload();
    }

    @Override
    public void onClosed() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
    }
}