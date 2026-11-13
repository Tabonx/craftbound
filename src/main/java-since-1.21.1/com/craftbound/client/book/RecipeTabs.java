package com.craftbound.client.book;

import java.util.function.BiConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

final class RecipeTabs
{
    static void forEach(BiConsumer<Item, BrowseTab> out)
    {
        var level = Minecraft.getInstance().level;
        if (level == null)
            return;

        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes())
        {
            BrowseTab tab = tabOf(holder.value());
            if (tab == null)
                continue;
            ItemStack result = resultOf(holder.value(), level.registryAccess());
            if (!result.isEmpty())
                out.accept(result.getItem(), tab);
        }
    }

    static BrowseTab tabOf(Recipe<?> recipe)
    {
        if (recipe instanceof CraftingRecipe crafting)
            return BrowseTab.of(crafting.category());
        if (recipe instanceof AbstractCookingRecipe cooking)
            return BrowseTab.of(cooking.category());
        return null;
    }

    // Some mods' recipes throw when asked for a fixed result.
    private static ItemStack resultOf(Recipe<?> recipe, HolderLookup.Provider registries)
    {
        try
        {
            return recipe.getResultItem(registries);
        }
        catch (RuntimeException e)
        {
            return ItemStack.EMPTY;
        }
    }

    private RecipeTabs() {}
}
