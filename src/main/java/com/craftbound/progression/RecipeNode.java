package com.craftbound.progression;

import java.util.List;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

// One recipe reduced to what progression needs: which category it belongs to, which items satisfy
// each of its input slots, and what it produces.
//
// A slot is a set of alternatives — any one of them satisfies it, which is what makes a "any
// planks" slot behave the way a player expects. An empty set is a slot with no item alternatives
// (a fluid, or an ingredient type we cannot judge); it never locks a recipe, the same convention
// RecipeOrder uses when ranking craftable recipes first.
public record RecipeNode(String categoryUid, List<Set<ResourceLocation>> inputSlots,
        Set<String> outputKeys)
{
}
