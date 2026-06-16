package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;
import org.jetbrains.annotations.ApiStatus;

public class RecipeEvents {
    public static final EventGroupHolder<RecipesReceivedEvent> RECIPES_UPDATED = new EventGroupHolder<>();

    public static final EventGroupHolder<ModifyRecipeJsonsEvent> MODIFY_RECIPES = new EventGroupHolder<>();

    public static void recipesReceived(EventCallback<RecipesReceivedEvent> cb) {
        RECIPES_UPDATED.register(cb);
    }

    public static void modifyRecipes(EventCallback<ModifyRecipeJsonsEvent> cb) {
        MODIFY_RECIPES.register(cb);
    }

    @ApiStatus.Internal
    public static class Internal {
        public static void postRecipesReceived(RecipesReceivedEvent e) {
            RECIPES_UPDATED.post(e);
        }

        public static void postModifyRecipes(ModifyRecipeJsonsEvent e) {
            MODIFY_RECIPES.post(e);
        }

        public static void register(IEventBus bus) {
            bus.addListener(Internal::postRecipesReceived);
            bus.addListener(Internal::postModifyRecipes);
        }

        public static void clear() {
            RECIPES_UPDATED.clear();
            MODIFY_RECIPES.clear();
        }
    }
}