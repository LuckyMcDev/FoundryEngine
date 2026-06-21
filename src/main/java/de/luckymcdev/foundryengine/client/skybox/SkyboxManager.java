package de.luckymcdev.foundryengine.client.skybox;

import com.mojang.math.Transformation;
import de.luckymcdev.foundryengine.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SkyboxManager {
    private static final Vector3f SCALE = new Vector3f(-65000, -65000, -65000);
    private Display.ItemDisplay skyboxEntity = null;
    private ItemStack skyboxItemStack = null;

    public void setSkyboxItem(ItemStack stack) {
        this.skyboxItemStack = stack;
        if (skyboxEntity != null && skyboxEntity.isAlive()) {
            skyboxEntity.setItemStack(stack);
        }
    }

    public void tick(ClientTickEvent.Pre event) {
        if(!ClientConfig.CUSTOM_SKYBOX.getAsBoolean()) return;
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) {
            if (skyboxEntity != null) {
                skyboxEntity.discard();
                skyboxEntity = null;
            }
            return;
        }

        if (skyboxEntity != null && skyboxEntity.level() != mc.level) {
            skyboxEntity.discard();
            skyboxEntity = null;
        }

        if (skyboxItemStack == null) {
            ItemStack stack = new ItemStack(Items.LEATHER_HORSE_ARMOR);
            stack.set(DataComponents.ITEM_MODEL, Identifier.parse("skybox"));
            setSkyboxItem(stack);
        }

        if (skyboxEntity == null || !skyboxEntity.isAlive()) {
            skyboxEntity = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, mc.level);
            skyboxEntity.setItemStack(skyboxItemStack);
            skyboxEntity.setTransformation(new Transformation(
                    new Vector3f(),
                    new Quaternionf(),
                    SCALE,
                    new Quaternionf()
            ));
            mc.level.addEntity(skyboxEntity);
        }

        skyboxEntity.setPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
    }
}