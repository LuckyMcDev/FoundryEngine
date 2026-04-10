package de.luckymcdev.foundryengine.mixin.screen;

import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.bundle.compat.BundleSelectable;
import de.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforge.common.util.Size2i;
import org.spongepowered.asm.mixin.Mixin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(ModListScreen.class)
public abstract class ModListScreenMixin implements BundleSelectable {

    @Override
    public void engine$setSelectedBundle(Bundle bundle) {
        try {
            ModListScreen self = (ModListScreen) (Object) this;

            Field selectedField = ModListScreen.class.getDeclaredField("selected");
            selectedField.setAccessible(true);
            selectedField.set(self, null);

            Field modListField = ModListScreen.class.getDeclaredField("modList");
            modListField.setAccessible(true);
            Object modList = modListField.get(self);
            Method setSelected = Arrays.stream(modList.getClass().getMethods())
                    .filter(m -> m.getName().equals("setSelected") && m.getParameterCount() == 1)
                    .findFirst()
                    .orElseThrow();
            setSelected.invoke(modList, (Object) null);

            BundleInfo info = bundle.info();
            List<String> lines = new ArrayList<>();
            lines.add("[Bundle]");
            lines.add(null);
            lines.add(info.displayName());
            lines.add("Version: " + info.versionInfo());
            lines.add("ID: " + info.id());
            if (!info.authors().isEmpty())
                lines.add("Authors: " + String.join(", ", info.authors()));
            lines.add(null);
            lines.add("Script count: " + bundle.bundleFiles().scriptCount());

            Field modInfoField = ModListScreen.class.getDeclaredField("modInfo");
            modInfoField.setAccessible(true);
            Object modInfo = modInfoField.get(self);

            Method setInfo = modInfo.getClass().getDeclaredMethod("setInfo", List.class, Identifier.class, Size2i.class);
            setInfo.setAccessible(true);
            setInfo.invoke(modInfo, lines, null, new Size2i(0, 0));

        } catch (Exception e) {
            throw new EngineException("foundry$setSelectedBundle failed", e);
        }
    }
}