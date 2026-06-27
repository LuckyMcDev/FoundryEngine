package de.luckymcdev.foundryengine.common.dialogue;

import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.nbt.CompoundTag;

public class DialogueStyle {
    private Color dialogueBackground = new Color(0xCC101010);
    private Color dialogueBorder = new Color(0xFF666666);
    private int dialogueBorderWidth = 2;
    private Color optionsBackground = new Color(0xCC101010);
    private Color optionsBorder = new Color(0xFF666666);
    private int optionsBorderWidth = 2;
    private Color buttonBackground = new Color(0x88000000);
    private Color buttonHover = new Color(0xAA444444);
    private Color buttonBorder = new Color(0xFF888888);
    private Color navButtonBackground = new Color(0x88000000);
    private Color navButtonHover = new Color(0xAA444444);
    private Color navButtonBorder = new Color(0xFF888888);
    private Color overlayColor = new Color(0x88000000);
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
        s.dialogueBackground = new Color(tag.getIntOr("DialogueBackground", s.dialogueBackground.argb()));
        s.dialogueBorder = new Color(tag.getIntOr("DialogueBorder", s.dialogueBorder.argb()));
        s.dialogueBorderWidth = tag.getIntOr("DialogueBorderWidth", s.dialogueBorderWidth);
        s.optionsBackground = new Color(tag.getIntOr("OptionsBackground", s.optionsBackground.argb()));
        s.optionsBorder = new Color(tag.getIntOr("OptionsBorder", s.optionsBorder.argb()));
        s.optionsBorderWidth = tag.getIntOr("OptionsBorderWidth", s.optionsBorderWidth);
        s.buttonBackground = new Color(tag.getIntOr("ButtonBackground", s.buttonBackground.argb()));
        s.buttonHover = new Color(tag.getIntOr("ButtonHover", s.buttonHover.argb()));
        s.buttonBorder = new Color(tag.getIntOr("ButtonBorder", s.buttonBorder.argb()));
        s.navButtonBackground = new Color(tag.getIntOr("NavButtonBackground", s.navButtonBackground.argb()));
        s.navButtonHover = new Color(tag.getIntOr("NavButtonHover", s.navButtonHover.argb()));
        s.navButtonBorder = new Color(tag.getIntOr("NavButtonBorder", s.navButtonBorder.argb()));
        s.overlayColor = new Color(tag.getIntOr("OverlayColor", s.overlayColor.argb()));
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
        tag.putInt("DialogueBackground", dialogueBackground.argb());
        tag.putInt("DialogueBorder", dialogueBorder.argb());
        tag.putInt("DialogueBorderWidth", dialogueBorderWidth);
        tag.putInt("OptionsBackground", optionsBackground.argb());
        tag.putInt("OptionsBorder", optionsBorder.argb());
        tag.putInt("OptionsBorderWidth", optionsBorderWidth);
        tag.putInt("ButtonBackground", buttonBackground.argb());
        tag.putInt("ButtonHover", buttonHover.argb());
        tag.putInt("ButtonBorder", buttonBorder.argb());
        tag.putInt("NavButtonBackground", navButtonBackground.argb());
        tag.putInt("NavButtonHover", navButtonHover.argb());
        tag.putInt("NavButtonBorder", navButtonBorder.argb());
        tag.putInt("OverlayColor", overlayColor.argb());
        tag.putInt("SpeakerFontSize", speakerFontSize);
        tag.putInt("DialogueFontSize", dialogueFontSize);
        tag.putInt("OptionFontSize", optionFontSize);
        tag.putInt("Margin", margin);
        tag.putInt("PanelHeight", panelHeight);
        tag.putInt("ButtonHeight", buttonHeight);
        tag.putInt("OptionGap", optionGap);
        return tag;
    }

    public Color getDialogueBackground() {
        return dialogueBackground;
    }

    public void setDialogueBackground(Color v) {
        this.dialogueBackground = v;
    }

    public Color getDialogueBorder() {
        return dialogueBorder;
    }

    public void setDialogueBorder(Color v) {
        this.dialogueBorder = v;
    }

    public int getDialogueBorderWidth() {
        return dialogueBorderWidth;
    }

    public void setDialogueBorderWidth(int v) {
        this.dialogueBorderWidth = v;
    }

    public Color getOptionsBackground() {
        return optionsBackground;
    }

    public void setOptionsBackground(Color v) {
        this.optionsBackground = v;
    }

    public Color getOptionsBorder() {
        return optionsBorder;
    }

    public void setOptionsBorder(Color v) {
        this.optionsBorder = v;
    }

    public int getOptionsBorderWidth() {
        return optionsBorderWidth;
    }

    public void setOptionsBorderWidth(int v) {
        this.optionsBorderWidth = v;
    }

    public Color getButtonBackground() {
        return buttonBackground;
    }

    public void setButtonBackground(Color v) {
        this.buttonBackground = v;
    }

    public Color getButtonHover() {
        return buttonHover;
    }

    public void setButtonHover(Color v) {
        this.buttonHover = v;
    }

    public Color getButtonBorder() {
        return buttonBorder;
    }

    public void setButtonBorder(Color v) {
        this.buttonBorder = v;
    }

    public Color getNavButtonBackground() {
        return navButtonBackground;
    }

    public void setNavButtonBackground(Color v) {
        this.navButtonBackground = v;
    }

    public Color getNavButtonHover() {
        return navButtonHover;
    }

    public void setNavButtonHover(Color v) {
        this.navButtonHover = v;
    }

    public Color getNavButtonBorder() {
        return navButtonBorder;
    }

    public void setNavButtonBorder(Color v) {
        this.navButtonBorder = v;
    }

    public Color getOverlayColor() {
        return overlayColor;
    }

    public void setOverlayColor(Color v) {
        this.overlayColor = v;
    }

    public int getSpeakerFontSize() {
        return speakerFontSize;
    }

    public void setSpeakerFontSize(int v) {
        this.speakerFontSize = v;
    }

    public int getDialogueFontSize() {
        return dialogueFontSize;
    }

    public void setDialogueFontSize(int v) {
        this.dialogueFontSize = v;
    }

    public int getOptionFontSize() {
        return optionFontSize;
    }

    public void setOptionFontSize(int v) {
        this.optionFontSize = v;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int v) {
        this.margin = v;
    }

    public int getPanelHeight() {
        return panelHeight;
    }

    public void setPanelHeight(int v) {
        this.panelHeight = v;
    }

    public int getButtonHeight() {
        return buttonHeight;
    }

    public void setButtonHeight(int v) {
        this.buttonHeight = v;
    }

    public int getOptionGap() {
        return optionGap;
    }

    public void setOptionGap(int v) {
        this.optionGap = v;
    }
}