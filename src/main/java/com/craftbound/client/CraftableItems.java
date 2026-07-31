package com.craftbound.client;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

// Which item results the player can craft right now in a menu's crafting grid, from the player
// inventory plus whatever sits in the craft slots. Includes Create's crafting-table recipes (they
// are ordinary CraftingRecipes) and excludes machine recipes, which cannot be made in a grid.
//
// Deliberately ignores recipe-unlock status: the browse grid already shows every producible item
// regardless of unlock, so the filter is purely about having the ingredients on hand.
public final class CraftableItems
{
    private CraftableItems()
    {
    }

    public static Set<Item> craftableIn(RecipeBookMenu<?, ?> menu)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null)
            return Set.of();

        StackedContents contents = new StackedContents();
        minecraft.player.getInventory().fillStackedContents(contents);
        menu.fillCraftSlotsStackedContents(contents);

        RegistryAccess registries = minecraft.level.registryAccess();
        RecipeManager recipes = minecraft.level.getRecipeManager();
        int width = menu.getGridWidth();
        int height = menu.getGridHeight();

        Set<Item> result = new HashSet<>();
        recipes.getAllRecipesFor(RecipeType.CRAFTING).forEach(holder ->
        {
            var recipe = holder.value();
            // Special recipes (the variable firework, leather dyeing, etc.) declare no ingredients,
            // so canCraft is trivially true even from an empty inventory. Vanilla's book skips them
            // the same way; where a normal recipe for the same result exists (e.g. the shapeless
            // gunpowder + paper firework), that one is kept and checked properly below.
            if (recipe.isSpecial())
                return;
            if (recipe.canCraftInDimensions(width, height) && contents.canCraft(recipe, null))
                result.add(recipe.getResultItem(registries).getItem());
        });
        return result;
    }
}
