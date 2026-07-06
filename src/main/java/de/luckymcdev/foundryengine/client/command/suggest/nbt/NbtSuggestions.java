package de.luckymcdev.foundryengine.client.command.suggest.nbt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NbtSuggestions {
	private static final Map<String, List<FieldDef>> ROOT_FIELDS = new LinkedHashMap<>();
	private static final Map<String, List<String>> ENUM_VALUES = new HashMap<>();

	public static void init() {
		registerBlockEntityFields();
		registerEntityFields();
		registerCommonFields();
		registerItemComponentFields();
		registerEnumValues();
		scanModdedRegistries();
	}

	public static @Nullable List<FieldDef> getFields(String type) {
		return ROOT_FIELDS.get(type);
	}

	public static @Nullable List<String> getEnumValues(String enumName) {
		return ENUM_VALUES.get(enumName);
	}

	public static String getFieldSubtext(String type, String fieldName) {
		List<FieldDef> fields = ROOT_FIELDS.get(type);
		if (fields == null) {
			return null;
		}
		for (FieldDef f : fields) {
			if (f.name().equals(fieldName)) {
				return f.subtext();
			}
		}
		return null;
	}

	private static void register(String key, FieldDef... fields) {
		ROOT_FIELDS.put(key, List.of(fields));
	}

	private static void register(String key, List<FieldDef> fields) {
		ROOT_FIELDS.put(key, fields);
	}

	private static void register(String key, String parentKey) {
		List<FieldDef> parentFields = ROOT_FIELDS.get(parentKey);
		if (parentFields != null) {
			register(key, parentFields);
		}
	}

	private static FieldDef common(String name, NbtType type, String desc) {
		return new FieldDef(name, type, desc);
	}

	private static List<FieldDef> commonBlockFields() {
		return List.of(
			common("CustomName", NbtType.STRING, "JSON text component"),
			common("Lock", NbtType.STRING, "lock key")
		);
	}

	private static List<FieldDef> commonEntityFields() {
		return List.of(
			common("Pos", NbtType.LIST, "[x, y, z]"),
			common("Rotation", NbtType.LIST, "[yaw, pitch]"),
			common("Motion", NbtType.LIST, "[dx, dy, dz]"),
			common("OnGround", NbtType.BOOLEAN, ""),
			common("NoGravity", NbtType.BOOLEAN, ""),
			common("FallDistance", NbtType.FLOAT, ""),
			common("Fire", NbtType.SHORT, "ticks on fire"),
			common("Air", NbtType.SHORT, "ticks of air"),
			common("CustomName", NbtType.STRING, "JSON text component"),
			common("CustomNameVisible", NbtType.BOOLEAN, ""),
			common("Silent", NbtType.BOOLEAN, ""),
			common("Invulnerable", NbtType.BOOLEAN, ""),
			common("Glowing", NbtType.BOOLEAN, ""),
			common("Tags", NbtType.LIST, "string list"),
			common("Command", NbtType.STRING, "command to run"),
			common("Passengers", NbtType.LIST, "entity list"),
			common("id", NbtType.STRING, "entity identifier"),
			common("UUID", NbtType.UUID, "")
		);
	}

	private static void registerBlockEntityFields() {
		FieldDef[] furnace = {
			common("Items", NbtType.LIST, "item stack list"),
			common("RecipesUsed", NbtType.COMPOUND, "recipe -> count map"),
			common("CookTimeTotal", NbtType.INT, "total cook time"),
			common("CookTime", NbtType.INT, "cook progress"),
			common("BurnTime", NbtType.INT, "fuel remaining"),
			common("LastRecipe", NbtType.STRING, "recipe ID"),
			common("CustomName", NbtType.STRING, ""),
			common("Lock", NbtType.STRING, ""),
		};
		register("block/minecraft:furnace", furnace);
		register("block/minecraft:blast_furnace", furnace);
		register("block/minecraft:smoker", furnace);

		register("block/minecraft:chest",
			common("Items", NbtType.LIST, "item stack list"),
			common("LootTable", NbtType.STRING, "loot table ID"),
			common("LootTableSeed", NbtType.LONG, ""),
			common("CustomName", NbtType.STRING, ""),
			common("Lock", NbtType.STRING, "")
		);
		register("block/minecraft:barrel", "chest");
		register("block/minecraft:shulker_box", "chest");
		register("block/minecraft:hopper",
			common("Items", NbtType.LIST, "item stack list"),
			common("TransferCooldown", NbtType.INT, ""),
			common("CustomName", NbtType.STRING, ""),
			common("Lock", NbtType.STRING, "")
		);
		register("block/minecraft:dispenser",
			common("Items", NbtType.LIST, "item stack list"),
			common("CustomName", NbtType.STRING, ""),
			common("Lock", NbtType.STRING, "")
		);
		register("block/minecraft:dropper", "block/minecraft:dispenser");

		register("block/minecraft:mob_spawner",
			common("SpawnData", NbtType.COMPOUND, "spawn entry"),
			common("SpawnPotentials", NbtType.LIST, "spawn potential list"),
			common("SpawnCount", NbtType.SHORT, ""),
			common("SpawnRange", NbtType.SHORT, ""),
			common("RequiredPlayerRange", NbtType.SHORT, ""),
			common("Delay", NbtType.SHORT, ""),
			common("MinSpawnDelay", NbtType.SHORT, ""),
			common("MaxSpawnDelay", NbtType.SHORT, ""),
			common("MaxNearbyEntities", NbtType.SHORT, ""),
			common("SpawnData", NbtType.COMPOUND, "entity NBT"),
			common("CustomName", NbtType.STRING, "")
		);

		register("block/minecraft:command_block",
			common("Command", NbtType.STRING, ""),
			common("SuccessCount", NbtType.INT, ""),
			common("TrackOutput", NbtType.BOOLEAN, ""),
			common("LastOutput", NbtType.STRING, "JSON text"),
			common("powered", NbtType.BOOLEAN, ""),
			common("auto", NbtType.BOOLEAN, ""),
			common("conditionMet", NbtType.BOOLEAN, ""),
			common("UpdateLastExecution", NbtType.BOOLEAN, ""),
			common("LastExecution", NbtType.LONG, ""),
			common("CustomName", NbtType.STRING, "")
		);

		register("block/minecraft:beacon",
			common("Levels", NbtType.INT, ""),
			common("Primary", NbtType.INT, "effect ID"),
			common("Secondary", NbtType.INT, "effect ID"),
			common("Payment", NbtType.COMPOUND, "item for payment"),
			common("CustomName", NbtType.STRING, "")
		);

		register("block/minecraft:skull",
			new FieldDef("SkullOwner", NbtType.COMPOUND, "player profile", List.of(
				new FieldDef("Id", NbtType.UUID, ""),
				new FieldDef("Name", NbtType.STRING, "player name"),
				new FieldDef("Properties", NbtType.COMPOUND, "texture properties")
			)),
			common("CustomName", NbtType.STRING, ""),
			common("note_block_sound", NbtType.STRING, "sound event ID")
		);
		register("block/minecraft:player_head", "block/minecraft:skull");

		List<FieldDef> signTextChildren = List.of(
			new FieldDef("messages", NbtType.LIST, "string[4]"),
			new FieldDef("color", NbtType.ENUM, "DyeColor"),
			new FieldDef("has_glowing_text", NbtType.BOOLEAN, "")
		);
		register("block/minecraft:sign",
			common("Text1", NbtType.STRING, "JSON text"),
			common("Text2", NbtType.STRING, "JSON text"),
			common("Text3", NbtType.STRING, "JSON text"),
			common("Text4", NbtType.STRING, "JSON text"),
			common("Color", NbtType.ENUM, "color"),
			common("GlowingText", NbtType.BOOLEAN, ""),
			new FieldDef("front_text", NbtType.COMPOUND, "", signTextChildren),
			new FieldDef("back_text", NbtType.COMPOUND, "", signTextChildren)
		);

		register("block/minecraft:jukebox",
			common("RecordItem", NbtType.COMPOUND, "music disc item")
		);

		register("block/minecraft:enchanting_table",
			common("CustomName", NbtType.STRING, "")
		);

		register("block/minecraft:brewing_stand",
			common("Items", NbtType.LIST, "item stack list"),
			common("BrewTime", NbtType.INT, ""),
			common("Fuel", NbtType.INT, ""),
			common("CustomName", NbtType.STRING, ""),
			common("Lock", NbtType.STRING, "")
		);

		register("block/minecraft:piston_head", common("blockId", NbtType.STRING, "block ID"));
		register("block/minecraft:piston",
			common("blockId", NbtType.STRING, ""),
			common("Progress", NbtType.FLOAT, ""),
			common("Extending", NbtType.BOOLEAN, ""),
			common("source", NbtType.BOOLEAN, "")
		);

		register("block/minecraft:note_block",
			common("note", NbtType.BYTE, "pitch 0-24"),
			common("powered", NbtType.BOOLEAN, ""),
			common("instrument", NbtType.ENUM, "instrument")
		);

		register("block/minecraft:comparator",
			common("OutputSignal", NbtType.INT, "")
		);

		register("block/minecraft:daylight_detector",
			common("power", NbtType.INT, "")
		);

		register("block/minecraft:beehive",
			common("Bees", NbtType.LIST, "bee data list"),
			common("honey_level", NbtType.INT, ""),
			common("flower_pos", NbtType.COMPOUND, "flower position"),
			common("sedated", NbtType.BOOLEAN, "")
		);

		register("block/minecraft:campfire",
			common("Items", NbtType.LIST, "item stack list"),
			common("CookingTimes", NbtType.INT_ARRAY, ""),
			common("CookingTotalTimes", NbtType.INT_ARRAY, "")
		);

		register("block/minecraft:chiseled_bookshelf",
			common("Items", NbtType.LIST, "book item list"),
			common("last_interacted_slot", NbtType.INT, "")
		);

		register("block/minecraft:decorated_pot",
			common("sherds", NbtType.LIST, "sherd item list"),
			common("item", NbtType.COMPOUND, "contained item")
		);

		register("block/minecraft:sculk_catalyst",
			common("cursors", NbtType.LIST, "sculk cursor list")
		);

		register("block/minecraft:sculk_shrieker",
			common("shrieker_sound", NbtType.BOOLEAN, ""),
			common("shrieker_sound_cooldown", NbtType.INT, ""),
			common("warning_level", NbtType.INT, "")
		);

		register("block/minecraft:calibrated_sculk_sensor",
			common("input_signal", NbtType.INT, ""),
			common("power", NbtType.INT, "")
		);

		register("block/minecraft:sculk_sensor",
			common("power", NbtType.INT, ""),
			common("last_vibration_frequency", NbtType.INT, "")
		);

		register("block/minecraft:end_gateway",
			common("age", NbtType.LONG, ""),
			common("exact_teleport", NbtType.BOOLEAN, ""),
			common("exit_portal", NbtType.COMPOUND, "position")
		);

		register("block/minecraft:end_portal",
			common("age", NbtType.LONG, "")
		);

		register("block/minecraft:bed",
			common("color", NbtType.ENUM, "DyeColor")
		);

		register("block/minecraft:conduit",
			common("target_uuid", NbtType.UUID, ""),
			common("active", NbtType.BOOLEAN, "")
		);

		register("block/minecraft:trapped_chest", "block/minecraft:chest");
		register("block/minecraft:ender_chest",
			common("CustomName", NbtType.STRING, ""),
			common("Lock", NbtType.STRING, "")
		);

		register("block/minecraft:structure_block",
			common("name", NbtType.STRING, "structure name"),
			common("author", NbtType.STRING, ""),
			common("metadata", NbtType.STRING, ""),
			common("posX", NbtType.INT, ""),
			common("posY", NbtType.INT, ""),
			common("posZ", NbtType.INT, ""),
			common("sizeX", NbtType.INT, ""),
			common("sizeY", NbtType.INT, ""),
			common("sizeZ", NbtType.INT, ""),
			common("rotation", NbtType.ENUM, "rotation"),
			common("mirror", NbtType.ENUM, "mirror"),
			common("mode", NbtType.ENUM, "mode"),
			common("powered", NbtType.BOOLEAN, ""),
			common("ignoreEntities", NbtType.BOOLEAN, ""),
			common("showair", NbtType.BOOLEAN, ""),
			common("showboundingbox", NbtType.BOOLEAN, ""),
			common("integrity", NbtType.FLOAT, ""),
			common("seed", NbtType.LONG, "")
		);

		register("block/minecraft:brushable_block",
			common("LootTable", NbtType.STRING, ""),
			common("LootTableSeed", NbtType.LONG, ""),
			common("brushed_state", NbtType.INT, ""),
			common("item", NbtType.COMPOUND, "contained item")
		);

		register("block/minecraft:trial_spawner",
			common("normal_config", NbtType.COMPOUND, ""),
			common("ominous_config", NbtType.COMPOUND, ""),
			common("spawn_data", NbtType.COMPOUND, ""),
			common("registered_players", NbtType.LIST, "UUID list"),
			common("cooldown_length", NbtType.INT, ""),
			common("total_mobs_spawned", NbtType.INT, ""),
			common("simulated_players", NbtType.INT, ""),
			common("players_per_slot", NbtType.INT, ""),
			common("required_player_range", NbtType.INT, ""),
			common("target_cooldown_length", NbtType.INT, ""),
			common("expulsion_range", NbtType.INT, ""),
			common("spawn_range", NbtType.INT, ""),
			common("ominous", NbtType.BOOLEAN, ""),
			common("stored_entities", NbtType.LIST, "")
		);

		register("block/minecraft:vault",
			common("config", NbtType.COMPOUND, ""),
			common("server_data", NbtType.COMPOUND, ""),
			common("shared_data", NbtType.COMPOUND, ""),
			common("ominous", NbtType.BOOLEAN, "")
		);

		register("block/minecraft:creaking_heart",
			common("creaking", NbtType.COMPOUND, "creaking entity data"),
			common("natural", NbtType.BOOLEAN, "")
		);
	}

	private static void registerEntityFields() {
		register("common/living",
			common("Health", NbtType.FLOAT, ""),
			common("AbsorptionAmount", NbtType.FLOAT, ""),
			common("HurtTime", NbtType.SHORT, ""),
			common("HurtByTimestamp", NbtType.INT, ""),
			common("DeathTime", NbtType.SHORT, ""),
			common("AttackTime", NbtType.SHORT, ""),
			common("PersistenceRequired", NbtType.BOOLEAN, ""),
			common("Leash", NbtType.COMPOUND, "leash holder UUID"),
			common("HandDropChances", NbtType.LIST, "float list"),
			common("ArmorDropChances", NbtType.LIST, "float list"),
			common("HandItems", NbtType.LIST, "item list"),
			common("ArmorItems", NbtType.LIST, "item list"),
			common("ActiveEffects", NbtType.LIST, "effect list"),
			common("Brain", NbtType.COMPOUND, "brain memory"),
			common("Attributes", NbtType.LIST, "attribute list"),
			common("CanPickUpLoot", NbtType.BOOLEAN, ""),
			common("CanBreakDoors", NbtType.BOOLEAN, ""),
			common("FromSpawner", NbtType.BOOLEAN, ""),
			common("LeftHanded", NbtType.BOOLEAN, "")
		);

		register("common/mob",
			common("PatrolTarget", NbtType.COMPOUND, "position"),
			common("PatrolLeader", NbtType.BOOLEAN, ""),
			common("Patrolling", NbtType.BOOLEAN, "")
		);

		register("entity/minecraft:zombie",
			common("IsBaby", NbtType.BOOLEAN, ""),
			common("CanBreakDoors", NbtType.BOOLEAN, ""),
			common("DrownedConversionTime", NbtType.INT, ""),
			common("ZombieType", NbtType.STRING, "")
		);

		register("entity/minecraft:skeleton",
			common("StrayConversionTime", NbtType.INT, "")
		);

		register("entity/minecraft:creeper",
			common("Fuse", NbtType.SHORT, ""),
			common("ExplosionRadius", NbtType.BYTE, ""),
			common("ignited", NbtType.BOOLEAN, ""),
			common("powered", NbtType.BOOLEAN, "")
		);

		register("entity/minecraft:spider",
			common("HasPassenger", NbtType.BOOLEAN, "")
		);

		register("entity/minecraft:enderman",
			common("carriedBlock", NbtType.COMPOUND, "block state")
		);

		register("entity/minecraft:witch",
			common("DrinkingPotion", NbtType.BOOLEAN, "")
		);

		register("entity/minecraft:villager",
			new FieldDef("VillagerData", NbtType.COMPOUND, "type/profession/level", List.of(
				new FieldDef("type", NbtType.STRING, "villager type ID"),
				new FieldDef("profession", NbtType.STRING, "profession ID", null),
				new FieldDef("level", NbtType.INT, "1-5")
			)),
			common("Gossips", NbtType.LIST, "gossip entries"),
			common("Inventory", NbtType.LIST, "item list"),
			common("Offers", NbtType.COMPOUND, "trade offers"),
			common("Xp", NbtType.INT, ""),
			common("LastRestock", NbtType.LONG, ""),
			common("RestocksToday", NbtType.INT, ""),
			common("Willing", NbtType.BOOLEAN, "")
		);

		register("entity/minecraft:iron_golem",
			common("PlayerCreated", NbtType.BOOLEAN, "")
		);

		register("entity/minecraft:snow_golem",
			common("PumpkinBit", NbtType.BOOLEAN, "")
		);

		register("entity/minecraft:wolf",
			common("CollarColor", NbtType.BYTE, "dye color ID"),
			common("Angry", NbtType.BOOLEAN, ""),
			common("Sitting", NbtType.BOOLEAN, ""),
			common("Trusting", NbtType.LIST, "UUID list")
		);

		register("entity/minecraft:cat",
			common("CollarColor", NbtType.BYTE, ""),
			common("Sitting", NbtType.BOOLEAN, ""),
			common("Trusting", NbtType.LIST, ""),
			common("variant", NbtType.ENUM, "CatVariant")
		);

		register("entity/minecraft:horse",
			common("ChestedHorse", NbtType.BOOLEAN, ""),
			common("HasArmor", NbtType.BOOLEAN, ""),
			common("HasSaddle", NbtType.BOOLEAN, ""),
			common("Tame", NbtType.BOOLEAN, ""),
			common("Items", NbtType.LIST, "item list"),
			common("variant", NbtType.INT, "horse variant"),
			common("armorItem", NbtType.COMPOUND, "armor item"),
			common("saddleItem", NbtType.COMPOUND, "saddle item")
		);
		register("entity/minecraft:donkey", "entity/minecraft:horse");
		register("entity/minecraft:mule", "entity/minecraft:horse");

		register("entity/minecraft:llama",
			common("ChestedHorse", NbtType.BOOLEAN, ""),
			common("Items", NbtType.LIST, ""),
			common("variant", NbtType.INT, "llama variant"),
			common("Strength", NbtType.INT, ""),
			common("DecorItem", NbtType.COMPOUND, "carpet item"),
			common("Tame", NbtType.BOOLEAN, "")
		);

		register("entity/minecraft:fox",
			common("Trusting", NbtType.LIST, "UUID list"),
			common("Sleeping", NbtType.BOOLEAN, ""),
			common("Sitting", NbtType.BOOLEAN, ""),
			common("Crouching", NbtType.BOOLEAN, ""),
			common("variant", NbtType.STRING, "red/snow")
		);

		register("entity/minecraft:bee",
			common("FlowerPos", NbtType.COMPOUND, "position"),
			common("HasNectar", NbtType.BOOLEAN, ""),
			common("HasStung", NbtType.BOOLEAN, ""),
			common("TicksSinceSting", NbtType.INT, ""),
			common("CannotEnterHiveTicks", NbtType.INT, ""),
			common("CropsGrown", NbtType.INT, ""),
			common("hive_pos", NbtType.COMPOUND, "hive position")
		);

		register("entity/minecraft:goat",
			common("IsScreamingGoat", NbtType.BOOLEAN, ""),
			common("HasLeftHorn", NbtType.BOOLEAN, ""),
			common("HasRightHorn", NbtType.BOOLEAN, "")
		);

		register("entity/minecraft:axolotl",
			common("Variant", NbtType.INT, ""),
			common("FromBucket", NbtType.BOOLEAN, "")
		);

		register("entity/minecraft:allay",
			common("Inventory", NbtType.LIST, "item list"),
			common("DuplicationCooldown", NbtType.LONG, ""),
			common("CanDuplicate", NbtType.BOOLEAN, ""),
			common("brain", NbtType.COMPOUND, "")
		);

		register("entity/minecraft:tnt",
			common("Fuse", NbtType.SHORT, ""),
			common("block_state", NbtType.COMPOUND, "")
		);

		register("entity/minecraft:falling_block",
			common("BlockState", NbtType.COMPOUND, ""),
			common("TileEntityData", NbtType.COMPOUND, "block entity data"),
			common("Time", NbtType.INT, ""),
			common("DropItem", NbtType.BOOLEAN, ""),
			common("HurtEntities", NbtType.BOOLEAN, ""),
			common("FallHurtMax", NbtType.INT, ""),
			common("FallHurtAmount", NbtType.FLOAT, ""),
			common("Data", NbtType.COMPOUND, "block entity data")
		);

		register("entity/minecraft:item",
			common("Item", NbtType.COMPOUND, "item stack"),
			common("PickupDelay", NbtType.SHORT, ""),
			common("Age", NbtType.SHORT, ""),
			common("Owner", NbtType.UUID, ""),
			common("Thrower", NbtType.UUID, ""),
			common("Health", NbtType.SHORT, "")
		);

		register("entity/minecraft:item_frame",
			common("Item", NbtType.COMPOUND, "displayed item"),
			common("Rotation", NbtType.BYTE, "0-7"),
			common("Invisible", NbtType.BOOLEAN, ""),
			common("Fixed", NbtType.BOOLEAN, "")
		);
		register("entity/minecraft:glow_item_frame", "entity/minecraft:item_frame");

		register("entity/minecraft:painting",
			common("variant", NbtType.STRING, "painting variant ID"),
			common("Motive", NbtType.STRING, "")
		);

		register("entity/minecraft:armor_stand",
			common("Marker", NbtType.BOOLEAN, ""),
			common("Invisible", NbtType.BOOLEAN, ""),
			common("NoBasePlate", NbtType.BOOLEAN, ""),
			new FieldDef("Pose", NbtType.COMPOUND, "pose angles", List.of(
				new FieldDef("Head", NbtType.LIST, "float[3]"),
				new FieldDef("Body", NbtType.LIST, "float[3]"),
				new FieldDef("LeftArm", NbtType.LIST, "float[3]"),
				new FieldDef("RightArm", NbtType.LIST, "float[3]"),
				new FieldDef("LeftLeg", NbtType.LIST, "float[3]"),
				new FieldDef("RightLeg", NbtType.LIST, "float[3]")
			)),
			common("ShowArms", NbtType.BOOLEAN, ""),
			common("Small", NbtType.BOOLEAN, ""),
			common("DisabledSlots", NbtType.INT, ""),
			common("ArmorItems", NbtType.LIST, ""),
			common("HandItems", NbtType.LIST, "")
		);

		register("entity/minecraft:boat",
			common("Type", NbtType.ENUM, "WoodType"),
			common("LeftEngineTank", NbtType.COMPOUND, ""),
			common("RightEngineTank", NbtType.COMPOUND, "")
		);

		register("entity/minecraft:chest_boat",
			common("LootTable", NbtType.STRING, ""),
			common("LootTableSeed", NbtType.LONG, ""),
			common("Items", NbtType.LIST, "")
		);

		register("entity/minecraft:minecart",
			common("CustomDisplayTile", NbtType.BOOLEAN, ""),
			common("DisplayState", NbtType.COMPOUND, "block state"),
			common("DisplayOffset", NbtType.INT, "")
		);
		register("entity/minecraft:chest_minecart",
			common("Items", NbtType.LIST, ""),
			common("LootTable", NbtType.STRING, ""),
			common("LootTableSeed", NbtType.LONG, "")
		);
		register("entity/minecraft:furnace_minecart",
			common("Fuel", NbtType.DOUBLE, ""),
			common("Push", NbtType.DOUBLE, ""),
			common("PushDir", NbtType.BOOLEAN, "")
		);
		register("entity/minecraft:hopper_minecart",
			common("Enabled", NbtType.BOOLEAN, ""),
			common("Items", NbtType.LIST, "")
		);
		register("entity/minecraft:spawner_minecart", common("SpawnData", NbtType.COMPOUND, ""));

		register("entity/minecraft:command_block_minecart",
			common("Command", NbtType.STRING, ""),
			common("SuccessCount", NbtType.INT, ""),
			common("TrackOutput", NbtType.BOOLEAN, ""),
			common("LastOutput", NbtType.STRING, "JSON text")
		);

		register("entity/minecraft:end_crystal",
			common("BeamTarget", NbtType.COMPOUND, "position"),
			common("ShowBottom", NbtType.BOOLEAN, "")
		);

		register("entity/minecraft:eye_of_ender",
			common("Item", NbtType.COMPOUND, "")
		);

		register("entity/minecraft:fireball",
			common("ExplosionPower", NbtType.INT, ""),
			common("power", NbtType.LIST, "velocity"),
			common("direction", NbtType.LIST, "velocity")
		);
		register("entity/minecraft:small_fireball", "entity/minecraft:fireball");
		register("entity/minecraft:dragon_fireball", "entity/minecraft:fireball");

		register("entity/minecraft:arrow",
			common("damage", NbtType.DOUBLE, ""),
			common("life", NbtType.SHORT, ""),
			common("color", NbtType.INT, "potion color"),
			common("inGround", NbtType.BOOLEAN, ""),
			common("crit", NbtType.BOOLEAN, ""),
			common("PierceLevel", NbtType.BYTE, ""),
			common("ShotFromCrossbow", NbtType.BOOLEAN, "")
		);
		register("entity/minecraft:spectral_arrow", "entity/minecraft:arrow");

		register("entity/minecraft:trident",
			common("damage", NbtType.DOUBLE, ""),
			common("Trident", NbtType.COMPOUND, "item"),
			common("DealtDamage", NbtType.BOOLEAN, ""),
			common("life", NbtType.SHORT, "")
		);

		register("entity/minecraft:potion",
			common("Item", NbtType.COMPOUND, "potion item"),
			common("Potion", NbtType.STRING, ""),
			common("color", NbtType.INT, "")
		);

		register("entity/minecraft:lingering_potion", "entity/minecraft:potion");
		register("entity/minecraft:splash_potion", "entity/minecraft:potion");

		register("entity/minecraft:ender_pearl",
			common("dangerous", NbtType.BOOLEAN, "")
		);

		register("entity/minecraft:experience_orb",
			common("value", NbtType.SHORT, ""),
			common("age", NbtType.SHORT, "")
		);

		register("entity/minecraft:area_effect_cloud",
			common("Duration", NbtType.INT, ""),
			common("WaitTime", NbtType.INT, ""),
			common("ReapplicationDelay", NbtType.INT, ""),
			common("DurationOnUse", NbtType.FLOAT, ""),
			common("Radius", NbtType.FLOAT, ""),
			common("RadiusOnUse", NbtType.FLOAT, ""),
			common("RadiusPerTick", NbtType.FLOAT, ""),
			common("Particle", NbtType.COMPOUND, "particle data"),
			common("Effects", NbtType.LIST, "effect list"),
			common("Color", NbtType.INT, ""),
			common("Age", NbtType.INT, ""),
			common("Owner", NbtType.UUID, "")
		);

		register("entity/minecraft:leash_knot", common("Facing", NbtType.BYTE, "direction"));
		register("entity/minecraft:fishing_bobber", common("angler", NbtType.UUID, "angler UUID"));

		register("entity/minecraft:lightning_bolt",
			common("powered", NbtType.BOOLEAN, ""),
			common("pos", NbtType.COMPOUND, "position")
		);

		register("entity/minecraft:wandering_trader",
			common("WanderTarget", NbtType.COMPOUND, "position"),
			common("DespawnDelay", NbtType.INT, ""),
			common("Inventory", NbtType.LIST, ""),
			common("Offers", NbtType.COMPOUND, "")
		);

		register("entity/minecraft:creaking",
			common("HomePos", NbtType.COMPOUND, "creaking heart position"),
			common("can_move", NbtType.BOOLEAN, ""),
			common("is_tether", NbtType.BOOLEAN, "")
		);

		register("entity/minecraft:breeze",
			common("WindCharges", NbtType.LIST, ""),
			common("AttackTarget", NbtType.COMPOUND, "")
		);

		register("entity/minecraft:breeze_wind_charge",
			common("power", NbtType.LIST, "velocity")
		);

		register("entity/minecraft:wind_charge",
			common("power", NbtType.LIST, "velocity")
		);

		register("entity/minecraft:bogged",
			common("Sheared", NbtType.BOOLEAN, ""),
			common("variant", NbtType.STRING, "mushroom variant")
		);
	}

	private static void registerCommonFields() {
		register("common/block", commonBlockFields());

		List<FieldDef> entityCommon = new ArrayList<>(commonEntityFields());
		register("common/entity", entityCommon);

		register("common/living", getFields("common/entity"));
		register("common/mob", List.of(
			common("PatrolTarget", NbtType.COMPOUND, "position"),
			common("PatrolLeader", NbtType.BOOLEAN, ""),
			common("Patrolling", NbtType.BOOLEAN, "")
		));

		register("common/item",
			common("Count", NbtType.BYTE, "stack size"),
			common("id", NbtType.STRING, "item ID"),
			common("components", NbtType.COMPOUND, "data components"),
			common("tag", NbtType.COMPOUND, "deprecated item tag")
		);

		register("common/block_item",
			common("BlockEntityTag", NbtType.COMPOUND, "block entity data"),
			common("BlockStateTag", NbtType.COMPOUND, "block state properties"),
			common("CanPlaceOn", NbtType.LIST, "block list"),
			common("CanDestroy", NbtType.LIST, "block list"),
			common("Items", NbtType.LIST, "container items")
		);

		register("common/spawn_egg_item",
			common("EntityTag", NbtType.COMPOUND, "entity NBT")
		);
	}

	private static void registerItemComponentFields() {
		Registry<DataComponentType<?>> registry = BuiltInRegistries.DATA_COMPONENT_TYPE;
		List<FieldDef> componentFields = new ArrayList<>();

		for (Map.Entry<ResourceKey<DataComponentType<?>>, DataComponentType<?>> entry : registry.entrySet()) {
			Identifier id = entry.getKey().identifier();
			componentFields.add(new FieldDef(id.toString(), NbtType.COMPOUND, "data component"));
		}

		register("item_components", componentFields);
	}

	private static void registerEnumValues() {
		var dyeColors = Arrays.stream(DyeColor.values())
			.map(DyeColor::getSerializedName).toList();
		ENUM_VALUES.put("DyeColor", dyeColors);
		ENUM_VALUES.put("color", dyeColors);
		ENUM_VALUES.put("Color", dyeColors);

		ENUM_VALUES.put("instrument", Arrays.stream(NoteBlockInstrument.values())
			.map(NoteBlockInstrument::getSerializedName).toList());

		ENUM_VALUES.put("rotation", Arrays.stream(Rotation.values())
			.map(Enum::name).toList());
		ENUM_VALUES.put("mirror", Arrays.stream(Mirror.values())
			.map(Enum::name).toList());
		ENUM_VALUES.put("mode", Arrays.stream(StructureMode.values())
			.map(Enum::name).toList());

		ENUM_VALUES.put("WoodType", List.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
			"mangrove", "cherry", "bamboo", "crimson", "warped"));
		ENUM_VALUES.put("Type", ENUM_VALUES.get("WoodType"));
		ENUM_VALUES.put("CatVariant", List.of("tabby", "tuxedo", "red", "siamese", "british", "calico",
			"persian", "ragdoll", "white", "jellie", "black", "all_black"));
		ENUM_VALUES.put("FrogVariant", List.of("temperate", "warm", "cold"));
		ENUM_VALUES.put("PaintingVariant", List.of("alban", "aztec", "aztec2", "bomb", "burning_skull",
			"bust", "courbet", "creebet", "donkey_kong", "earth", "fighters", "fire", "graham", "kebab",
			"match", "pigscene", "plant", "pointer", "pool", "sea", "skeleton", "skull_and_roses", "stage",
			"sunset", "void", "wanderer", "wasteland", "water", "wind", "wither"));
		ENUM_VALUES.put("Facing", List.of("down", "up", "north", "south", "west", "east"));

		ENUM_VALUES.put("ZombieType", List.of("husk", "drowned", "zombie_villager"));
		ENUM_VALUES.put("Profession", List.of("none", "nitwit", "armorer", "butcher", "cartographer",
			"cleric", "farmer", "fisherman", "fletcher", "leatherworker", "librarian", "mason",
			"shepherd", "toolsmith", "weaponsmith"));
	}

	private static void scanModdedRegistries() {
		for (var entry : BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet()) {
			Identifier id = entry.getKey().identifier();
			if (!id.getNamespace().equals("minecraft") && !ROOT_FIELDS.containsKey("block/" + id)) {
				register("block/" + id, NbtSuggestions.commonBlockFields().toArray(FieldDef[]::new));
			}
		}
		for (var entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
			Identifier id = entry.getKey().identifier();
			if (!id.getNamespace().equals("minecraft") && !ROOT_FIELDS.containsKey("entity/" + id)) {
				register("entity/" + id, commonEntityFields().toArray(FieldDef[]::new));
			}
		}
	}

	public static @Nullable String getBlockAt(Coordinates coords) {
		if (!(coords instanceof WorldCoordinates)) {
			return null;
		}
		if (coords.isXRelative() || coords.isYRelative() || coords.isZRelative()) {
			return null;
		}

		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			return null;
		}

		BlockPos pos = coords.getBlockPos(new net.minecraft.commands.CommandSourceStack(
			null, Vec3.ZERO, Vec2.ZERO, null, PermissionSet.NO_PERMISSIONS, null, null, null, null));
		Block block = level.getBlockState(pos).getBlock();

		return "block/" + BuiltInRegistries.BLOCK.getKey(block);
	}

	public static @Nullable String getEntityFrom(EntitySelector selector) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			return null;
		}

		try {
			var typeField = EntitySelector.class.getDeclaredField("type");
			typeField.setAccessible(true);
			Object typeTest = typeField.get(selector);
			if (typeTest instanceof EntityType<?> entityType) {
				return "entity/" + EntityType.getKey(entityType);
			}

			var uuidField = EntitySelector.class.getDeclaredField("entityUUID");
			uuidField.setAccessible(true);
			java.util.UUID uuid = (java.util.UUID) uuidField.get(selector);
			if (uuid != null) {
				Entity entity = level.getEntity(uuid);
				if (entity != null) {
					return "entity/" + EntityType.getKey(entity.getType());
				}
			}

			var nameField = EntitySelector.class.getDeclaredField("playerName");
			nameField.setAccessible(true);
			String playerName = (String) nameField.get(selector);
			if (playerName != null) {
				for (Player player : level.players()) {
					if (player.getGameProfile().name().equals(playerName)) {
						return "entity/" + EntityType.getKey(EntityType.PLAYER);
					}
				}
			}
		} catch (Exception ignored) {
		}

		return null;
	}

	public static List<FieldDef> getFieldsForInherited(String key) {
		if (key == null) {
			return List.of();
		}

		List<FieldDef> direct = ROOT_FIELDS.get(key);
		List<FieldDef> result = new ArrayList<>();

		if (direct != null) {
			result.addAll(direct);
		}

		String parent = null;
		if (key.startsWith("entity/")) {
			parent = "common/entity";
		} else if (key.startsWith("block/")) {
			parent = "common/block";
		}

		if (parent != null) {
			List<FieldDef> parentFields = ROOT_FIELDS.get(parent);
			if (parentFields != null) {
				Set<String> names = new HashSet<>();
				for (FieldDef f : result) {
					names.add(f.name());
				}
				for (FieldDef f : parentFields) {
					if (!names.contains(f.name())) {
						result.add(f);
					}
				}
			}
		}

		return result;
	}

	public enum NbtType {
		BYTE("b"), SHORT("s"), INT(""), LONG("l"), FLOAT("f"), DOUBLE("d"),
		STRING(""), BOOLEAN(""), COMPOUND(""), LIST(""),
		BYTE_ARRAY(""), INT_ARRAY(""), LONG_ARRAY(""), UUID(""), ENUM("");

		public final String suffix;

		NbtType(String suffix) {
			this.suffix = suffix;
		}
	}

	public record FieldDef(String name, NbtType type, String subtext, @Nullable List<FieldDef> children) {
		public FieldDef(String name, NbtType type, String subtext) {
			this(name, type, subtext, null);
		}
	}
}