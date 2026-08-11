package com.craftbound.client;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.RecipeIngredientRole;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
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

        Set<Item> held = heldItems();
        if (held.isEmpty())
            return recipes;

        return recipes.stream()
                .sorted(Comparator.comparingInt(recipe -> hasEveryInput(recipe, held) ? 0 : 1))
                .toList();
    }

    // Counts are ignored on purpose: this ranks recipes, it does not promise the craft will fit.
    // A slot with no item alternatives (a fluid, say) cannot be judged, so it does not count against
    // the recipe.
    private static boolean hasEveryInput(IRecipeLayoutDrawable<?> recipe, Set<Item> held)
    {
        return recipe.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT).stream()
                .allMatch(slot -> satisfied(slot, held));
    }

    private static boolean satisfied(IRecipeSlotView slot, Set<Item> held)
    {
        List<ItemStack> options = slot.getIngredients(VanillaTypes.ITEM_STACK).toList();
        return options.isEmpty() || options.stream().anyMatch(stack -> held.contains(stack.getItem()));
    }

    private static Set<Item> heldItems()
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return Set.of();

        Set<Item> held = new HashSet<>();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++)
        {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty())
                held.add(stack.getItem());
        }
        return held;
    }

    private RecipeOrder() {}
}
