package com.craftbound.client.book;

import java.util.HashMap;
import java.util.Map;

import com.craftbound.client.jei.BookIngredient;

import net.minecraft.world.item.Item;

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
        Map<Item, BrowseTab> byItem = new HashMap<>();
        RecipeTabs.forEach(byItem::putIfAbsent);
        return new ItemCategories(byItem);
    }

    public BrowseTab tabOf(BookIngredient ingredient)
    {
        return ingredient.item().map(byItem::get).orElse(BrowseTab.MISC);
    }
}
