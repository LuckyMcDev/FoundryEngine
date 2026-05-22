package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.event.BlueprintContexts;
import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;
import org.jetbrains.annotations.ApiStatus;

public class RecipeEvents {
    public static final EventGroupHolder<RecipesReceivedEvent> RECIPES_UPDATED =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_RECIPE_VIEWER_UPDATED, BlueprintContexts::recipesReceived);

    public static final EventGroupHolder<ModifyRecipeJsonsEvent> MODIFY_RECIPES =
            new EventGroupHolder<>();

    public static void recipesReceived(EventCallback<RecipesReceivedEvent> cb) {
        RECIPES_UPDATED.register(cb);
    }

    public static void modifyRecipes(EventCallback<ModifyRecipeJsonsEvent> cb) {
        MODIFY_RECIPES.register(cb);
    }

    @ApiStatus.Internal
    public static class Internal {

        public static void register(IEventBus bus) {
            bus.addListener(RECIPES_UPDATED::post);
            bus.addListener(MODIFY_RECIPES::post);
        }

        public static void clear() {
            RECIPES_UPDATED.clear();
            MODIFY_RECIPES.clear();
        }
    }
}