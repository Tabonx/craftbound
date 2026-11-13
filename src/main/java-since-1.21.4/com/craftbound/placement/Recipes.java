package com.craftbound.placement;

import java.util.Optional;

import com.craftbound.platform.ServerLevels;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;

// The menu places a recipe without asking the player's recipe book; only vanilla's own placement
// packet checks it.
public final class Recipes
{
    public static ResourceLocation id(RecipeHolder<?> recipe)
    {
        return recipe.id().location();
    }

    public static Optional<RecipeHolder<?>> byId(ServerLevel level, ResourceLocation id)
    {
        return level.getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, id));
    }

    public static boolean fitsMenu(RecipeBookMenu menu, Recipe<?> recipe)
    {
        return !recipe.placementInfo().isImpossibleToPlace()
                && recipe.display().stream().allMatch(display -> fits(menu, display));
    }

    // Read off the display, as vanilla's book does. The menu does not check this when placing: a
    // 3x3 recipe sent to the 2x2 grid is laid out cut off, taking its ingredients along.
    public static boolean fits(RecipeBookMenu menu, RecipeDisplay display)
    {
        if (!(menu instanceof AbstractCraftingMenu crafting))
            return true;

        int width = crafting.getGridWidth();
        int height = crafting.getGridHeight();
        return switch (display)
        {
            case ShapedCraftingRecipeDisplay shaped -> shaped.width() <= width && shaped.height() <= height;
            case ShapelessCraftingRecipeDisplay shapeless -> shapeless.ingredients().size() <= width * height;
            default -> true;
        };
    }

    public static void place(ServerPlayer player, RecipeBookMenu menu, RecipeHolder<?> recipe, boolean placeAll)
    {
        menu.handlePlacement(placeAll, player.isCreative(), recipe, ServerLevels.of(player), player.getInventory());
    }

    public static void placeLent(ServerPlayer player, RecipeBookMenu menu, RecipeHolder<?> recipe, boolean placeAll)
    {
        place(player, menu, recipe, placeAll);
    }

    // Vanilla's placement packet names a display rather than a recipe, and the client is never told
    // which display a recipe has, so there is nothing to ask a server without Craftbound for.
    public static boolean vanillaPlaces(RecipeBook book, RecipeHolder<?> recipe)
    {
        return false;
    }

    public static Optional<Packet<?>> vanillaPlacement(int containerId, RecipeHolder<?> recipe, boolean placeAll)
    {
        return Optional.empty();
    }

    private Recipes() {}
}
