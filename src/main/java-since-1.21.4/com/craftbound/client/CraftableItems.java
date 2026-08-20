package com.craftbound.client;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

// Which item results the player can make right now in the open menu, from the player inventory plus
// whatever sits in the input slots. Follows the menu's recipe book: a crafting screen checks
// crafting recipes against its grid, a furnace/smoker/blast screen checks its smelting family.
// Includes Create's crafting-table recipes (ordinary CraftingRecipes) and excludes machine recipes,
// which are not made through any of these menus.
//
// Deliberately ignores progression: the grid it filters has already dropped everything still
// locked, so this is purely about having the ingredients on hand.
public final class CraftableItems
{
    private CraftableItems()
    {
    }

    // The client holds recipe displays rather than recipes, and each display answers for itself
    // whether the given inventory can craft it.
    public static Set<Item> craftableIn(RecipeBookMenu menu)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null)
            return Set.of();

        StackedContents contents = new StackedContents();
        minecraft.player.getInventory().fillStackedContents(contents);
        menu.fillCraftSlotsStackedContents(contents);

        Set<Item> result = new HashSet<>();
        ContextMap context = SlotDisplayContext.fromLevel(minecraft.level);
        for (RecipeCollection collection : minecraft.player.getRecipeBook().getCollections())
            for (RecipeDisplayEntry entry : collection.getRecipes())
                if (entry.canCraft(contents))
                    for (ItemStack stack : entry.resultItems(context))
                        if (!stack.isEmpty())
                            result.add(stack.getItem());
        return result;
    }
}
