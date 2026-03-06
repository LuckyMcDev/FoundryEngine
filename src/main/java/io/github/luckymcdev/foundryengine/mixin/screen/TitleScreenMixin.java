package io.github.luckymcdev.foundryengine.mixin.screen;

import io.github.luckymcdev.foundryengine.common.Common;
import io.github.luckymcdev.foundryengine.common.game.behavior.GameBehaviorCancelation;
import io.github.luckymcdev.foundryengine.common.game.behavior.MenuBehavior;
import io.github.luckymcdev.foundryengine.interfaces.FeTitleScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin implements FeTitleScreen {

    @ModifyArg(
            method = "createNormalMenuOptions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;",
                    ordinal = 0
            ),
            index = 1
    )
    private Button.OnPress modifySingleplayerCallback(Button.OnPress original) {
        return button -> {
            if (this.fe$onSingleplayerClick() == GameBehaviorCancelation.CANCEL) {
                return;
            }
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
    private Button.OnPress modifyMultiplayerCallback(Button.OnPress original) {
        return button -> {
            if (this.fe$onMultiplayerClick() == GameBehaviorCancelation.CANCEL) {
                return;
            }
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
    private Button.OnPress modifyRealmsCallback(Button.OnPress original) {
        return button -> {
            if (this.fe$onRealmsClick() == GameBehaviorCancelation.CANCEL) {
                return;
            }
            original.onPress(button);
        };
    }

    @Override
    public GameBehaviorCancelation fe$onSingleplayerClick() {
        TitleScreen screen = (TitleScreen) (Object) this;
        List<MenuBehavior> behaviors = Common.getGameBehaviorManager().getBehaviors(MenuBehavior.class);

        for (MenuBehavior behavior : behaviors) {
            GameBehaviorCancelation result = behavior.onSingleplayerButtonClick(screen);
            if (result == GameBehaviorCancelation.CANCEL) {
                return GameBehaviorCancelation.CANCEL;
            }
        }

        return GameBehaviorCancelation.CONTINUE;
    }

    @Override
    public GameBehaviorCancelation fe$onMultiplayerClick() {
        TitleScreen screen = (TitleScreen) (Object) this;
        List<MenuBehavior> behaviors = Common.getGameBehaviorManager().getBehaviors(MenuBehavior.class);

        for (MenuBehavior behavior : behaviors) {
            GameBehaviorCancelation result = behavior.onMultiplayerButtonClick(screen);
            if (result == GameBehaviorCancelation.CANCEL) {
                return GameBehaviorCancelation.CANCEL;
            }
        }

        return GameBehaviorCancelation.CONTINUE;
    }

    @Override
    public GameBehaviorCancelation fe$onRealmsClick() {
        //TODO
        return GameBehaviorCancelation.CONTINUE;
    }
}