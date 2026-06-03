package de.luckymcdev.foundryengine.server.packs;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CompositePackResources implements PackResources {
    private final PackLocationInfo location;
    private final List<PackResources> children;
    private final byte[] packMetaBytes;

    public CompositePackResources(PackLocationInfo location, List<PackResources> children, PackType packType) {
        this.location = location;
        List<PackResources> reversed = new ArrayList<>(children);
        Collections.reverse(reversed);
        this.children = List.copyOf(reversed);
        this.packMetaBytes = buildPackMeta(location.title(), packType);
    }

    private static byte[] buildPackMeta(Component description, PackType packType) {
        PackFormat format = SharedConstants.getCurrentVersion().packVersion(packType);
        JsonObject pack = new JsonObject();
        pack.add("description", JsonOps.INSTANCE.createString(description.toString()));
        pack.addProperty("pack_format", format.major());
        JsonObject root = new JsonObject();
        root.add("pack", pack);
        return root.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... paths) {
        if (paths.length == 1 && "pack.mcmeta".equals(paths[0])) {
            return () -> new ByteArrayInputStream(packMetaBytes);
        }
        for (var child : children) {
            var result = child.getRootResource(paths);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier location) {
        for (var child : children) {
            var result = child.getResource(type, location);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public void listResources(PackType type, String namespace, String directory, ResourceOutput output) {
        Set<Identifier> seen = new HashSet<>();
        for (var child : children) {
            child.listResources(type, namespace, directory, (id, supplier) -> {
                if (seen.add(id)) {
                    output.accept(id, supplier);
                }
            });
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        Set<String> result = new HashSet<>();
        for (var child : children) {
            result.addAll(child.getNamespaces(type));
        }
        return result;
    }

    @Override
    public @Nullable <T> T getMetadataSection(MetadataSectionType<T> serializer) throws IOException {
        for (var child : children) {
            var result = child.getMetadataSection(serializer);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return location;
    }

    @Override
    public void close() {
        for (var child : children) {
            child.close();
        }
    }
}
