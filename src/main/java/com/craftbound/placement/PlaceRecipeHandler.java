package com.craftbound.placement;

import com.craftbound.platform.ServerLevels;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class PlaceRecipeHandler
{
    public static void handle(PlaceRecipePayload payload, IPayloadContext context)
    {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator())
            return;

        player.resetLastActionTime();
        if (!(player.containerMenu instanceof RecipeBookMenu menu)
                || menu.containerId != payload.containerId()
                || !menu.stillValid(player))
            return;

        RecipeHolder<?> recipe = Recipes.byId(ServerLevels.of(player), payload.recipeId()).orElse(null);
        if (recipe == null || !RecipePlacement.canPlace(menu, recipe))
            return;

        switch (PlacementAccess.of(limitedCrafting(player), player.getRecipeBook().contains(recipe.id())))
        {
            case REFUSE -> {}
            case PLACE -> Recipes.place(player, menu, recipe, payload.placeAll());
            case PLACE_WITH_TEMPORARY_GRANT -> Recipes.placeLent(player, menu, recipe, payload.placeAll());
        }
    }

    private static boolean limitedCrafting(ServerPlayer player)
    {
        return ServerLevels.of(player).getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING);
    }

    private PlaceRecipeHandler() {}
}
