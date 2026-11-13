package com.craftbound.client.jei;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.craftbound.progression.RecipeIndex;
import com.craftbound.progression.UnlockKey;

import mezz.jei.api.ingredients.ITypedIngredient;

// Representatives are kept because a key names a fluid and its subtype, not a stack that can be
// drawn. Invisible outputs belong in the grid, since JEI finds recipes by them, but unlock nothing.
public record RecipeIndexSnapshot(RecipeIndex index, Map<UnlockKey, ITypedIngredient<?>> representatives,
        Set<UnlockKey> invisibleOutputs)
{
    public static final RecipeIndexSnapshot EMPTY = new RecipeIndexSnapshot(RecipeIndex.EMPTY, Map.of(), Set.of());

    public List<BookIngredient> producible(List<BookIngredient> ingredients)
    {
        return ingredients.stream().filter(ingredient -> index.makes(ingredient.unlockKey())
                || invisibleOutputs.contains(ingredient.unlockKey())).toList();
    }

    public Optional<BookIngredient> displayFor(UnlockKey unlockKey)
    {
        ITypedIngredient<?> typed = representatives.get(unlockKey);
        return typed == null ? Optional.empty() : BookIngredients.of(typed);
    }
}
