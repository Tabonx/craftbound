package com.craftbound.client.book;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import com.craftbound.client.book.search.SearchSyntax;

// What the browse grid is asked to show: a ribbon, the search text, and whether only what can be
// made right now. Two equal queries show the same thing, so the grid keeps its page between them.
public record BrowseQuery(BrowseTab tab, String text, boolean craftableOnly)
{
    // What the grid is filled from.
    public interface Catalog<T>
    {
        List<T> producible();

        List<T> bookmarked();

        // Every ingredient, raw ones included, so a search can name an oak log and find its uses.
        List<T> searchable();

        boolean unlocked(T item);

        BrowseTab tabOf(T item);

        // The variant group an item folds into, or null.
        String variantGroup(T item);

        boolean named(T item, String lowerCaseText);

        Predicate<T> jeiMatches(String text);

        // What the unlocked recipes taking any of these inputs make.
        Predicate<T> madeFrom(List<T> inputs);
    }

    // The grid's entries: one item, or every variant of one behind a single slot. Bookmarks are
    // listed one by one, as they were chosen.
    public <T> List<List<T>> entries(Catalog<T> catalog, Predicate<? super T> craftable)
    {
        boolean bookmarks = tab == BrowseTab.BOOKMARKS;
        List<T> shown = (bookmarks ? catalog.bookmarked() : catalog.producible()).stream()
                .filter(catalog::unlocked)
                .filter(item -> !tab.isItemCategory() || catalog.tabOf(item) == tab)
                .toList();
        List<T> result = text.isBlank() ? shown : searched(catalog, shown);
        if (craftableOnly)
            result = result.stream().filter(craftable).toList();
        return GridEntries.of(result, bookmarks ? item -> null : catalog::variantGroup);
    }

    // Matches first. A plain name search then adds what those matches are used to make. Text with
    // JEI's operators, such as @mod or #tag, goes to JEI's own search.
    private <T> List<T> searched(Catalog<T> catalog, List<T> source)
    {
        boolean plain = SearchSyntax.isPlainName(text);
        String needle = text.toLowerCase(Locale.ROOT);
        Predicate<T> matches = plain ? item -> catalog.named(item, needle) : catalog.jeiMatches(text);
        Predicate<T> made = plain
                ? catalog.madeFrom(catalog.searchable().stream().filter(matches).toList())
                : item -> false;

        List<T> result = new ArrayList<>();
        List<T> uses = new ArrayList<>();
        for (T item : source)
        {
            if (matches.test(item))
                result.add(item);
            else if (made.test(item))
                uses.add(item);
        }
        result.addAll(uses);
        return result;
    }
}
