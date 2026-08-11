package com.craftbound;

import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

// Which recipes a given menu can lay out in its input slots. The client uses this to decide whether
// to offer the place button; the server uses it to reject anything the menu could not handle.
public final class RecipePlacement
{
    public static RecipeType<?> recipeTypeFor(RecipeBookType bookType)
    {
        return switch (bookType)
        {
            case CRAFTING -> RecipeType.CRAFTING;
            case FURNACE -> RecipeType.SMELTING;
            case BLAST_FURNACE -> RecipeType.BLASTING;
            case SMOKER -> RecipeType.SMOKING;
        };
    }

    // Special recipes (firework variants, leather dyeing) declare no ingredients, so there is
    // nothing to place; the recipe type must match or the menu would cast it to the wrong type.
    public static boolean canPlace(RecipeBookMenu<?, ?> menu, RecipeHolder<?> recipe)
    {
        Recipe<?> value = recipe.value();
        return !value.isSpecial()
                && value.getType() == recipeTypeFor(menu.getRecipeBookType())
                && value.canCraftInDimensions(menu.getGridWidth(), menu.getGridHeight());
    }

    private RecipePlacement() {}
}
