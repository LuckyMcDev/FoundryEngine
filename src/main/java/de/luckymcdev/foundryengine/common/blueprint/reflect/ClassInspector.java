package de.luckymcdev.foundryengine.common.blueprint.reflect;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ClassInspector {

    private static final Map<Class<?>, ClassInfo> CACHE = new ConcurrentHashMap<>();
    private static final Set<Class<?>> COMMON_CLASSES = new LinkedHashSet<>();
    private static final Map<String, Class<?>> TYPE_CLASS_MAP = new HashMap<>();

    static {
        initTypeClassMap();
        populateFromBlueprintTypes();
    }

    private ClassInspector() {
    }

    private static void initTypeClassMap() {
        TYPE_CLASS_MAP.put("Object", Object.class);
        TYPE_CLASS_MAP.put("String", String.class);
        TYPE_CLASS_MAP.put("Bool", Boolean.class);
        TYPE_CLASS_MAP.put("Int", Integer.class);
        TYPE_CLASS_MAP.put("Float", Float.class);
        TYPE_CLASS_MAP.put("MinecraftServer", net.minecraft.server.MinecraftServer.class);
        TYPE_CLASS_MAP.put("Player", net.minecraft.server.level.ServerPlayer.class);
        TYPE_CLASS_MAP.put("LivingEntity", net.minecraft.world.entity.LivingEntity.class);
        TYPE_CLASS_MAP.put("Entity", net.minecraft.world.entity.Entity.class);
        TYPE_CLASS_MAP.put("Level", net.minecraft.world.level.Level.class);
        TYPE_CLASS_MAP.put("Vec3", net.minecraft.world.phys.Vec3.class);
        TYPE_CLASS_MAP.put("Vec2", net.minecraft.world.phys.Vec2.class);
        TYPE_CLASS_MAP.put("CommandSource", net.minecraft.commands.CommandSourceStack.class);
        TYPE_CLASS_MAP.put("BlockState", net.minecraft.world.level.block.state.BlockState.class);
        TYPE_CLASS_MAP.put("BlockEntity", net.minecraft.world.level.block.entity.BlockEntity.class);
        TYPE_CLASS_MAP.put("BlockPos", net.minecraft.core.BlockPos.class);
        TYPE_CLASS_MAP.put("Direction", net.minecraft.core.Direction.class);
        TYPE_CLASS_MAP.put("ItemStack", net.minecraft.world.item.ItemStack.class);
        TYPE_CLASS_MAP.put("ItemEntity", net.minecraft.world.entity.item.ItemEntity.class);
        TYPE_CLASS_MAP.put("EntityType", net.minecraft.world.entity.EntityType.class);
        TYPE_CLASS_MAP.put("Component", net.minecraft.network.chat.Component.class);
        TYPE_CLASS_MAP.put("Advancement", net.minecraft.advancements.Advancement.class);
        TYPE_CLASS_MAP.put("Container", net.minecraft.world.inventory.AbstractContainerMenu.class);
        TYPE_CLASS_MAP.put("Explosion", net.minecraft.world.level.Explosion.class);
        TYPE_CLASS_MAP.put("DamageSource", net.minecraft.world.damagesource.DamageSource.class);
        TYPE_CLASS_MAP.put("Connection", net.minecraft.network.Connection.class);
        TYPE_CLASS_MAP.put("InteractionHand", net.minecraft.world.InteractionHand.class);
        TYPE_CLASS_MAP.put("CommandDispatcher", com.mojang.brigadier.CommandDispatcher.class);
        TYPE_CLASS_MAP.put("CommandContext", com.mojang.brigadier.context.CommandContext.class);
        TYPE_CLASS_MAP.put("Effect", net.minecraft.world.effect.MobEffect.class);
        TYPE_CLASS_MAP.put("Enchantment", net.minecraft.world.item.enchantment.Enchantment.class);
        TYPE_CLASS_MAP.put("Particle", net.minecraft.core.particles.ParticleType.class);
        TYPE_CLASS_MAP.put("SoundEvent", net.minecraft.sounds.SoundEvent.class);
        TYPE_CLASS_MAP.put("Recipe", net.minecraft.world.item.crafting.Recipe.class);
        TYPE_CLASS_MAP.put("Item", net.minecraft.world.item.Item.class);
        TYPE_CLASS_MAP.put("Block", net.minecraft.world.level.block.Block.class);
        TYPE_CLASS_MAP.put("MobEffect", net.minecraft.world.effect.MobEffect.class);
        TYPE_CLASS_MAP.put("ServerLevel", net.minecraft.server.level.ServerLevel.class);
        TYPE_CLASS_MAP.put("LookupProvider", net.minecraft.core.HolderLookup.Provider.class);
    }

    private static void populateFromBlueprintTypes() {
        for (NodePinType<?> type : BlueprintTypes.getRegisteredTypes().values()) {
            Class<?> clazz = TYPE_CLASS_MAP.get(type.displayName);
            if (clazz != null) {
                addCommonClass(clazz);
            }
        }
        addCommonClass(net.minecraft.world.item.Item.Properties.class);
        addCommonClass(net.minecraft.world.level.block.state.BlockBehaviour.Properties.class);
        addCommonClass(net.minecraft.world.entity.player.Player.class);
        addCommonClass(net.minecraft.server.MinecraftServer.class);
        addCommonClass(net.minecraft.commands.CommandSourceStack.class);
        addCommonClass(java.util.Collection.class);
        addCommonClass(java.util.List.class);
        addCommonClass(java.util.Map.class);
        addCommonClass(net.minecraft.network.chat.MutableComponent.class);
    }

    public static Set<Class<?>> getCommonClasses() {
        return Collections.unmodifiableSet(COMMON_CLASSES);
    }

    public static void addCommonClass(Class<?> clazz) {
        COMMON_CLASSES.add(clazz);
    }

    public static ClassInfo inspect(Class<?> clazz) {
        return CACHE.computeIfAbsent(clazz, ClassInspector::buildInfo);
    }

    public static @Nullable ClassInfo get(Class<?> clazz) {
        return CACHE.get(clazz);
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static ClassInfo buildInfo(Class<?> clazz) {
        List<MethodInfo> methods = new ArrayList<>();
        List<FieldInfo> fields = new ArrayList<>();

        for (Method m : clazz.getMethods()) {
            if (m.getDeclaringClass() == Object.class) continue;
            int mod = m.getModifiers();
            if (!Modifier.isPublic(mod)) continue;
            Parameter[] params = m.getParameters();
            String[] paramTypes = new String[params.length];
            String[] paramNames = new String[params.length];
            for (int i = 0; i < params.length; i++) {
                paramTypes[i] = params[i].getType().getName();
                paramNames[i] = params[i].isNamePresent() ? params[i].getName() : ("p" + i);
            }
            methods.add(new MethodInfo(
                    m.getName(),
                    m.getReturnType().getName(),
                    paramTypes,
                    paramNames
            ));
        }

        for (Field f : clazz.getFields()) {
            int mod = f.getModifiers();
            if (!Modifier.isPublic(mod)) continue;
            fields.add(new FieldInfo(
                    f.getName(),
                    f.getType().getName()
            ));
        }

        return new ClassInfo(clazz.getName(), methods.toArray(new MethodInfo[0]), fields.toArray(new FieldInfo[0]));
    }

    public record ClassInfo(String className, MethodInfo[] methods, FieldInfo[] fields) {
    }

    public record MethodInfo(String name, String returnType, String[] paramTypes, String[] paramNames) {
        public String signature() {
            return name + "(" + String.join(", ", paramTypes) + ")";
        }

        public String displayName() {
            StringBuilder sb = new StringBuilder(name).append("(");
            for (int i = 0; i < paramTypes.length; i++) {
                if (i > 0) sb.append(", ");
                String shortName = paramTypes[i].contains(".")
                        ? paramTypes[i].substring(paramTypes[i].lastIndexOf('.') + 1)
                        : paramTypes[i];
                sb.append(shortName);
            }
            sb.append(")");
            return sb.toString();
        }
    }

    public record FieldInfo(String name, String type) {
        public String displayName() {
            String shortType = type.contains(".")
                    ? type.substring(type.lastIndexOf('.') + 1)
                    : type;
            return shortType + " " + name;
        }
    }
}
