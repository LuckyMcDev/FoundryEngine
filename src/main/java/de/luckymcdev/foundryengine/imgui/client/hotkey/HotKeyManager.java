package de.luckymcdev.foundryengine.imgui.client.hotkey;

import com.mojang.blaze3d.platform.InputConstants;
import de.luckymcdev.foundryengine.editor.client.panel.Panel;
import de.luckymcdev.foundryengine.util.client.ImGuiGLFWKeyThing;
import de.luckymcdev.foundryengine.manager.api.Persistable;
import de.luckymcdev.foundryengine.savedata.common.SavedDataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.intellij.lang.annotations.MagicConstant;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HotKeyManager implements Persistable {
	public static final String SAVE_SECTION = "hotkeys";
	private final ImHotKey imHotKey = new ImHotKey();
	private final ImHotKey execImHotKey = new ImHotKey();
	private final List<RegisteredHotKey> registry = new ArrayList<>();
	private ImHotKey.HotKey[] cachedDisplayArray = new ImHotKey.HotKey[0];
	private ImHotKey.HotKey[] cachedExecutableArray = new ImHotKey.HotKey[0];
	private int[] cachedExecutableMap = new int[0];
	private boolean needsRebuild = false;
	private CompoundTag pendingHotkeySection = new CompoundTag();


	public ImHotKey getImHotKey() {
		return imHotKey;
	}

	public ImHotKey.HotKey[] getHotkeys() {
		if (needsRebuild) {
			rebuildCaches();
		}
		return cachedDisplayArray;
	}

	public RegisteredHotKey register(Identifier id, String name, String description, @Nullable Runnable action, @MagicConstant(flagsFromClass = InputConstants.class) int... keys) {
		remove(id);
		long packed = pack(keys);
		ImHotKey.HotKey hotKey = new ImHotKey.HotKey(Component.literal(name), description, packed);
		RegisteredHotKey entry = new RegisteredHotKey(id, hotKey, action);

		registry.add(entry);
		applyPersisted(entry);
		needsRebuild = true;
		return entry;
	}


	public void remove(Identifier id) {
		registry.removeIf(e -> e.id().equals(id));
		needsRebuild = true;
	}


	public void registerPanel(Panel panel, Runnable toggleAction) {
		Identifier id = panel.getId();
		remove(id);
		ImHotKey.HotKey hk = panel.getShortcut();
		if (hk == null) {
			hk = new ImHotKey.HotKey(panel.getLabel(), "Toggle " + panel.getLabel().getString());
		}
		RegisteredHotKey entry = new RegisteredHotKey(id, hk, toggleAction);
		registry.add(entry);
		applyPersisted(entry);
		needsRebuild = true;
	}

	public ImHotKey.@Nullable HotKey getHotKeyById(Identifier id) {
		for (RegisteredHotKey entry : registry) {
			if (entry.id().equals(id)) {
				return entry.hotKey();
			}
		}
		return null;
	}

	public void update() {
		if (registry.isEmpty()) {
			return;
		}

		if (needsRebuild) {
			rebuildCaches();
		}

		int activeIndex = execImHotKey.getHotKey(cachedExecutableArray);
		if (activeIndex != -1 && activeIndex < cachedExecutableMap.length) {
			registry.get(cachedExecutableMap[activeIndex]).action().run();
		}
	}

	@Override
	public String saveSection() {
		return SAVE_SECTION;
	}

	@Override
	public SavedDataManager.Scope saveScope() {
		return SavedDataManager.Scope.GLOBAL;
	}

	@Override
	public CompoundTag saveData() {
		CompoundTag section = new CompoundTag();
		for (RegisteredHotKey entry : registry) {
			section.putLong(entry.id().toString(), entry.hotKey().functionKeys);
		}
		return section;
	}

	@Override
	public void loadData(CompoundTag data) {
		pendingHotkeySection = data.copy();
		for (RegisteredHotKey entry : registry) {
			applyPersisted(entry);
		}
	}

	private void applyPersisted(RegisteredHotKey entry) {
		String key = entry.id().toString();
		pendingHotkeySection.getLong(key).ifPresent(v -> entry.hotKey().functionKeys = v);
	}


	public long pack(int... glfwKeys) {
		int[] imguiKeys = new int[glfwKeys.length];
		for (int i = 0; i < glfwKeys.length; i++) {
			imguiKeys[i] = ImGuiGLFWKeyThing.glfwKeyToImGuiKey(glfwKeys[i]);
		}
		return imHotKey.pack(imguiKeys);
	}

	private void rebuildCaches() {
		cachedDisplayArray = registry.stream()
			.map(RegisteredHotKey::hotKey)
			.toArray(ImHotKey.HotKey[]::new);

		List<ImHotKey.HotKey> execList = new ArrayList<>();
		List<Integer> execIndexList = new ArrayList<>();
		for (int i = 0; i < registry.size(); i++) {
			RegisteredHotKey entry = registry.get(i);
			if (entry.action() != null) {
				execList.add(entry.hotKey());
				execIndexList.add(i);
			}
		}
		cachedExecutableArray = execList.toArray(ImHotKey.HotKey[]::new);
		cachedExecutableMap = execIndexList.stream().mapToInt(Integer::intValue).toArray();
		needsRebuild = false;
	}

	public record RegisteredHotKey(Identifier id, ImHotKey.HotKey hotKey, Runnable action) {
	}
}
