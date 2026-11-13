package com.craftbound.client.integration;

import java.util.Optional;

import com.craftbound.client.placement.CraftableItems;
import com.craftbound.client.placement.GridPlacer;
import com.craftbound.client.placement.RecipePlacer;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.RecipeBookMenu;

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
