package com.craftbound.client;

import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.RecipeBookMenu;

// The screens vanilla gives a recipe book: the book fills their grid, and filters by what that grid
// can make from the player's inventory.
final class VanillaIntegration implements BookIntegration
{
    @Override
    public Optional<MenuBinding> bind(AbstractContainerMenu menu, RecipePlacer transfer)
    {
        return menu instanceof RecipeBookMenu recipeBook
                ? Optional.of(new MenuBinding(new GridPlacer(recipeBook), () -> CraftableItems.craftableIn(recipeBook),
                        VanillaIntegration::inventoryVersion))
                : Optional.empty();
    }

    private static int inventoryVersion()
    {
        var player = Minecraft.getInstance().player;
        return player == null ? -1 : player.getInventory().getTimesChanged();
    }
}
