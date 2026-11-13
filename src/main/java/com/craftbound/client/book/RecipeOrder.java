package com.craftbound.client.book;

import java.util.List;
import java.util.Set;

import com.craftbound.obtained.PlayerItems;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// Recipes the player can make right now come first. JEI hands recipes over in the order Minecraft
// happened to hash them, which means nothing to a player, so the book ranks them once when the
// recipe is opened. Sorting is stable, and it is deliberately not redone while the recipe is on
// screen: recipes reordering themselves under the cursor as the inventory changes reads as a bug.
public final class RecipeOrder
{
    public static List<IRecipeLayoutDrawable<?>> craftableFirst(List<IRecipeLayoutDrawable<?>> recipes)
    {
        if (recipes.size() < 2)
            return recipes;
        return CraftableFirst.sort(recipes, RecipeOrder::inputSlots, heldItems());
    }

    private static List<List<Item>> inputSlots(IRecipeLayoutDrawable<?> recipe)
    {
        return recipe.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT).stream()
                .map(slot -> slot.getIngredients(VanillaTypes.ITEM_STACK).map(ItemStack::getItem).toList())
                .toList();
    }

    // The same stacks the place button counts, so a recipe ranked first is one it would place.
    private static Set<Item> heldItems()
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return Set.of();
        return PlayerItems.counts(PlayerItems.craftingPool(player, player.containerMenu)).keySet();
    }

    private RecipeOrder() {}
}
