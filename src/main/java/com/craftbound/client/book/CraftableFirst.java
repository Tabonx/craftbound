package com.craftbound.client.book;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

// Counts are ignored on purpose: this ranks recipes, it does not promise the craft will fit. A slot
// with no alternatives (a fluid, say) cannot be judged, so it does not count against the recipe.
final class CraftableFirst
{
    static <R, I> List<R> sort(List<R> recipes, Function<R, List<List<I>>> slots, Set<I> held)
    {
        if (held.isEmpty())
            return recipes;
        return recipes.stream()
                .sorted(Comparator.comparingInt(recipe -> hasEveryInput(slots.apply(recipe), held) ? 0 : 1))
                .toList();
    }

    private static <I> boolean hasEveryInput(List<List<I>> slots, Set<I> held)
    {
        return slots.stream().allMatch(options -> options.isEmpty() || options.stream().anyMatch(held::contains));
    }

    private CraftableFirst() {}
}
