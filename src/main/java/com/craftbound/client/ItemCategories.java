package com.craftbound.client;

import java.util.HashMap;
import java.util.Map;

import com.craftbound.client.jei.BookIngredient;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

// Which ribbon each item belongs on, taken from the book category vanilla already stores on every
// crafting and cooking recipe — so any mod shipping ordinary recipes is categorised for free.
// Anything else (Create's mixing, pressing, and the rest) has no such category and falls to Misc,
// which is where vanilla puts an uncategorised recipe too.
public final class ItemCategories
{
    public static final ItemCategories EMPTY = new ItemCategories(Map.of());

    private final Map<Item, BrowseTab> byItem;

    private ItemCategories(Map<Item, BrowseTab> byItem)
    {
        this.byItem = byItem;
    }

    public static ItemCategories fromClientRecipes()
    {
        var level = Minecraft.getInstance().level;
        if (level == null)
            return EMPTY;

        Map<Item, BrowseTab> byItem = new HashMap<>();
        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes())
        {
            BrowseTab tab = tabOf(holder.value());
            if (tab == null)
                continue;
            ItemStack result = resultOf(holder.value(), level.registryAccess());
            if (!result.isEmpty())
                byItem.putIfAbsent(result.getItem(), tab);
        }
        return new ItemCategories(byItem);
    }

    public BrowseTab tabOf(BookIngredient ingredient)
    {
        return ingredient.item().map(byItem::get).orElse(BrowseTab.MISC);
    }

    private static BrowseTab tabOf(Recipe<?> recipe)
    {
        if (recipe instanceof CraftingRecipe crafting)
            return BrowseTab.of(crafting.category());
        if (recipe instanceof AbstractCookingRecipe cooking)
            return BrowseTab.of(cooking.category());
        return null;
    }

    // Some mods' recipes have no meaningful fixed result and throw when asked for one; such a
    // recipe simply doesn't categorise anything rather than taking the whole book down with it.
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
}
