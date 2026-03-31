package de.luckymcdev.foundryengine.client.editor.builtin.explorer;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * Unified node structure for browser trees, supporting both filesystem and Minecraft resources.
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
     * File-system based node
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
     * Minecraft resource based node
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
     * Remote based node
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