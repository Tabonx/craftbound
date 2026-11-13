package com.craftbound.client.book;

import java.util.List;
import java.util.function.Supplier;

import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.BookRecipes;
import com.craftbound.client.jei.RecipeGroup;
import com.craftbound.client.progression.Progression;
import com.craftbound.progression.UnlockKey;

import mezz.jei.api.gui.IRecipeLayoutDrawable;

// What the book shows on one screen: the ribbon, page and grid while browsing, the recipe it has
// open, and the highlights still swelling.
public final class BookState
{
    // A ribbon's recipes for the focused item come ranked, the ones the player can make first.
    // A whole category is left unsorted: ranking it would build every drawable in it up front.
    final RecipeNavigator<RecipeGroup, IRecipeLayoutDrawable<?>> recipes = new RecipeNavigator<>(
            group -> RecipeOrder.craftableFirst(group.recipes()).stream()
                    .<Supplier<IRecipeLayoutDrawable<?>>>map(recipe -> () -> recipe)
                    .toList(),
            group -> BookRecipes.allIn(group, Progression.gate()));
    final GridPager pager = new GridPager();
    final UnlockHighlights<UnlockKey> highlights = new UnlockHighlights<>();
    BrowseTab tab = BrowseTab.ALL;
    List<List<BookIngredient>> entries = List.of();
    int catalogRevision = -1;
    int unlocksVersion = -1;
    // The JEI run the open recipe was built in.
    int recipesGeneration = -1;
    int ticks = 0;
}
