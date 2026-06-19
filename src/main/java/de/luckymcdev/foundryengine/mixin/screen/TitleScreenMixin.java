package de.luckymcdev.foundryengine.mixin.screen;

import de.luckymcdev.foundryengine.common.event.modification.TitleScreenModificationEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    @ModifyArg(
            method = "createNormalMenuOptions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;",
                    ordinal = 0
            ),
            index = 1
    )
    private Button.OnPress wrapSingleplayerCallback(Button.OnPress original) {
        return button -> {
            TitleScreenModificationEvent event = new TitleScreenModificationEvent(TitleScreenModificationEvent.ButtonType.SINGLEPLAYER);
            NeoForge.EVENT_BUS.post(event);
            if (event.isCanceled()) return;
            original.onPress(button);
        };
    }

    @ModifyArg(
            method = "createNormalMenuOptions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;",
                    ordinal = 1
            ),
            index = 1
    )
    private Button.OnPress wrapMultiplayerCallback(Button.OnPress original) {
        return button -> {
            TitleScreenModificationEvent event = new TitleScreenModificationEvent(TitleScreenModificationEvent.ButtonType.MULTIPLAYER);
            NeoForge.EVENT_BUS.post(event);
            if (event.isCanceled()) return;
            original.onPress(button);
        };
    }

    @ModifyArg(
            method = "createNormalMenuOptions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;",
                    ordinal = 2
            ),
            index = 1
    )
    private Button.OnPress wrapRealmsCallback(Button.OnPress original) {
        return button -> {
            TitleScreenModificationEvent event = new TitleScreenModificationEvent(TitleScreenModificationEvent.ButtonType.REALMS);
            NeoForge.EVENT_BUS.post(event);
            if (event.isCanceled()) return;
            original.onPress(button);
        };
    }
}