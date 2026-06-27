package de.luckymcdev.foundryengine.client.dialogue.display;

import com.mojang.blaze3d.platform.InputConstants;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.ui.Enums;
import de.luckymcdev.foundryengine.client.ui.UIVec;
import de.luckymcdev.foundryengine.client.ui.screen.EngineScreen;
import de.luckymcdev.foundryengine.client.ui.widget.ButtonWidget;
import de.luckymcdev.foundryengine.client.ui.widget.PanelWidget;
import de.luckymcdev.foundryengine.client.ui.widget.TextWidget;
import de.luckymcdev.foundryengine.common.dialogue.DialogueNode;
import de.luckymcdev.foundryengine.common.dialogue.DialogueSession;
import de.luckymcdev.foundryengine.common.dialogue.DialogueStyle;
import de.luckymcdev.foundryengine.common.dialogue.display.IDialogueDisplay;
import de.luckymcdev.foundryengine.common.network.packets.dialogue.ServerboundDialoguePacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen-based dialogue display using the widget system (EngineScreen + PanelWidget / ButtonWidget / TextWidget).
 */
public class ScreenDialogueDisplay implements IDialogueDisplay {
    private @Nullable DialogueScreen currentScreen;

    @Override
    public void showDialogue(Identifier treeId, DialogueSession session, DialogueNode node) {
        currentScreen = new DialogueScreen(node, session.getStyle());
        Client.setScreen(currentScreen);
    }

    @Override
    public void advanceDialogue(Identifier treeId, DialogueSession session, DialogueNode node) {
        if (currentScreen != null) {
            currentScreen.setStyle(session.getStyle());
            currentScreen.updateNode(node);
        } else {
            showDialogue(treeId, session, node);
        }
    }

    @Override
    public void endDialogue(Identifier treeId) {
        if (currentScreen != null) currentScreen = null;
        var mc = Client.getMc();
        if (mc.screen instanceof DialogueScreen) mc.setScreen(null);
    }

    @Override
    public boolean isActive() {
        return currentScreen != null;
    }

    private static class DialogueScreen extends EngineScreen {
        private static final double CHARS_PER_SECOND = 45.0;
        private final List<ButtonWidget> optionButtons = new ArrayList<>();
        private DialogueNode node;
        private DialogueStyle style;
        private boolean widgetsBuilt;
        private PanelWidget dialogueBox;
        private TextWidget speakerText;
        private TextWidget dialogueText;
        private PanelWidget optionsBox;
        private ButtonWidget navButton;
        private String fullText = "";
        private long typewriterStartNanos;
        private boolean typewriterDone;
        private boolean optionsRevealed;

        DialogueScreen(DialogueNode node, DialogueStyle style) {
            this.node = node;
            this.style = style;
            this.widgetsBuilt = false;
        }

        void setStyle(DialogueStyle s) {
            this.style = s;
        }

        void updateNode(DialogueNode n) {
            this.node = n;
            startTypewriter();
            if (widgetsBuilt) rebuildOptions();
        }

        @Override
        protected void init() {
            buildLayout();
            startTypewriter();
            rebuildOptions();
            widgetsBuilt = true;
            super.init();
        }

        private void buildLayout() {
            var m = style.getMargin();
            var ph = style.getPanelHeight();

            dialogueBox = new PanelWidget(
                    new UIVec(0, 1, m, -m),
                    new UIVec(0.75, 0, 0, ph)
            );
            dialogueBox.setAnchorPoint(new Vec2(0, 1));
            dialogueBox.setBackgroundColor(style.getDialogueBackground());
            dialogueBox.setBorder(style.getDialogueBorder(), style.getDialogueBorderWidth());

            speakerText = new TextWidget(
                    new UIVec(0, 0, 10, 8),
                    new UIVec(1, 0, -20, 14)
            );
            speakerText.setFontSize(style.getSpeakerFontSize());
            dialogueBox.addWidget(speakerText);

            dialogueText = new TextWidget(
                    new UIVec(0, 0, 10, 26),
                    new UIVec(1, 1, -20, -34)
            );
            dialogueText.setFontSize(style.getDialogueFontSize());
            dialogueBox.addWidget(dialogueText);

            this.addWidget(dialogueBox);

            optionsBox = new PanelWidget(
                    new UIVec(1, 1, -m, -m),
                    new UIVec(0.25, 0, 0, ph)
            );
            optionsBox.setAnchorPoint(new Vec2(1, 1));
            optionsBox.setBackgroundColor(style.getOptionsBackground());
            optionsBox.setBorder(style.getOptionsBorder(), style.getOptionsBorderWidth());
            this.addWidget(optionsBox);

            updateSpeaker();
        }

        private void updateSpeaker() {
            if (node == null) return;
            speakerText.setText(Component.literal("<" + node.getSpeaker() + ">").withStyle(s -> s.withColor(node.getSpeakerColor())));
        }

        private void startTypewriter() {
            fullText = node != null ? node.getText() : "";
            typewriterStartNanos = System.nanoTime();
            typewriterDone = fullText.isEmpty();
            optionsRevealed = typewriterDone;
            updateSpeaker();
            applyVisibleText(typewriterDone ? fullText.length() : 0);
        }

        private void tickTypewriter() {
            if (typewriterDone || fullText.isEmpty()) return;

            double elapsedSeconds = (System.nanoTime() - typewriterStartNanos) / 1_000_000_000.0;
            int visibleChars = (int) (elapsedSeconds * CHARS_PER_SECOND);

            if (visibleChars >= fullText.length()) {
                visibleChars = fullText.length();
                typewriterDone = true;
            }

            applyVisibleText(visibleChars);

            if (typewriterDone && !optionsRevealed) {
                optionsRevealed = true;
                if (widgetsBuilt) rebuildOptions();
            }
        }

        private void applyVisibleText(int visibleChars) {
            String shown = fullText.substring(0, Math.min(visibleChars, fullText.length()));
            dialogueText.setText(Component.literal(shown));
        }

        void skipTypewriter() {
            if (typewriterDone) return;
            typewriterDone = true;
            applyVisibleText(fullText.length());
            if (!optionsRevealed) {
                optionsRevealed = true;
                if (widgetsBuilt) rebuildOptions();
            }
        }

        private void rebuildOptions() {
            for (var btn : optionButtons) optionsBox.removeWidget(btn);
            optionButtons.clear();
            if (navButton != null) {
                optionsBox.removeWidget(navButton);
                navButton = null;
            }

            if (node == null) return;

            if (!optionsRevealed) return;

            var bt = style.getButtonHeight();
            var gap = style.getOptionGap();
            var options = node.getOptions();

            if (options.isEmpty()) {
                boolean hasNext = node.getNextNodeId() != null && !node.getNextNodeId().isBlank();
                navButton = new ButtonWidget(
                        new UIVec(0.5, 0.5, 0, 0),
                        new UIVec(0, 0, 130, bt),
                        (mx, my, btn) -> ClientPacketDistributor.sendToServer(
                                hasNext ? ServerboundDialoguePacket.advanceNext() : ServerboundDialoguePacket.end())
                );
                navButton.setAnchorPoint(new Vec2(0.5f, 0.5f));
                navButton.setBackgroundColor(style.getNavButtonBackground());
                navButton.setHoverColor(style.getNavButtonHover());
                navButton.setBorderColor(style.getNavButtonBorder());

                var label = new TextWidget(
                        new UIVec(0.5, 0.5, 0, 0),
                        new UIVec(0, 0, 120, bt)
                );
                label.setAnchorPoint(new Vec2(0.5f, 0.5f));
                label.setAlignment(Enums.Alignment.CENTER);
                label.setFontSize(style.getOptionFontSize());
                label.setText(Component.literal(hasNext ? "Next \u2192" : "[End Dialogue]"));
                navButton.addWidget(label);
                optionsBox.addWidget(navButton);
            } else {
                int y = 8;
                for (var opt : options) {
                    var btn = new ButtonWidget(
                            new UIVec(0, 0, 8, y),
                            new UIVec(1, 0, -16, bt),
                            (mx, my, b) -> ClientPacketDistributor.sendToServer(
                                    ServerboundDialoguePacket.selectOption(opt.getId()))
                    );
                    btn.setBackgroundColor(style.getButtonBackground());
                    btn.setHoverColor(style.getButtonHover());
                    btn.setBorderColor(style.getButtonBorder());

                    var label = new TextWidget(
                            new UIVec(0, 0, 4, 3),
                            new UIVec(1, 0, -8, 16)
                    );
                    label.setFontSize(style.getOptionFontSize());
                    label.setText(Component.literal(opt.getText()));

                    btn.addWidget(label);
                    optionButtons.add(btn);
                    optionsBox.addWidget(btn);
                    y += bt + gap;
                }
            }

            if (widgetsBuilt) {
                for (var child : optionsBox.getChildren()) child.onInit();
                optionsBox.updateArea();
            }
        }

        @Override
        public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            tickTypewriter();
            guiGraphics.fill(RenderPipelines.GUI, 0, 0, this.width, this.height, style.getOverlayColor().argb());
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            if(event.key() == InputConstants.KEY_SPACE) {
                skipTypewriter();
            }
            return super.keyPressed(event);
        }
    }

}