package com.craftbound.client.jei;

import mezz.jei.api.recipe.category.IRecipeCategory;

// Which of JEI's categories the book is about.
//
// JEI carries categories that are not recipes at all. Its "Tag Info" entries list an item's tag
// memberships as a pseudo-recipe taking a whole tag in and handing the same tag back out. The book
// shows how things are made, so it never displays them, and progression must not read them either:
// one such entry treats every member of a tag as made from any other member, so a single dirt block
// would reveal everything sharing a tag with it.
//
// Both the book and the index ask here, because a category counted by one and not the other is what
// lets an entry appear that cannot be opened.
public final class BookCategories
{
    private static final String TAG_RECIPE_PATH_PREFIX = "tag_recipes/";

    static boolean isBrowsable(IRecipeCategory<?> category)
    {
        return isBrowsable(category.getRecipeType().getUid().getPath());
    }

    public static boolean isBrowsable(String recipeTypePath)
    {
        return !recipeTypePath.startsWith(TAG_RECIPE_PATH_PREFIX);
    }

    private BookCategories() {}
}
