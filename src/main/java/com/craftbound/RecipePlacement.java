package com.craftbound;

import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
//? if >=1.21.4 {
/*import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
*///?}
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
    public static boolean canPlace(RecipeBookMenu menu, RecipeHolder<?> recipe)
    {
        Recipe<?> value = recipe.value();
        //? if >=1.21.4 {
        /*return !value.isSpecial()
                && value.getType() == recipeTypeFor(menu.getRecipeBookType())
                && !value.placementInfo().isImpossibleToPlace()
                && value.display().stream().allMatch(display -> fits(menu, display));
        *///?} else {
        return !value.isSpecial()
                && value.getType() == recipeTypeFor(menu.getRecipeBookType())
                && value.canCraftInDimensions(menu.getGridWidth(), menu.getGridHeight());
        //?}
    }

    //? if >=1.21.4 {
    /*// A recipe no longer answers whether it fits a grid, so its shape is read off the display it
    // hands the recipe book, as vanilla's own book does. The menu does not check this when placing:
    // a 3x3 recipe sent to the 2x2 grid is laid out cut off, taking its ingredients along.
    public static boolean fits(RecipeBookMenu menu, RecipeDisplay display)
    {
        if (!(menu instanceof AbstractCraftingMenu crafting))
            return true;

        int width = crafting.getGridWidth();
        int height = crafting.getGridHeight();
        return switch (display)
        {
            case ShapedCraftingRecipeDisplay shaped -> shaped.width() <= width && shaped.height() <= height;
            case ShapelessCraftingRecipeDisplay shapeless -> shapeless.ingredients().size() <= width * height;
            default -> true;
        };
    }
    *///?}

    private RecipePlacement() {}
}
