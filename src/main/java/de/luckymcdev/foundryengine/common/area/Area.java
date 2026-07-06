package de.luckymcdev.foundryengine.common.area;

import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Area {
	public static final Color DEFAULT_COLOR = new Color(255, 68, 68);

	private final Identifier id;
	private final ResourceKey<Level> dimension;
	private final List<Identifier> moduleIds = new ArrayList<>();
	private final Map<Identifier, CompoundTag> moduleData = new HashMap<>();
	private final Map<String, Identifier> linkedAreas = new HashMap<>();
	private Color color;

	protected Area(Identifier id, ResourceKey<Level> dimension, Color color) {
		this.id = id;
		this.dimension = dimension;
		this.color = color;
	}

	public static Area readFromNbt(CompoundTag tag) {
		String type = tag.getString("type").orElse("aabb");
		Area area = switch (type) {
			case "block" -> BlockArea.readFromNbt(tag);
			default -> AABBArea.readFromNbt(tag);
		};

		ListTag modList = tag.getListOrEmpty("modules");
		for (int i = 0; i < modList.size(); i++) {
			modList.getString(i).ifPresent(s -> area.addModule(Identifier.parse(s)));
		}

		CompoundTag dataTag = tag.getCompound("moduleData").orElse(new CompoundTag());
		for (String key : dataTag.keySet()) {
			dataTag.getCompound(key).ifPresent(ct -> area.setModuleData(Identifier.parse(key), ct));
		}

		CompoundTag linkTag = tag.getCompound("linkedAreas").orElse(new CompoundTag());
		for (String key : linkTag.keySet()) {
			linkTag.getString(key).ifPresent(value -> area.linkArea(key, Identifier.parse(value)));
		}

		return area;
	}

	public Identifier id() {
		return id;
	}

	public ResourceKey<Level> dimension() {
		return dimension;
	}

	public Color color() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public abstract AABB bounds();

	public abstract boolean contains(GlobalPos position);

	public abstract boolean contains(BlockPos pos);

	public abstract boolean contains(Vec3 pos);

	public abstract boolean contains(double x, double y, double z);

	public abstract void drawDebugOutline();

	public abstract CompoundTag writeToNbt();

	public List<Identifier> moduleIds() {
		return Collections.unmodifiableList(moduleIds);
	}

	public void addModule(Identifier moduleId) {
		if (!moduleIds.contains(moduleId)) {
			moduleIds.add(moduleId);
		}
	}

	public void removeModule(Identifier moduleId) {
		moduleIds.remove(moduleId);
		moduleData.remove(moduleId);
	}

	public boolean hasModule(Identifier moduleId) {
		return moduleIds.contains(moduleId);
	}

	public void clearModules() {
		moduleIds.clear();
		moduleData.clear();
	}

	public Map<Identifier, CompoundTag> moduleData() {
		return Collections.unmodifiableMap(moduleData);
	}

	public CompoundTag getModuleData(Identifier moduleId) {
		return moduleData.computeIfAbsent(moduleId, k -> new CompoundTag());
	}

	public void setModuleData(Identifier moduleId, CompoundTag data) {
		moduleData.put(moduleId, data);
		if (!moduleIds.contains(moduleId)) {
			moduleIds.add(moduleId);
		}
	}

	public boolean hasModuleData(Identifier moduleId) {
		return moduleData.containsKey(moduleId);
	}

	public Map<String, Identifier> linkedAreas() {
		return Collections.unmodifiableMap(linkedAreas);
	}

	public void linkArea(String name, Identifier areaId) {
		linkedAreas.put(name, areaId);
	}

	public void unlinkArea(String name) {
		linkedAreas.remove(name);
	}

	@Nullable
	public Identifier getLinkedArea(String name) {
		return linkedAreas.get(name);
	}

	protected CompoundTag writeSharedNbt() {
		CompoundTag tag = new CompoundTag();
		tag.putString("id", id.toString());
		tag.putString("dimension", dimension.identifier().toString());
		tag.putInt("color", color.argb());

		if (!moduleIds.isEmpty()) {
			ListTag modList = new ListTag();
			for (Identifier mid : moduleIds) {
				modList.add(StringTag.valueOf(mid.toString()));
			}
			tag.put("modules", modList);
		}

		if (!moduleData.isEmpty()) {
			CompoundTag dataTag = new CompoundTag();
			for (var entry : moduleData.entrySet()) {
				dataTag.put(entry.getKey().toString(), entry.getValue());
			}
			tag.put("moduleData", dataTag);
		}

		if (!linkedAreas.isEmpty()) {
			CompoundTag linkTag = new CompoundTag();
			for (var entry : linkedAreas.entrySet()) {
				linkTag.putString(entry.getKey(), entry.getValue().toString());
			}
			tag.put("linkedAreas", linkTag);
		}

		return tag;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Area area)) {
			return false;
		}
		return id.equals(area.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
