package de.luckymcdev.foundryengine.client.imgui.hotkey;
// ImHotKey v1.0 - Java port for imgui-java 1.90.0
// Original: https://github.com/CedricGuillemet/ImHotKey
//
// The MIT License (MIT)
//
// Copyright (c) 2019 Cedric Guillemet
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions :
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

import imgui.ImColor;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImHotKey {
	public static final long NO_HOTKEY = -1L;
	private static final int NO_KEY = 0xFFFF;
	private static final int ACTIVE_COLOR = ImColor.rgba(30, 70, 230, 255);
	private static final int INACTIVE_COLOR = ImColor.rgba(0, 0, 0, 90);

	private final KeyDef[][] KEYS = {
		{
			new KeyDef("Esc", ImGuiKey.Escape, 4, 18),
			new KeyDef("F1", ImGuiKey.F1, 5, 18), new KeyDef("F2", ImGuiKey.F2, 6),
			new KeyDef("F3", ImGuiKey.F3, 7), new KeyDef("F4", ImGuiKey.F4, 8),
			new KeyDef("F5", ImGuiKey.F5, 9, 24), new KeyDef("F6", ImGuiKey.F6, 10),
			new KeyDef("F7", ImGuiKey.F7, 11), new KeyDef("F8", ImGuiKey.F8, 12),
			new KeyDef("F9", ImGuiKey.F9, 13, 24), new KeyDef("F10", ImGuiKey.F10, 14),
			new KeyDef("F11", ImGuiKey.F11, 15), new KeyDef("F12", ImGuiKey.F12, 16),
			new KeyDef("PrSn", ImGuiKey.PrintScreen, 17, 24),
			new KeyDef("ScLk", ImGuiKey.ScrollLock, 18),
			new KeyDef("Brk", ImGuiKey.Pause, 19)
		},
		{
			new KeyDef("~", ImGuiKey.GraveAccent, 20),
			new KeyDef("1", ImGuiKey._1, 21), new KeyDef("2", ImGuiKey._2, 22),
			new KeyDef("3", ImGuiKey._3, 23), new KeyDef("4", ImGuiKey._4, 24),
			new KeyDef("5", ImGuiKey._5, 25), new KeyDef("6", ImGuiKey._6, 26),
			new KeyDef("7", ImGuiKey._7, 27), new KeyDef("8", ImGuiKey._8, 28),
			new KeyDef("9", ImGuiKey._9, 29), new KeyDef("0", ImGuiKey._0, 30),
			new KeyDef("-", ImGuiKey.Minus, 31), new KeyDef("+", ImGuiKey.Equal, 32),
			new KeyDef("Backspace", ImGuiKey.Backspace, 33, 0.0f, 80.0f),
			new KeyDef("Ins", ImGuiKey.Insert, 34, 24),
			new KeyDef("Hom", ImGuiKey.Home, 35), new KeyDef("PgU", ImGuiKey.PageUp, 36)
		},
		{
			new KeyDef("Tab", ImGuiKey.Tab, 3, 0.0f, 60.0f),
			new KeyDef("Q", ImGuiKey.Q, 37), new KeyDef("W", ImGuiKey.W, 38),
			new KeyDef("E", ImGuiKey.E, 39), new KeyDef("R", ImGuiKey.R, 40),
			new KeyDef("T", ImGuiKey.T, 41), new KeyDef("Y", ImGuiKey.Y, 42),
			new KeyDef("U", ImGuiKey.U, 43), new KeyDef("I", ImGuiKey.I, 44),
			new KeyDef("O", ImGuiKey.O, 45), new KeyDef("P", ImGuiKey.P, 46),
			new KeyDef("[", ImGuiKey.LeftBracket, 47), new KeyDef("]", ImGuiKey.RightBracket, 48),
			new KeyDef("|", ImGuiKey.Backslash, 49, 0.0f, 60.0f),
			new KeyDef("Del", ImGuiKey.Delete, 50, 24),
			new KeyDef("End", ImGuiKey.End, 51), new KeyDef("PgD", ImGuiKey.PageDown, 52)
		},
		{
			new KeyDef("Caps Lock", ImGuiKey.CapsLock, 53, 0.0f, 80.0f),
			new KeyDef("A", ImGuiKey.A, 54), new KeyDef("S", ImGuiKey.S, 55),
			new KeyDef("D", ImGuiKey.D, 56), new KeyDef("F", ImGuiKey.F, 57),
			new KeyDef("G", ImGuiKey.G, 58), new KeyDef("H", ImGuiKey.H, 59),
			new KeyDef("J", ImGuiKey.J, 60), new KeyDef("K", ImGuiKey.K, 61),
			new KeyDef("L", ImGuiKey.L, 62), new KeyDef(";", ImGuiKey.Semicolon, 63),
			new KeyDef("'", ImGuiKey.Apostrophe, 64), new KeyDef("Ret", ImGuiKey.Enter, 65, 0.0f, 84.0f)
		},
		{
			new KeyDef("Shift", ImGuiKey.LeftShift, 2, 0.0f, 104.0f),
			new KeyDef("Z", ImGuiKey.Z, 66), new KeyDef("X", ImGuiKey.X, 67),
			new KeyDef("C", ImGuiKey.C, 68), new KeyDef("V", ImGuiKey.V, 69),
			new KeyDef("B", ImGuiKey.B, 70), new KeyDef("N", ImGuiKey.N, 71),
			new KeyDef("M", ImGuiKey.M, 72), new KeyDef(",", ImGuiKey.Comma, 73),
			new KeyDef(".", ImGuiKey.Period, 74), new KeyDef("/", ImGuiKey.Slash, 75),
			new KeyDef("Shift", ImGuiKey.RightShift, 2, 0.0f, 104.0f),
			new KeyDef("Up", ImGuiKey.UpArrow, 76, 68)
		},
		{
			new KeyDef("Ctrl", ImGuiKey.LeftCtrl, 0, 0.0f, 60.0f),
			new KeyDef("Alt", ImGuiKey.LeftAlt, 1, 68, 60.0f),
			new KeyDef("Space", ImGuiKey.Space, 77, 0.0f, 260.0f),
			new KeyDef("Alt", ImGuiKey.RightAlt, 1, 0.0f, 60.0f),
			new KeyDef("Ctrl", ImGuiKey.RightCtrl, 0, 68, 60.0f),
			new KeyDef("Left", ImGuiKey.LeftArrow, 78, 24),
			new KeyDef("Down", ImGuiKey.DownArrow, 79), new KeyDef("Right", ImGuiKey.RightArrow, 80)
		}
	};
	private final List<KeyDef> ALL_KEYS = new ArrayList<>();
	private final Map<Integer, Boolean> keyDown = new HashMap<>();
	private int editingHotkey = -1;
	private long lastHotKey = NO_HOTKEY;
	private Runnable onHotKeySet;

	public ImHotKey() {
		for (KeyDef[] row : KEYS) {
			ALL_KEYS.addAll(Arrays.asList(row));
		}
	}

	public void setOnHotKeySet(Runnable r) {
		this.onHotKeySet = r;
	}

	private KeyDef findKeyDef(int imguiKey) {
		for (KeyDef def : ALL_KEYS) {
			if (def.imguiKey == imguiKey) {
				return def;
			}
		}
		return null;
	}

	private int[] unpackKeys(long packed) {
		int[] out = new int[4];
		for (int i = 0; i < 4; i++) {
			out[i] = (int) ((packed >> (16 * i)) & 0xFFFFL);
		}
		return out;
	}

	private long packKeys(List<Integer> keys) {
		long result = NO_HOTKEY;
		for (int i = 0; i < keys.size() && i < 4; i++) {
			long slot = ((long) keys.get(i) & 0xFFFFL) << (16 * i);
			long mask = ~(0xFFFFL << (16 * i));
			result = (result & mask) | slot;
		}
		return result;
	}

	private long orderedPack(List<Integer> pressedKeys) {
		List<Integer> sorted = new ArrayList<>(pressedKeys);
		sorted.sort((a, b) -> {
			KeyDef da = findKeyDef(a);
			KeyDef db = findKeyDef(b);
			int oa = da != null ? da.order : Integer.MAX_VALUE;
			int ob = db != null ? db.order : Integer.MAX_VALUE;
			return Integer.compare(oa, ob);
		});
		return packKeys(sorted);
	}

	/**
	 * Builds a display string such as "Ctrl + Shift + S", or "Save (Ctrl + Shift + S)" if functionName is non-null.
	 */
	public String getHotKeyLabel(long functionKeys, @Nullable Component functionName) {
		int[] keys = unpackKeys(functionKeys);
		List<String> parts = new ArrayList<>();
		for (int k : keys) {
			if (k == NO_KEY) {
				continue;
			}
			KeyDef def = findKeyDef(k);
			if (def != null) {
				parts.add(def.label);
			}
		}
		if (parts.isEmpty()) {
			return functionName != null ? functionName.getString() : "";
		}
		String combo = String.join(" + ", parts);
		return functionName != null ? functionName.getString() + " (" + combo + ")" : combo;
	}

	/**
	 * Renders the hotkey editor inline (inside a panel or window).
	 * The editor is rendered only when hotkeys.length &gt; 0.
	 */
	public void render(HotKey[] hotkeys) {
		if (hotkeys.length == 0) {
			return;
		}

		ImGui.beginChild("HotKeyList", 320, -1, true);
		for (int i = 0; i < hotkeys.length; i++) {
			String label = getHotKeyLabel(hotkeys[i].functionKeys, hotkeys[i].functionName);
			boolean selected = editingHotkey == i;
			if (ImGui.selectable(label, selected) || editingHotkey == -1) {
				editingHotkey = i;
				keyDown.clear();
				for (int k : unpackKeys(hotkeys[editingHotkey].functionKeys)) {
					if (k != NO_KEY) {
						keyDown.put(k, true);
					}
				}
			}
		}
		ImGui.endChild();

		float availWidth = ImGui.getWindowWidth() - ImGui.getStyle().getWindowPaddingX() * 2;
		float rightSectionWidth = 850;
		float posX = availWidth - rightSectionWidth;
		if (posX < 250.0f) {
			posX = 250.0f;
		}
		ImGui.sameLine(posX);
		ImGui.beginGroup();

		for (KeyDef[] row : KEYS) {
			ImGui.beginGroup();
			for (int x = 0; x < row.length; x++) {
				KeyDef key = row[x];
				float ofs = key.offset + (x != 0 ? 4.0f : 0.0f);
				if (x != 0) {
					ImGui.sameLine(0.0f, ofs);
				} else if (ofs >= 1.0f) {
					ImGui.indent(ofs);
				}

				boolean down = keyDown.getOrDefault(key.imguiKey, false);
				ImGui.pushStyleColor(ImGuiCol.Button, down ? ACTIVE_COLOR : INACTIVE_COLOR);
				if (ImGui.button(key.label, key.width, 40)) {
					keyDown.put(key.imguiKey, !down);
				}
				ImGui.popStyleColor();
			}
			ImGui.endGroup();
			if (row[0].offset >= 1.0f) {
				ImGui.unindent(row[0].offset);
			}
		}

		ImGui.invisibleButton("space", 10, 70);
		ImGui.beginChild("HotKeyDescription", 540, 60, true);
		ImGui.text(hotkeys[editingHotkey].functionName.getString() + " :");
		ImGui.sameLine();
		ImGui.textWrapped(hotkeys[editingHotkey].functionLib);
		ImGui.endChild();
		ImGui.sameLine();

		int keyDownCount = 0;
		for (boolean d : keyDown.values()) {
			if (d) {
				keyDownCount++;
			}
		}

		if (ImGui.button("Clear", 80, 40)) {
			keyDown.clear();
			hotkeys[editingHotkey].functionKeys = NO_HOTKEY;
			if (onHotKeySet != null) {
				onHotKeySet.run();
			}
		}
		ImGui.sameLine();

		if (keyDownCount > 0 && keyDownCount < 5) {
			if (ImGui.button("Set", 80, 40)) {
				List<Integer> pressed = new ArrayList<>();
				for (Map.Entry<Integer, Boolean> e : keyDown.entrySet()) {
					if (e.getValue()) {
						pressed.add(e.getKey());
					}
				}
				hotkeys[editingHotkey].functionKeys = orderedPack(pressed);
				if (onHotKeySet != null) {
					onHotKeySet.run();
				}
			}
			ImGui.sameLine(0.0f, 20.0f);
		} else {
			ImGui.sameLine(0.0f, 100.0f);
		}
		ImGui.endGroup();
	}

	/**
	 * Renders the hotkey editor inside a named modal popup.
	 * Call {@code ImGui.openPopup(popupModal)} to open it.
	 */
	public void edit(HotKey[] hotkeys, String popupModal) {
		if (hotkeys.length == 0) {
			return;
		}

		ImGui.setNextWindowSize(1060, 400);
		if (!ImGui.beginPopupModal(popupModal, ImGuiWindowFlags.NoResize)) {
			return;
		}

		render(hotkeys);

		ImGui.sameLine(0.0f, 200.0f);
		if (ImGui.button("Done", 80, 40)) {
			ImGui.closeCurrentPopup();
		}
		ImGui.endPopup();
	}

	/**
	 * Returns the index of the hotkey currently held down, or -1 if none matches.
	 */
	public int getHotKey(HotKey[] hotkeys) {
		List<Integer> pressed = new ArrayList<>();
		for (KeyDef def : ALL_KEYS) {
			if (ImGui.isKeyDown(def.imguiKey)) {
				pressed.add(def.imguiKey);
				if (pressed.size() == 4) {
					break;
				}
			}
		}

		long newHotKey = orderedPack(pressed);

		if (!pressed.isEmpty()) {
			if (newHotKey != lastHotKey) {
				for (int i = 0; i < hotkeys.length; i++) {
					if (hotkeys[i].functionKeys == newHotKey) {
						lastHotKey = newHotKey;
						return i;
					}
				}
				lastHotKey = NO_HOTKEY;
			}
			return -1;
		}

		lastHotKey = NO_HOTKEY;
		return -1;
	}

	/**
	 * Packs one or more ImGuiKey values into the ordered bitfield format used by getHotKey().
	 * Keys are sorted by keyboard layout order for consistent matching.
	 */
	public long pack(int... imguiKeys) {
		List<Integer> list = new ArrayList<>();
		for (int k : imguiKeys) {
			if (k != ImGuiKey.None) {
				list.add(k);
			}
		}
		return orderedPack(list);
	}

	/**
	 * Returns true on the frame a single HotKey is pressed (fire-and-forget convenience).
	 */
	public boolean isPressed(HotKey hotkey) {
		return getHotKey(new HotKey[]{hotkey}) >= 0;
	}

	public static final class HotKey {
		public Component functionName;
		public String functionLib;
		public long functionKeys;

		public HotKey(String functionName, String functionLib) {
			this(Component.literal(functionName), functionLib, NO_HOTKEY);
		}

		public HotKey(Component functionName, String functionLib) {
			this(functionName, functionLib, NO_HOTKEY);
		}

		public HotKey(String functionName, String functionLib, long functionKeys) {
			this(Component.literal(functionName), functionLib, functionKeys);
		}

		public HotKey(Component functionName, String functionLib, long functionKeys) {
			this.functionName = functionName;
			this.functionLib = functionLib;
			this.functionKeys = functionKeys;
		}
	}

	public static final class KeyDef {
		final String label;
		final int imguiKey;
		final int order;
		final float offset;
		final float width;

		KeyDef(String label, int imguiKey, int order) {
			this(label, imguiKey, order, 0.0f, 40.0f);
		}

		KeyDef(String label, int imguiKey, int order, float offset) {
			this(label, imguiKey, order, offset, 40.0f);
		}

		KeyDef(String label, int imguiKey, int order, float offset, float width) {
			this.label = label;
			this.imguiKey = imguiKey;
			this.order = order;
			this.offset = offset;
			this.width = width;
		}
	}
}
