package com.craftbound.client.book;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.craftbound.client.book.search.SearchAliases;
import com.craftbound.client.bookmarks.BookmarkStore;
import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.BookIngredients;
import com.craftbound.client.jei.JeiSearch;
import com.craftbound.client.jei.JeiSession;
import com.craftbound.client.jei.RecipeIndexBuilder;
import com.craftbound.client.jei.RecipeIndexSnapshot;
import com.craftbound.client.progression.Progression;

// Everything the book can list, loaded once per JEI run, since every ingredient came from that run:
// what a search can name, what some recipe makes, which ribbon each sits on and which are bookmarked.
public final class BookCatalog implements BrowseQuery.Catalog<BookIngredient>
{
    private int generation = -1;
    private int revision = 0;
    private List<BookIngredient> searchable = List.of();
    private List<BookIngredient> producible = List.of();
    private List<BookIngredient> bookmarked = List.of();
    private ItemCategories categories = ItemCategories.EMPTY;

    // Moves whenever what the catalog holds does.
    public int refresh()
    {
        if (generation == JeiSession.generation())
            return revision;
        Optional<RecipeIndexSnapshot> recipes = RecipeIndexBuilder.current();
        if (recipes.isEmpty())
            return revision;
        searchable = BookIngredients.all();
        producible = recipes.get().producible(searchable);
        categories = ItemCategories.fromClientRecipes();
        bookmarked = BookmarkStore.resolve(producible);
        generation = JeiSession.generation();
        return ++revision;
    }

    public void toggleBookmark(BookIngredient ingredient)
    {
        BookmarkStore.toggle(ingredient);
        bookmarked = BookmarkStore.resolve(producible);
        revision++;
    }

    @Override
    public List<BookIngredient> producible()
    {
        return producible;
    }

    @Override
    public List<BookIngredient> bookmarked()
    {
        return bookmarked;
    }

    @Override
    public List<BookIngredient> searchable()
    {
        return searchable;
    }

    @Override
    public boolean unlocked(BookIngredient item)
    {
        return Progression.isUnlocked(item);
    }

    @Override
    public BrowseTab tabOf(BookIngredient item)
    {
        return categories.tabOf(item);
    }

    @Override
    public String unlockKey(BookIngredient item)
    {
        return item.unlockKey();
    }

    @Override
    public String variantGroup(BookIngredient item)
    {
        return Progression.variantGroup(item);
    }

    @Override
    public boolean named(BookIngredient item, String lowerCaseText)
    {
        return item.displayName().toLowerCase(Locale.ROOT).contains(lowerCaseText)
                || SearchAliases.matches(item.registryKey(), lowerCaseText);
    }

    @Override
    public Predicate<BookIngredient> jeiMatches(String text)
    {
        Set<String> found = JeiSearch.matches(text);
        return item -> found.contains(item.sessionUid());
    }

    @Override
    public Set<String> outputsUsing(Set<String> unlockKeys)
    {
        return Progression.outputsUsing(unlockKeys);
    }
}
