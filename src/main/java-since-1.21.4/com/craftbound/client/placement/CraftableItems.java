package com.craftbound.client.placement;

import java.util.HashSet;
import java.util.Set;

import com.craftbound.placement.RecipePlacement;
import com.craftbound.placement.Recipes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

// Ignores progression: the grid it filters has already dropped everything still locked.
public final class CraftableItems
{
    private CraftableItems()
    {
    }

    public static Set<Item> craftableIn(RecipeBookMenu menu)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null)
            return Set.of();

        StackedContents contents = RecipePlacement.available(minecraft.player, menu);

        Set<Item> result = new HashSet<>();
        ContextMap context = SlotDisplayContext.fromLevel(minecraft.level);
        SearchRecipeBookCategory category = searchCategoryFor(menu.getRecipeBookType());
        for (RecipeCollection collection : minecraft.player.getRecipeBook().getCollection(category))
            for (RecipeDisplayEntry entry : collection.getRecipes())
                if (Recipes.fits(menu, entry.display()) && entry.canCraft(contents))
                    for (ItemStack stack : entry.resultItems(context))
                        if (!stack.isEmpty())
                            result.add(stack.getItem());
        return result;
    }

    private static SearchRecipeBookCategory searchCategoryFor(RecipeBookType bookType)
    {
        return switch (bookType)
        {
            case CRAFTING -> SearchRecipeBookCategory.CRAFTING;
            case FURNACE -> SearchRecipeBookCategory.FURNACE;
            case BLAST_FURNACE -> SearchRecipeBookCategory.BLAST_FURNACE;
            case SMOKER -> SearchRecipeBookCategory.SMOKER;
        };
    }
}
