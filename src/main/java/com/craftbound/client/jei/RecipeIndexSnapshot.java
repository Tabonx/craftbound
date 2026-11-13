package com.craftbound.client.jei;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.craftbound.progression.RecipeIndex;

import mezz.jei.api.ingredients.ITypedIngredient;

// What one pass over JEI's recipes yields: the progression index, plus one renderable ingredient per
// output key so an unlock can be drawn the same way the book draws it. Reconstructing that from the
// key alone is not possible: a key names a fluid and its subtype, not the stack that produced it.
//
// `invisibleOutputs` are what categories declare a recipe makes without drawing it. JEI finds the
// recipe by them, so they belong in the grid, but progression never reads them.
public record RecipeIndexSnapshot(RecipeIndex index, Map<String, ITypedIngredient<?>> representatives,
        Set<String> invisibleOutputs)
{
    public static final RecipeIndexSnapshot EMPTY = new RecipeIndexSnapshot(RecipeIndex.EMPTY, Map.of(), Set.of());

    // The ingredients some recipe makes, read or not. An item like an oak log is a valid ingredient
    // but nothing produces it, so it would just be dead weight in the browse grid.
    public List<BookIngredient> producible(List<BookIngredient> ingredients)
    {
        return ingredients.stream().filter(ingredient -> index.makes(ingredient.unlockKey())
                || invisibleOutputs.contains(ingredient.unlockKey())).toList();
    }

    public Optional<BookIngredient> displayFor(String unlockKey)
    {
        ITypedIngredient<?> typed = representatives.get(unlockKey);
        return typed == null ? Optional.empty() : BookIngredients.of(typed);
    }
}
