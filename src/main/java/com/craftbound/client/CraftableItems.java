package com.craftbound.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.craftbound.RecipePlacement;

import net.minecraft.client.Minecraft;
//? if >=1.21.5 {
/*import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
*///?}
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

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

    public static Set<Item> craftableIn(RecipeBookMenu menu)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null)
            return Set.of();

        StackedContents contents = new StackedContents();
        minecraft.player.getInventory().fillStackedContents(contents);
        menu.fillCraftSlotsStackedContents(contents);

        Set<Item> result = new HashSet<>();
        //? if >=1.21.5 {
        /*// The client holds recipe displays rather than recipes now, and each display answers for
        // itself whether the given inventory can craft it.
        ContextMap context = SlotDisplayContext.fromLevel(minecraft.level);
        for (RecipeCollection collection : minecraft.player.getRecipeBook().getCollections())
            for (RecipeDisplayEntry entry : collection.getRecipes())
                if (entry.canCraft(contents))
                    for (ItemStack stack : entry.resultItems(context))
                        if (!stack.isEmpty())
                            result.add(stack.getItem());
        *///?} else {
        collect(minecraft.level.getRecipeManager(), RecipePlacement.recipeTypeFor(menu.getRecipeBookType()),
                contents, menu.getGridWidth(), menu.getGridHeight(),
                minecraft.level.registryAccess(), result);
        //?}
        return result;
    }

    // Newer versions have no client recipe manager to walk; craftableIn reads displays instead.
    //? if <1.21.5 {
    // getAllRecipesFor pins the recipe type to its input type; since the type is chosen at runtime
    // from the menu, erase to a raw RecipeType and read each holder as a plain Recipe.
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void collect(RecipeManager recipes, RecipeType<?> type, StackedContents contents,
            int width, int height, RegistryAccess registries, Set<Item> result)
    {
        List<RecipeHolder<?>> holders = recipes.getAllRecipesFor((RecipeType) type);
        for (RecipeHolder<?> holder : holders)
        {
            Recipe<?> recipe = holder.value();
            // Special recipes (the variable firework, leather dyeing, etc.) declare no ingredients,
            // so canCraft is trivially true even from an empty inventory. Vanilla's book skips them
            // the same way; where a normal recipe for the same result exists (e.g. the shapeless
            // gunpowder + paper firework), that one is kept and checked properly.
            if (recipe.isSpecial())
                continue;
            if (recipe.canCraftInDimensions(width, height) && contents.canCraft(recipe, null))
                result.add(recipe.getResultItem(registries).getItem());
        }
    }
    //?}
}
