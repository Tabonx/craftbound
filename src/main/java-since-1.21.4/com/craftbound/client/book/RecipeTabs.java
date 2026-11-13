package com.craftbound.client.book;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

// The result of every recipe the client knows, with the ribbon its book category puts it on. The
// client is no longer given the recipes themselves, only the displays its own recipe book was told
// about, and each of those names the book category the recipe's own category was turned into.
final class RecipeTabs
{
    // Each book category back to the recipe category it was made from.
    private static final Map<RecipeBookCategory, BrowseTab> TABS = Map.ofEntries(
            Map.entry(RecipeBookCategories.CRAFTING_BUILDING_BLOCKS, BrowseTab.of(CraftingBookCategory.BUILDING)),
            Map.entry(RecipeBookCategories.CRAFTING_REDSTONE, BrowseTab.of(CraftingBookCategory.REDSTONE)),
            Map.entry(RecipeBookCategories.CRAFTING_EQUIPMENT, BrowseTab.of(CraftingBookCategory.EQUIPMENT)),
            Map.entry(RecipeBookCategories.CRAFTING_MISC, BrowseTab.of(CraftingBookCategory.MISC)),
            Map.entry(RecipeBookCategories.FURNACE_FOOD, BrowseTab.of(CookingBookCategory.FOOD)),
            Map.entry(RecipeBookCategories.FURNACE_BLOCKS, BrowseTab.of(CookingBookCategory.BLOCKS)),
            Map.entry(RecipeBookCategories.FURNACE_MISC, BrowseTab.of(CookingBookCategory.MISC)),
            Map.entry(RecipeBookCategories.BLAST_FURNACE_BLOCKS, BrowseTab.of(CookingBookCategory.BLOCKS)),
            Map.entry(RecipeBookCategories.BLAST_FURNACE_MISC, BrowseTab.of(CookingBookCategory.MISC)),
            Map.entry(RecipeBookCategories.SMOKER_FOOD, BrowseTab.of(CookingBookCategory.FOOD)),
            Map.entry(RecipeBookCategories.CAMPFIRE, BrowseTab.of(CookingBookCategory.FOOD)));

    static void forEach(BiConsumer<Item, BrowseTab> out)
    {
        var level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        if (level == null || player == null)
            return;

        ContextMap context = SlotDisplayContext.fromLevel(level);
        for (RecipeCollection collection : player.getRecipeBook().getCollections())
            for (RecipeDisplayEntry entry : collection.getRecipes())
            {
                BrowseTab tab = tabOf(entry.category());
                if (tab == null)
                    continue;
                for (ItemStack result : resultsOf(entry, context))
                    if (!result.isEmpty())
                        out.accept(result.getItem(), tab);
            }
    }

    static BrowseTab tabOf(RecipeBookCategory category)
    {
        return TABS.get(category);
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

    private RecipeTabs() {}
}
