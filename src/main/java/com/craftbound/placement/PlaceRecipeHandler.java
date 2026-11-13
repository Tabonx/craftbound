package com.craftbound.placement;

import com.craftbound.ServerLevels;

//? if >=1.21.4 {
/*import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
*///?}
import net.minecraft.resources.ResourceLocation;
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

        RecipeHolder<?> recipe = find(player, payload.recipeId());
        if (recipe == null || !RecipePlacement.canPlace(menu, recipe))
            return;

        switch (PlacementAccess.of(limitedCrafting(player), player.getRecipeBook().contains(recipe.id())))
        {
            case REFUSE -> {}
            case PLACE -> place(player, menu, recipe, payload.placeAll());
            case PLACE_WITH_TEMPORARY_GRANT -> placeWithTemporaryGrant(player, menu, recipe, payload.placeAll());
        }
    }

    private static RecipeHolder<?> find(ServerPlayer player, ResourceLocation id)
    {
        //? if >=1.21.4 {
        /*ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id);
        return ServerLevels.of(player).getServer().getRecipeManager().byKey(key).orElse(null);
        *///?} else {
        return ServerLevels.of(player).getRecipeManager().byKey(id).orElse(null);
        //?}
    }

    private static boolean limitedCrafting(ServerPlayer player)
    {
        //? if >=1.21.11 {
        /*return ServerLevels.of(player).getGameRules().get(GameRules.LIMITED_CRAFTING);
        *///?} else {
        return ServerLevels.of(player).getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING);
        //?}
    }

    // The add and remove below are the recipe book's silent ones: the client hears of neither, so
    // no toast shows and the client's vanilla book never lists the lent recipe.
    private static void placeWithTemporaryGrant(ServerPlayer player, RecipeBookMenu menu, RecipeHolder<?> recipe,
            boolean placeAll)
    {
        //? if >=1.21.4 {
        /*player.getRecipeBook().add(recipe.id());
        try
        {
            place(player, menu, recipe, placeAll);
        }
        finally
        {
            player.getRecipeBook().remove(recipe.id());
        }
        *///?} else {
        player.getRecipeBook().add(recipe);
        try
        {
            place(player, menu, recipe, placeAll);
        }
        finally
        {
            player.getRecipeBook().remove(recipe);
        }
        //?}
    }

    private static void place(ServerPlayer player, RecipeBookMenu menu, RecipeHolder<?> recipe, boolean placeAll)
    {
        //? if >=1.21.4 {
        /*menu.handlePlacement(placeAll, player.isCreative(), recipe, ServerLevels.of(player), player.getInventory());
        *///?} else {
        menu.handlePlacement(placeAll, recipe, player);
        //?}
    }

    private PlaceRecipeHandler() {}
}
