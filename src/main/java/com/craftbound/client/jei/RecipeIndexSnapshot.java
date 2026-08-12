package com.craftbound.client.jei;

import java.util.Map;
import java.util.Optional;

import com.craftbound.progression.RecipeIndex;

import mezz.jei.api.ingredients.ITypedIngredient;

// What one pass over JEI's recipes yields: the progression index, plus one renderable ingredient per
// output key so an unlock can be drawn the same way the book draws it. Reconstructing that from the
// key alone is not possible — a key names a fluid and its subtype, not the stack that produced it.
public record RecipeIndexSnapshot(RecipeIndex index, Map<String, ITypedIngredient<?>> representatives)
{
    public static final RecipeIndexSnapshot EMPTY = new RecipeIndexSnapshot(RecipeIndex.EMPTY, Map.of());

    public Optional<BookIngredient> displayFor(String unlockKey)
    {
        ITypedIngredient<?> typed = representatives.get(unlockKey);
        return typed == null ? Optional.empty() : CraftboundJeiPlugin.toBookIngredient(typed);
    }
}
