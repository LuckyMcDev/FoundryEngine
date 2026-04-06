package de.luckymcdev.foundryengine.common.editor.builtin;

import de.luckymcdev.foundryengine.common.bundle.info.BundleInfo;

import java.util.List;

public class EditorContext {
    private final BundleInfo bundleInfo;

    public EditorContext(String id, String displayName, List<String> authors, BundleInfo.VersionInfo versionInfo) {
        this.bundleInfo = new BundleInfo(id, displayName, authors, versionInfo);
    }

    public BundleInfo getBundleInfo() {
        return bundleInfo;
    }
}
