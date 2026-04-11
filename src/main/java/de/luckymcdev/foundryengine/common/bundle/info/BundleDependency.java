package de.luckymcdev.foundryengine.common.bundle.info;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BundleDependency(String id, String version, Type type) {
    public static final Codec<BundleDependency> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(BundleDependency::id),
            Codec.STRING.fieldOf("version").forGetter(BundleDependency::version),
            Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), Enum::name).fieldOf("type").forGetter(BundleDependency::type)
    ).apply(instance, BundleDependency::new));

    public static BundleDependency parse(String dep) {
        String[] typeParts = dep.split(":", 2);
        Type type = Type.valueOf(typeParts[0].toUpperCase());

        String[] idParts = typeParts[1].split("@", 2);
        String id = idParts[0];
        String version = idParts.length > 1 ? idParts[1] : "any";

        return new BundleDependency(id, version, type);
    }

    @Override
    public String toString() {
        return type.name().toLowerCase() + ":" + id + "@" + version;
    }

    public enum Type {
        MOD, BUNDLE
    }
}