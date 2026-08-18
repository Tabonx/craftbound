package com.craftbound.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.craftbound.client.jei.BookIngredient;

import net.minecraft.client.Minecraft;
//? if >=1.21.5 {
/*import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.context.ContextMap;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
*///?}
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

// Which ribbon each item belongs on, taken from the book category vanilla already stores on every
// crafting and cooking recipe, so any mod shipping ordinary recipes is categorised for free.
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
        //? if >=1.21.5 {
        /*// The client is no longer given the recipes themselves, only the displays its own recipe
        // book was told about, and each of those already names the book category it belongs to.
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return EMPTY;

        ContextMap context = SlotDisplayContext.fromLevel(level);
        for (RecipeCollection collection : player.getRecipeBook().getCollections())
            for (RecipeDisplayEntry entry : collection.getRecipes())
            {
                BrowseTab tab = tabOf(entry.category());
                if (tab == null)
                    continue;
                for (ItemStack result : resultsOf(entry, context))
                    if (!result.isEmpty())
                        byItem.putIfAbsent(result.getItem(), tab);
            }
        *///?} else {
        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes())
        {
            BrowseTab tab = tabOf(holder.value());
            if (tab == null)
                continue;
            ItemStack result = resultOf(holder.value(), level.registryAccess());
            if (!result.isEmpty())
                byItem.putIfAbsent(result.getItem(), tab);
        }
        //?}
        return new ItemCategories(byItem);
    }

    public BrowseTab tabOf(BookIngredient ingredient)
    {
        return ingredient.item().map(byItem::get).orElse(BrowseTab.MISC);
    }

    //? if >=1.21.5 {
    /*private static BrowseTab tabOf(RecipeBookCategory category)
    {
        if (category == RecipeBookCategories.CRAFTING_BUILDING_BLOCKS)
            return BrowseTab.BUILDING;
        if (category == RecipeBookCategories.CRAFTING_EQUIPMENT)
            return BrowseTab.EQUIPMENT;
        if (category == RecipeBookCategories.CRAFTING_REDSTONE)
            return BrowseTab.REDSTONE;
        if (category == RecipeBookCategories.CRAFTING_MISC)
            return BrowseTab.MISC;
        // The cooking categories all read as food and oddments rather than as a rail of their own.
        if (category == RecipeBookCategories.FURNACE_BLOCKS)
            return BrowseTab.BUILDING;
        if (category == RecipeBookCategories.FURNACE_FOOD || category == RecipeBookCategories.FURNACE_MISC
                || category == RecipeBookCategories.SMOKER_FOOD || category == RecipeBookCategories.CAMPFIRE)
            return BrowseTab.MISC;
        return null;
    }

    // A display can name several results, and asking for them can throw on a mod's own display.
    private static List<ItemStack> resultsOf(RecipeDisplayEntry entry, ContextMap context)
    {
        try
        {
            return entry.resultItems(context);
        }
        catch (RuntimeException e)
        {
            return List.of();
        }
    }
    *///?} else {
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
    //?}
}
