package com.craftbound.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

// Folds the browse grid's items into entries, one per group of variants, as vanilla's book shows
// every chest boat behind one button. An entry sits where its first member would have, and an item
// without a group, or whose variants were all filtered away, stays an entry of its own.
public final class GridEntries
{
    public static <T> List<List<T>> of(List<T> items, Function<T, String> groupOf)
    {
        Map<Object, List<T>> entries = new LinkedHashMap<>();
        for (T item : items)
        {
            String group = groupOf.apply(item);
            Object key = group == null ? new Object() : group;
            entries.computeIfAbsent(key, unused -> new ArrayList<>()).add(item);
        }
        return List.copyOf(entries.values());
    }

    private GridEntries() {}
}
