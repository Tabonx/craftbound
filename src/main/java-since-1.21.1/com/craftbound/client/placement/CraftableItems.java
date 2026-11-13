package com.craftbound.client.placement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.craftbound.placement.RecipePlacement;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

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
        collect(minecraft.level.getRecipeManager(), RecipePlacement.recipeTypeFor(menu.getRecipeBookType()),
                contents, menu.getGridWidth(), menu.getGridHeight(),
                minecraft.level.registryAccess(), result);
        return result;
    }

    // For a pool other than the player's inventory, such as the stock a storage screen orders from.
    public static Set<Item> craftableFrom(StackedContents contents)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null)
            return Set.of();

        Set<Item> result = new HashSet<>();
        collect(minecraft.level.getRecipeManager(), RecipeType.CRAFTING, contents, 3, 3,
                minecraft.level.registryAccess(), result);
        return result;
    }

    // The recipe type is chosen at runtime, so it is erased to a raw type for getAllRecipesFor.
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void collect(RecipeManager recipes, RecipeType<?> type, StackedContents contents,
            int width, int height, RegistryAccess registries, Set<Item> result)
    {
        List<RecipeHolder<?>> holders = recipes.getAllRecipesFor((RecipeType) type);
        for (RecipeHolder<?> holder : holders)
        {
            Recipe<?> recipe = holder.value();
            // Special recipes declare no ingredients, so canCraft is true from an empty inventory.
            // Vanilla's book skips them too.
            if (recipe.isSpecial())
                continue;
            if (recipe.canCraftInDimensions(width, height) && contents.canCraft(recipe, null))
                result.add(recipe.getResultItem(registries).getItem());
        }
    }
}
