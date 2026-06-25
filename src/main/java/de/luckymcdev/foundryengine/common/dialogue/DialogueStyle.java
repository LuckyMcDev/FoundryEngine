package de.luckymcdev.foundryengine.common.dialogue;

import net.minecraft.nbt.CompoundTag;

public class DialogueStyle {
    private int dialogueBackground = 0xCC101010;
    private int dialogueBorder = 0xFF666666;
    private int dialogueBorderWidth = 2;
    private int optionsBackground = 0xCC101010;
    private int optionsBorder = 0xFF666666;
    private int optionsBorderWidth = 2;
    private int buttonBackground = 0x88000000;
    private int buttonHover = 0xAA444444;
    private int buttonBorder = 0xFF888888;
    private int navButtonBackground = 0x88000000;
    private int navButtonHover = 0xAA444444;
    private int navButtonBorder = 0xFF888888;
    private int overlayColor = 0x88000000;
    private int speakerFontSize = 9;
    private int dialogueFontSize = 9;
    private int optionFontSize = 9;
    private int margin = 10;
    private int panelHeight = 150;
    private int buttonHeight = 22;
    private int optionGap = 4;

    public DialogueStyle() {
    }

    public static DialogueStyle fromNbt(CompoundTag tag) {
        var s = new DialogueStyle();
        s.dialogueBackground = tag.getIntOr("DialogueBackground", s.dialogueBackground);
        s.dialogueBorder = tag.getIntOr("DialogueBorder", s.dialogueBorder);
        s.dialogueBorderWidth = tag.getIntOr("DialogueBorderWidth", s.dialogueBorderWidth);
        s.optionsBackground = tag.getIntOr("OptionsBackground", s.optionsBackground);
        s.optionsBorder = tag.getIntOr("OptionsBorder", s.optionsBorder);
        s.optionsBorderWidth = tag.getIntOr("OptionsBorderWidth", s.optionsBorderWidth);
        s.buttonBackground = tag.getIntOr("ButtonBackground", s.buttonBackground);
        s.buttonHover = tag.getIntOr("ButtonHover", s.buttonHover);
        s.buttonBorder = tag.getIntOr("ButtonBorder", s.buttonBorder);
        s.navButtonBackground = tag.getIntOr("NavButtonBackground", s.navButtonBackground);
        s.navButtonHover = tag.getIntOr("NavButtonHover", s.navButtonHover);
        s.navButtonBorder = tag.getIntOr("NavButtonBorder", s.navButtonBorder);
        s.overlayColor = tag.getIntOr("OverlayColor", s.overlayColor);
        s.speakerFontSize = tag.getIntOr("SpeakerFontSize", s.speakerFontSize);
        s.dialogueFontSize = tag.getIntOr("DialogueFontSize", s.dialogueFontSize);
        s.optionFontSize = tag.getIntOr("OptionFontSize", s.optionFontSize);
        s.margin = tag.getIntOr("Margin", s.margin);
        s.panelHeight = tag.getIntOr("PanelHeight", s.panelHeight);
        s.buttonHeight = tag.getIntOr("ButtonHeight", s.buttonHeight);
        s.optionGap = tag.getIntOr("OptionGap", s.optionGap);
        return s;
    }

    public CompoundTag toNbt() {
        var tag = new CompoundTag();
        tag.putInt("DialogueBackground", dialogueBackground);
        tag.putInt("DialogueBorder", dialogueBorder);
        tag.putInt("DialogueBorderWidth", dialogueBorderWidth);
        tag.putInt("OptionsBackground", optionsBackground);
        tag.putInt("OptionsBorder", optionsBorder);
        tag.putInt("OptionsBorderWidth", optionsBorderWidth);
        tag.putInt("ButtonBackground", buttonBackground);
        tag.putInt("ButtonHover", buttonHover);
        tag.putInt("ButtonBorder", buttonBorder);
        tag.putInt("NavButtonBackground", navButtonBackground);
        tag.putInt("NavButtonHover", navButtonHover);
        tag.putInt("NavButtonBorder", navButtonBorder);
        tag.putInt("OverlayColor", overlayColor);
        tag.putInt("SpeakerFontSize", speakerFontSize);
        tag.putInt("DialogueFontSize", dialogueFontSize);
        tag.putInt("OptionFontSize", optionFontSize);
        tag.putInt("Margin", margin);
        tag.putInt("PanelHeight", panelHeight);
        tag.putInt("ButtonHeight", buttonHeight);
        tag.putInt("OptionGap", optionGap);
        return tag;
    }

    public int getDialogueBackground() { return dialogueBackground; }
    public void setDialogueBackground(int v) { this.dialogueBackground = v; }
    public int getDialogueBorder() { return dialogueBorder; }
    public void setDialogueBorder(int v) { this.dialogueBorder = v; }
    public int getDialogueBorderWidth() { return dialogueBorderWidth; }
    public void setDialogueBorderWidth(int v) { this.dialogueBorderWidth = v; }
    public int getOptionsBackground() { return optionsBackground; }
    public void setOptionsBackground(int v) { this.optionsBackground = v; }
    public int getOptionsBorder() { return optionsBorder; }
    public void setOptionsBorder(int v) { this.optionsBorder = v; }
    public int getOptionsBorderWidth() { return optionsBorderWidth; }
    public void setOptionsBorderWidth(int v) { this.optionsBorderWidth = v; }
    public int getButtonBackground() { return buttonBackground; }
    public void setButtonBackground(int v) { this.buttonBackground = v; }
    public int getButtonHover() { return buttonHover; }
    public void setButtonHover(int v) { this.buttonHover = v; }
    public int getButtonBorder() { return buttonBorder; }
    public void setButtonBorder(int v) { this.buttonBorder = v; }
    public int getNavButtonBackground() { return navButtonBackground; }
    public void setNavButtonBackground(int v) { this.navButtonBackground = v; }
    public int getNavButtonHover() { return navButtonHover; }
    public void setNavButtonHover(int v) { this.navButtonHover = v; }
    public int getNavButtonBorder() { return navButtonBorder; }
    public void setNavButtonBorder(int v) { this.navButtonBorder = v; }
    public int getOverlayColor() { return overlayColor; }
    public void setOverlayColor(int v) { this.overlayColor = v; }
    public int getSpeakerFontSize() { return speakerFontSize; }
    public void setSpeakerFontSize(int v) { this.speakerFontSize = v; }
    public int getDialogueFontSize() { return dialogueFontSize; }
    public void setDialogueFontSize(int v) { this.dialogueFontSize = v; }
    public int getOptionFontSize() { return optionFontSize; }
    public void setOptionFontSize(int v) { this.optionFontSize = v; }
    public int getMargin() { return margin; }
    public void setMargin(int v) { this.margin = v; }
    public int getPanelHeight() { return panelHeight; }
    public void setPanelHeight(int v) { this.panelHeight = v; }
    public int getButtonHeight() { return buttonHeight; }
    public void setButtonHeight(int v) { this.buttonHeight = v; }
    public int getOptionGap() { return optionGap; }
    public void setOptionGap(int v) { this.optionGap = v; }
}
