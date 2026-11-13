package com.craftbound.client.jei;

import java.util.Set;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.resources.ResourceLocation;

// JEI's "Tag Info" categories list tag memberships as pseudo-recipes taking a tag in and handing
// the same tag out. Progression must not read them: one dirt block would reveal everything sharing a
// tag with it. Both the book and the index ask here, so an entry never appears that cannot be opened.
public final class BookCategories
{
    private static final String TAG_RECIPE_PATH_PREFIX = "tag_recipes/";

    static boolean isBrowsable(IRecipeCategory<?> category)
    {
        return isBrowsable(category.getRecipeType().getUid());
    }

    // JEI names a tag category after the registry the tag belongs to, minecraft:tag_recipes/item and
    // the like, so only the path is JEI's.
    public static boolean isBrowsable(ResourceLocation recipeTypeUid)
    {
        return !recipeTypeUid.getPath().startsWith(TAG_RECIPE_PATH_PREFIX);
    }

    // A recipe group means "variants of one recipe" only where vanilla's book reads it. Elsewhere a
    // mod may fill it with anything: Create gives every machine recipe the group "processing".
    public static boolean readsGroups(String categoryUid)
    {
        return VanillaBook.UIDS.contains(categoryUid);
    }

    private static final class VanillaBook
    {
        private static final Set<String> UIDS = Set.of(
                RecipeTypes.CRAFTING.getUid().toString(),
                RecipeTypes.SMELTING.getUid().toString(),
                RecipeTypes.BLASTING.getUid().toString(),
                RecipeTypes.SMOKING.getUid().toString());
    }

    private BookCategories() {}
}
