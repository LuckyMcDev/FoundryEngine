package de.luckymcdev.foundryengine.client.editor.builtin.explorer;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * Unified node structure for explorer trees.
 * <p>
 * Three concrete subtypes exist:
 * <ul>
 *   <li>{@link FileExplorerNode}   – local filesystem entries</li>
 *   <li>{@link ResourceExplorerNode} – Minecraft resource-manager entries</li>
 *   <li>{@link RemoteExplorerNode}  – remote server entries fetched over the network</li>
 * </ul>
 */
public abstract class ExplorerNode {
    public final String name;
    public final Map<String, ExplorerNode> children = new TreeMap<>();

    protected ExplorerNode(String name) {
        this.name = name;
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    public void addChild(String key, ExplorerNode child) {
        children.put(key, child);
    }

    public @Nullable ExplorerNode getChild(String key) {
        return children.get(key);
    }

    public Collection<ExplorerNode> getChildren() {
        return children.values();
    }

    /**
     * A node backed by a real {@link File} on the local filesystem.
     */
    public static class FileExplorerNode extends ExplorerNode {
        public final File file;
        public final List<File> files = new ArrayList<>();

        public FileExplorerNode(String name, File file) {
            super(name);
            this.file = file;
        }

        public boolean isDirectory() {
            return file.isDirectory();
        }

        @Override
        public boolean isEmpty() {
            return children.isEmpty() && files.isEmpty();
        }
    }

    /**
     * A node backed by Minecraft's {@link net.minecraft.server.packs.resources.ResourceManager}.
     */
    public static class ResourceExplorerNode extends ExplorerNode {
        public final List<Identifier> resources = new ArrayList<>();

        public ResourceExplorerNode(String name) {
            super(name);
        }

        @Override
        public boolean isEmpty() {
            return children.isEmpty() && resources.isEmpty();
        }
    }

    /**
     * A node representing a file or directory on a remote (multiplayer) server.
     */
    public static class RemoteExplorerNode extends ExplorerNode {
        public final String relativePath;
        public final boolean isDirectory;
        public final List<RemoteExplorerNode> files = new ArrayList<>();

        public RemoteExplorerNode(String name, String relativePath, boolean isDirectory) {
            super(name);
            this.relativePath = relativePath;
            this.isDirectory = isDirectory;
        }

        @Override
        public boolean isEmpty() {
            return children.isEmpty() && files.isEmpty();
        }
    }
}