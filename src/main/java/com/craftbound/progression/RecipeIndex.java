package com.craftbound.progression;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.resources.ResourceLocation;

// Every recipe in the game reduced to RecipeNodes, plus each category's catalysts (its
// workstations) so a category can be gated on owning the machine.
//
// Nodes are grouped by category first and only then keyed by the recipe object, because one recipe
// object belongs to several categories: Create re-lists ordinary shaped recipes under its own
// Automatic Shaped Crafting. A single recipe-keyed map would let the mechanical crafter's entry
// overwrite the crafting table's and lock the recipe behind a machine the player cannot have yet.
public record RecipeIndex(Map<String, Map<Object, RecipeNode>> byCategory,
        Map<String, Set<ResourceLocation>> catalysts)
{
    public static final RecipeIndex EMPTY = new RecipeIndex(Map.of(), Map.of());

    public boolean isEmpty()
    {
        return byCategory.isEmpty();
    }

    // Null when the recipe was never indexed, which callers read as "not gated".
    public RecipeNode node(String categoryUid, Object recipe)
    {
        return byCategory.getOrDefault(categoryUid, Map.of()).get(recipe);
    }

    public Stream<RecipeNode> nodes()
    {
        return byCategory.values().stream().flatMap(nodes -> nodes.values().stream());
    }

    public Set<ResourceLocation> catalystsFor(String categoryUid)
    {
        return catalysts.getOrDefault(categoryUid, Set.of());
    }
}
