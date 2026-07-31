package de.luckymcdev.foundryengine.client.dialogue.display;

import com.mojang.blaze3d.platform.InputConstants;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.ui.Enums;
import de.luckymcdev.foundryengine.client.ui.screen.EngineScreen;
import de.luckymcdev.foundryengine.client.ui.widget.ButtonWidget;
import de.luckymcdev.foundryengine.client.ui.widget.PanelWidget;
import de.luckymcdev.foundryengine.client.ui.widget.TextWidget;
import de.luckymcdev.foundryengine.common.dialogue.DialogueNode;
import de.luckymcdev.foundryengine.common.dialogue.DialogueSession;
import de.luckymcdev.foundryengine.common.dialogue.DialogueStyle;
import de.luckymcdev.foundryengine.common.dialogue.display.IDialogueDisplay;
import de.luckymcdev.foundryengine.common.network.packets.dialogue.ServerboundDialoguePacket;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
		if (currentScreen != null) {
			currentScreen = null;
		}
		var mc = Client.getMc();
		if (mc.screen instanceof DialogueScreen) {
			mc.setScreen(null);
		}
	}

	@Override
	public boolean isActive() {
		return currentScreen != null;
	}

	private static class DialogueScreen extends EngineScreen {
		private static final double MAX_PANEL_HEIGHT_FRACTION = 0.28;

		private final List<ButtonWidget> optionButtons = new ArrayList<>();
		private DialogueNode node;
		private DialogueStyle style;
		private PanelWidget dialogueBox;
		private TextWidget speakerText;
		private TextWidget dialogueText;
		private PanelWidget optionsBox;
		private ButtonWidget navButton;
		private String fullText = "";
		private long typewriterStartNanos;
		private int lastVisibleCharCount = 0;
		private boolean typewriterDone;
		private boolean optionsRevealed;

		DialogueScreen(DialogueNode node, DialogueStyle style) {
			this.node = node;
			this.style = style;
		}

		void setStyle(DialogueStyle s) {
			this.style = s;
		}

		void updateNode(DialogueNode n) {
			this.node = n;
			startTypewriter();
			if (isWidgetsInitialized()) {
				rebuildOptions();
			}
		}

		@Override
		protected void init() {
			if (shouldBuildWidgets()) {
				buildLayout();
				startTypewriter();
			} else {
				applyVisibleText(lastVisibleCharCount);
			}
			rebuildOptions();
			super.init();
		}

		private int resolvePanelHeight() {
			int configured = style.getPanelHeight();
			int capped = (int) (this.height * MAX_PANEL_HEIGHT_FRACTION);
			return Math.clamp(capped, 60, configured);
		}

		private void buildLayout() {
			var m = style.getMargin();
			var ph = resolvePanelHeight();

			dialogueBox = new PanelWidget();
			dialogueBox.setPositionAbsolute();
			dialogueBox.setInsetLeft(m);
			dialogueBox.setInsetBottom(m);
			dialogueBox.setWidthPercent(0.75f);
			dialogueBox.setHeight(ph);
			dialogueBox.setFlexDirection(FlexDirection.COLUMN);
			dialogueBox.setPadding(10, 10, 8, 8);
			dialogueBox.setGap(4);
			dialogueBox.setBackgroundColor(style.getDialogueBackground());
			dialogueBox.setBorder(style.getDialogueBorder(), style.getDialogueBorderWidth());

			speakerText = new TextWidget();
			speakerText.setWidthPercent(1.0f);
			speakerText.setHeight(14);
			speakerText.setFlexShrink(0);
			speakerText.setFontSize(style.getSpeakerFontSize());
			dialogueBox.addWidget(speakerText);

			dialogueText = new TextWidget();
			dialogueText.setWidthPercent(1.0f);
			dialogueText.setFlexGrow(1);
			dialogueText.setFontSize(style.getDialogueFontSize());
			dialogueBox.addWidget(dialogueText);

			this.addWidget(dialogueBox);

			optionsBox = new PanelWidget();
			optionsBox.setPositionAbsolute();
			optionsBox.setInsetRight(m);
			optionsBox.setInsetBottom(m);
			optionsBox.setWidthPercent(0.25f);
			optionsBox.setHeight(ph);
			optionsBox.setFlexDirection(FlexDirection.COLUMN);
			optionsBox.setPadding(8, 8, 8, 8);
			optionsBox.setGap(style.getOptionGap());
			optionsBox.setBackgroundColor(style.getOptionsBackground());
			optionsBox.setBorder(style.getOptionsBorder(), style.getOptionsBorderWidth());
			this.addWidget(optionsBox);

			updateSpeaker();
		}

		private void updateSpeaker() {
			if (node == null) {
				return;
			}
			speakerText.setText(Component.literal("<" + node.getSpeaker() + ">").withStyle(s -> s.withColor(node.getSpeakerColor())));
		}

		private void startTypewriter() {
			fullText = node != null ? node.getText() : "";
			typewriterStartNanos = System.nanoTime();
			typewriterDone = fullText.isEmpty();
			optionsRevealed = typewriterDone;
			lastVisibleCharCount = 0;
			updateSpeaker();
			applyVisibleText(typewriterDone ? fullText.length() : 0);
		}

		private void tickTypewriter() {
			if (typewriterDone || fullText.isEmpty()) {
				return;
			}

			double elapsedSeconds = (System.nanoTime() - typewriterStartNanos) / 1_000_000_000.0;
			int visibleChars = (int) (elapsedSeconds * Math.max(1, style.getTypewriterCharsPerSecond()));

			if (visibleChars >= fullText.length()) {
				visibleChars = fullText.length();
				typewriterDone = true;
			}

			int newChars = visibleChars - lastVisibleCharCount;
			if (newChars > 0 && style.isTypewriterSoundEnabled()) {
				SoundEvent typewriterSound = SoundEvents.POINTED_DRIPSTONE_DRIP_WATER;
				for (int i = 0; i < newChars; i++) {
					Minecraft.getInstance().getSoundManager().play(
						SimpleSoundInstance.forUI(typewriterSound, 0.4f, 0.9f)
					);
				}
			}
			lastVisibleCharCount = visibleChars;

			applyVisibleText(visibleChars);

			if (typewriterDone && !optionsRevealed) {
				optionsRevealed = true;
				if (isWidgetsInitialized()) {
					rebuildOptions();
				}
			}
		}

		private void applyVisibleText(int visibleChars) {
			String shown = fullText.substring(0, Math.min(visibleChars, fullText.length()));
			dialogueText.setText(Component.literal(shown));
		}

		void skipTypewriter() {
			if (typewriterDone) {
				return;
			}
			typewriterDone = true;
			lastVisibleCharCount = fullText.length();
			applyVisibleText(fullText.length());
			if (!optionsRevealed) {
				optionsRevealed = true;
				if (isWidgetsInitialized()) {
					rebuildOptions();
				}
			}
		}

		private void rebuildOptions() {
			for (var btn : optionButtons) {
				optionsBox.removeWidget(btn);
			}
			optionButtons.clear();
			if (navButton != null) {
				optionsBox.removeWidget(navButton);
				navButton = null;
			}

			if (node == null) {
				return;
			}

			if (!optionsRevealed) {
				return;
			}

			var bt = style.getButtonHeight();
			var options = node.getOptions();

			if (options.isEmpty()) {
				boolean hasNext = node.getNextNodeId() != null && !node.getNextNodeId().isBlank();
				navButton = new ButtonWidget((mx, my, btn) -> ClientPacketDistributor.sendToServer(
					hasNext ? ServerboundDialoguePacket.advanceNext() : ServerboundDialoguePacket.end()));
				navButton.setWidthPercent(1.0f);
				navButton.setHeight(bt);
				navButton.setFlexShrink(0);
				navButton.setBackgroundColor(style.getNavButtonBackground());
				navButton.setHoverColor(style.getNavButtonHover());
				navButton.setBorderColor(style.getNavButtonBorder());

				var label = new TextWidget();
				label.setWidthPercent(1.0f);
				label.setHeightPercent(1.0f);
				label.setAlignment(Enums.Alignment.CENTER);
				label.setFontSize(style.getOptionFontSize());
				label.setText(Component.literal(hasNext ? "Next ->" : "[End Dialogue]"));
				navButton.addWidget(label);
				optionsBox.addWidget(navButton);
			} else {
				for (var opt : options) {
					var btn = new ButtonWidget((mx, my, b) -> ClientPacketDistributor.sendToServer(
						ServerboundDialoguePacket.selectOption(opt.getId())));
					btn.setWidthPercent(1.0f);
					btn.setHeight(bt);
					btn.setFlexShrink(0);
					btn.setPadding(4, 4, 3, 3);
					btn.setAlignItems(AlignItems.CENTER);
					btn.setBackgroundColor(style.getButtonBackground());
					btn.setHoverColor(style.getButtonHover());
					btn.setBorderColor(style.getButtonBorder());

					var label = new TextWidget();
					label.setWidthPercent(1.0f);
					label.setHeightPercent(1.0f);
					label.setFontSize(style.getOptionFontSize());
					label.setText(Component.literal(opt.getText()));

					btn.addWidget(label);
					optionButtons.add(btn);
					optionsBox.addWidget(btn);
				}
			}

			if (isWidgetsInitialized()) {
				for (var child : optionsBox.getChildren()) {
					child.onInit();
				}
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
			if (event.key() == InputConstants.KEY_SPACE) {
				skipTypewriter();
			}
			return super.keyPressed(event);
		}
	}

}