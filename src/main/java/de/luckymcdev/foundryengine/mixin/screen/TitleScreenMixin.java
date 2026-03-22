package de.luckymcdev.foundryengine.mixin.screen;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.game.behavior.GameBehaviorCancellation;
import de.luckymcdev.foundryengine.common.game.behavior.MenuBehavior;
import de.luckymcdev.foundryengine.interfaces.EngineTitleScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

/**
 * Mixin to make the{@link MenuBehavior} work
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin implements EngineTitleScreen {

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
            if (this.engine$onSingleplayerClick() == GameBehaviorCancellation.CANCEL) {
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
            if (this.engine$onMultiplayerClick() == GameBehaviorCancellation.CANCEL) {
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
            if (this.engine$onRealmsClick() == GameBehaviorCancellation.CANCEL) {
                return;
            }
            original.onPress(button);
        };
    }

    @Override
    public GameBehaviorCancellation engine$onSingleplayerClick() {
        TitleScreen screen = (TitleScreen) (Object) this;
        List<MenuBehavior> behaviors = Common.getGameBehaviorManager().getBehaviors(MenuBehavior.class);

        for (MenuBehavior behavior : behaviors) {
            GameBehaviorCancellation result = behavior.onSingleplayerButtonClick(screen);
            if (result == GameBehaviorCancellation.CANCEL) {
                return GameBehaviorCancellation.CANCEL;
            }
        }

        return GameBehaviorCancellation.CONTINUE;
    }

    @Override
    public GameBehaviorCancellation engine$onMultiplayerClick() {
        TitleScreen screen = (TitleScreen) (Object) this;
        List<MenuBehavior> behaviors = Common.getGameBehaviorManager().getBehaviors(MenuBehavior.class);

        for (MenuBehavior behavior : behaviors) {
            GameBehaviorCancellation result = behavior.onMultiplayerButtonClick(screen);
            if (result == GameBehaviorCancellation.CANCEL) {
                return GameBehaviorCancellation.CANCEL;
            }
        }

        return GameBehaviorCancellation.CONTINUE;
    }

    @Override
    public GameBehaviorCancellation engine$onRealmsClick() {
        TitleScreen screen = (TitleScreen) (Object) this;
        List<MenuBehavior> behaviors = Common.getGameBehaviorManager().getBehaviors(MenuBehavior.class);

        for (MenuBehavior behavior : behaviors) {
            GameBehaviorCancellation result = behavior.onRealmsButtonClick(screen);
            if (result == GameBehaviorCancellation.CANCEL) {
                return GameBehaviorCancellation.CANCEL;
            }
        }

        return GameBehaviorCancellation.CONTINUE;
    }
}