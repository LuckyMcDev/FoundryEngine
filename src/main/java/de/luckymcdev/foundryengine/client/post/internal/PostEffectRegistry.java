package de.luckymcdev.foundryengine.client.post.internal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import de.luckymcdev.foundryengine.client.post.RenderPhase;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PostEffectRegistry {

    private final List<PostEffectEntry> entries = new CopyOnWriteArrayList<>();

    public PostEffectRegistry() {}

    public PostEffectEntry register(Identifier id) {
        PostEffectEntry entry = new PostEffectEntry(id);
        entries.add(entry);
        entries.sort(Comparator.comparingInt(PostEffectEntry::getPriority).reversed());
        return entry;
    }

    public void unregister(PostEffectEntry entry) {
        entry.close();
        entries.remove(entry);
    }

    public void applyAll(RenderPhase phase, float deltaTick, GraphicsResourceAllocator allocator) {
        for (PostEffectEntry entry : entries) {
            entry.apply(phase, deltaTick, allocator);
        }
    }

    public boolean hasEnabledEffectInPhase(RenderPhase phase) {
        for (PostEffectEntry entry : entries) {
            if (entry.getPhase() == phase && entry.isEnabled()) return true;
        }
        return false;
    }

    public void captureWorldDepthSnapshot(RenderTarget framebuffer) {
        WorldDepthSnapshot.capture(framebuffer);
    }

    public void capturePostRenderDepthSnapshot(RenderTarget framebuffer) {
        PostRenderDepthSnapshot.capture(framebuffer);
    }

    public void restorePostRenderDepthSnapshotInto(RenderTarget framebuffer) {
        if (!PostRenderDepthSnapshot.restoreInto(framebuffer)) {
            WorldDepthSnapshot.restoreInto(framebuffer);
        }
    }

    public void invalidatePipelineCaches() {
        for (PostEffectEntry entry : entries) {
            entry.invalidatePipelineCache();
        }
    }

    public List<PostEffectEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public Optional<PostEffectEntry> getEntry(String name) {
        for (PostEffectEntry entry : entries) {
            if (entry.getId().getPath().equals(name)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public void closeAll() {
        for (PostEffectEntry entry : entries) {
            entry.close();
        }
    }
}
