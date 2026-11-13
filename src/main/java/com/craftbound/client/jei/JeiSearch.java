package com.craftbound.client.jei;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;

// The book uses JEI's search text as its own, so mods that sync with JEI's search, like Create's
// stock keeper, stay in sync with the book.
public final class JeiSearch
{
    // The book filters again on every tab switch and unlock, often with the same text.
    private static String searchedText;
    private static int searchedVersion = -1;
    private static Set<String> searchedUids = Set.of();

    public static String query()
    {
        return JeiSession.runtime().map(runtime -> runtime.getIngredientFilter().getFilterText()).orElse("");
    }

    public static void setQuery(String text)
    {
        JeiSession.runtime().ifPresent(runtime -> runtime.getIngredientFilter().setFilterText(text));
    }

    // Delegates to JEI's own search so the book understands the same syntax.
    public static Set<String> matches(String text)
    {
        Optional<IJeiRuntime> runtime = JeiSession.runtime();
        if (runtime.isEmpty())
            return Set.of();
        int version = JeiSession.ingredientsVersion();
        if (text.equals(searchedText) && version == searchedVersion)
            return searchedUids;

        IIngredientFilter filter = runtime.get().getIngredientFilter();
        if (!text.equals(filter.getFilterText()))
            filter.setFilterText(text);
        IIngredientManager manager = runtime.get().getIngredientManager();
        Set<String> uids = new HashSet<>();
        for (IIngredientType<?> type : BookIngredients.browsableTypes(runtime.get()))
            collectUids(manager, filter, type, uids);

        searchedText = text;
        searchedVersion = version;
        searchedUids = uids;
        return uids;
    }

    private static <V> void collectUids(IIngredientManager manager, IIngredientFilter filter,
            IIngredientType<V> type, Set<String> out)
    {
        var helper = manager.getIngredientHelper(type);
        for (V ingredient : filter.getFilteredIngredients(type))
            out.add(BookIngredient.sessionUidOf(type, helper, ingredient));
    }

    private JeiSearch() {}
}
