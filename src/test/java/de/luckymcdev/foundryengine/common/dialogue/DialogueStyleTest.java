package de.luckymcdev.foundryengine.common.dialogue;

import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DialogueStyleTest {

	@Test
	void defaults_WhenNoOverrides() {
		var s = new DialogueStyle();
		assertEquals(0xCC101010, s.getDialogueBackground().argb());
		assertEquals(2, s.getDialogueBorderWidth());
		assertEquals(125, s.getPanelHeight());
		assertEquals(9, s.getSpeakerFontSize());
		assertTrue(s.isTypewriterSoundEnabled());
		assertEquals(45, s.getTypewriterCharsPerSecond());
	}

	@Test
	void baseLayer_SeededFromDefaults_WithoutMutatingDefaults() {
		var s = new DialogueStyle();
		s.setPanelHeight(200);

		assertEquals(200, s.getPanelHeight());
		assertEquals(125, DialogueStyle.DEFAULTS.get("PanelHeight"));
	}

	@Test
	void setters_OverrideValues() {
		var s = new DialogueStyle();
		s.setDialogueBackground(new Color(0xFF123456));
		s.setPanelHeight(200);
		s.setTypewriterSoundEnabled(false);
		s.setTypewriterCharsPerSecond(80);

		assertEquals(0xFF123456, s.getDialogueBackground().argb());
		assertEquals(200, s.getPanelHeight());
		assertFalse(s.isTypewriterSoundEnabled());
		assertEquals(80, s.getTypewriterCharsPerSecond());
	}

	@Test
	void pushPop_OverridesTopLayer() {
		var s = new DialogueStyle();
		s.setPanelHeight(100);

		s.push();
		s.setPanelHeight(200);
		assertEquals(200, s.getPanelHeight());

		s.pop();
		assertEquals(100, s.getPanelHeight());
	}

	@Test
	void pop_KeepsAtLeastOneLayer() {
		var s = new DialogueStyle();
		s.setPanelHeight(50);
		s.pop();
		assertNull(s.pop());
		assertEquals(50, s.getPanelHeight());
	}

	@Test
	void reset_RestoresDefaults() {
		var s = new DialogueStyle();
		s.setPanelHeight(300);
		s.setTypewriterSoundEnabled(false);
		s.reset();

		assertEquals(125, s.getPanelHeight());
		assertTrue(s.isTypewriterSoundEnabled());
		assertEquals(45, s.getTypewriterCharsPerSecond());
	}

	@Test
	void nbt_RoundTrip_PreservesValues() {
		var s = new DialogueStyle();
		s.setDialogueBackground(new Color(0xFF010203));
		s.setMargin(24);
		s.setTypewriterSoundEnabled(false);
		s.setTypewriterCharsPerSecond(60);

		var loaded = DialogueStyle.fromNbt(s.toNbt());

		assertEquals(0xFF010203, loaded.getDialogueBackground().argb());
		assertEquals(24, loaded.getMargin());
		assertFalse(loaded.isTypewriterSoundEnabled());
		assertEquals(60, loaded.getTypewriterCharsPerSecond());
	}

	@Test
	void nbt_MissingKeys_FallBackToDefaults() {
		var loaded = DialogueStyle.fromNbt(new CompoundTag());
		assertEquals(125, loaded.getPanelHeight());
		assertTrue(loaded.isTypewriterSoundEnabled());
		assertEquals(45, loaded.getTypewriterCharsPerSecond());
	}

	@Test
	void defaults_IsFrozen() {
		assertThrows(IllegalStateException.class, () -> DialogueStyle.DEFAULTS.set("PanelHeight", 999));
		assertThrows(IllegalStateException.class, () -> DialogueStyle.DEFAULTS.remove("PanelHeight"));
	}
}
