package com.craftbound.placement;

import java.util.Optional;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

// Recipes as this generation keeps them: on both sides, named by a plain id, and placed by the menu
// only when the player's recipe book holds them.
public final class Recipes
{
    public static ResourceLocation id(RecipeHolder<?> recipe)
    {
        return recipe.id();
    }

    public static Optional<RecipeHolder<?>> byId(ServerLevel level, ResourceLocation id)
    {
        return level.getRecipeManager().byKey(id);
    }

    public static boolean fitsMenu(RecipeBookMenu menu, Recipe<?> recipe)
    {
        return recipe.canCraftInDimensions(menu.getGridWidth(), menu.getGridHeight());
    }

    public static void place(ServerPlayer player, RecipeBookMenu menu, RecipeHolder<?> recipe, boolean placeAll)
    {
        menu.handlePlacement(placeAll, recipe, player);
    }

    // The add and remove are the recipe book's silent ones: the client hears of neither, so no toast
    // shows and the client's vanilla book never lists the lent recipe.
    public static void placeLent(ServerPlayer player, RecipeBookMenu menu, RecipeHolder<?> recipe, boolean placeAll)
    {
        player.getRecipeBook().add(recipe);
        try
        {
            place(player, menu, recipe, placeAll);
        }
        finally
        {
            player.getRecipeBook().remove(recipe);
        }
    }

    // A server without Craftbound places only what the player's vanilla recipe book holds.
    public static boolean vanillaPlaces(RecipeBook book, RecipeHolder<?> recipe)
    {
        return book.contains(recipe);
    }

    public static Optional<Packet<?>> vanillaPlacement(int containerId, RecipeHolder<?> recipe, boolean placeAll)
    {
        return Optional.of(new ServerboundPlaceRecipePacket(containerId, recipe, placeAll));
    }

    private Recipes() {}
}
