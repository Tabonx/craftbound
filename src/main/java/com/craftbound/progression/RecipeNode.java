package com.craftbound.progression;

import java.util.List;
import java.util.Set;

// One recipe reduced to what progression needs: which category it belongs to, what each of its
// input slots demands, and what it produces.
public record RecipeNode(String categoryUid, List<InputSlot> inputSlots, Set<String> outputKeys)
{
}
