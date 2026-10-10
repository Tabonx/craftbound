package com.craftbound.progression;

import java.util.List;
import java.util.Set;

// One recipe reduced to what progression needs: which category it belongs to, what each of its
// input slots demands, and what it produces.
//
// `group` is the recipe's vanilla recipe-book group, scoped by category, or empty. Recipes sharing
// one are variants of each other, such as the chest boat in every wood.
public record RecipeNode(String categoryUid, List<InputSlot> inputSlots, Set<String> outputKeys, String group)
{
    public RecipeNode(String categoryUid, List<InputSlot> inputSlots, Set<String> outputKeys)
    {
        this(categoryUid, inputSlots, outputKeys, "");
    }
}
