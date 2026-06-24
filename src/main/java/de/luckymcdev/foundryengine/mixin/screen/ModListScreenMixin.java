package de.luckymcdev.foundryengine.mixin.screen;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.bundle.compat.BundleSelectable;
import de.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforge.client.gui.widget.ModListWidget;
import net.neoforged.neoforge.common.util.Size2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements {@link BundleSelectable} on ModListScreen to display bundle info in the mod list UI.
 */
@Mixin(ModListScreen.class)
public abstract class ModListScreenMixin implements BundleSelectable {

    @Shadow(remap = false)
    private ModListWidget.ModEntry selected;
    @Shadow(remap = false)
    private ModListWidget modList;

    /**
     * Sets the selected bundle and updates the info panel with bundle details.
     */
    @Override
    public void engine$setSelectedBundle(Bundle bundle) {
        selected = null;
        modList.setSelected(null);

        try {
            Field modInfoField = ModListScreen.class.getDeclaredField("modInfo");
            modInfoField.setAccessible(true);
            Object modInfo = modInfoField.get(this);

            BundleInfo info = bundle.info();
            List<String> lines = new ArrayList<>();
            lines.add("[Bundle]");
            lines.add(null);
            lines.add(info.displayName());
            lines.add("Version: " + info.versionInfo());
            lines.add("ID: " + info.id());
            if (!info.authors().isEmpty()) {
                lines.add("Authors: " + String.join(", ", info.authors()));
            }
            if (!info.dependencies().isEmpty()) {
                lines.add("Dependencies:");
                for (var dep : info.dependencies()) {
                    lines.add(" - " + dep.type().name().toLowerCase() + ": " + dep.id() + " (" + dep.version() + ")");
                }
            }
            lines.add(null);
            lines.add("Script count: " + bundle.bundleFiles().scriptCount());

            Identifier textId = bundle.id(bundle.info().id() + ".png");
            var textureView = Client.getMc().getTextureManager().getTexture(textId).getTextureView();
            Size2i txSize = new Size2i(textureView.getWidth(0), textureView.getHeight(0));
            ((InfoPanelAccessor) modInfo).invokeSetInfo(lines, textId, txSize);
        } catch (Exception e) {
            throw new EngineException("Failed to access modInfo panel", e);
        }
    }
}