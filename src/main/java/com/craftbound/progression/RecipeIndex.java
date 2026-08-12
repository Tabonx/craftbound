package com.craftbound.progression;

import java.util.HashSet;
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
//
// `producedKeys` is every output anything makes. A fluid outside it — water, say — is not produced
// by any recipe, so requiring it to be "unlocked" would be a gate that can never open; slots
// demanding one are left unjudged instead.
public record RecipeIndex(Map<String, Map<Object, RecipeNode>> byCategory,
        Map<String, Set<ResourceLocation>> catalysts, Set<String> producedKeys)
{
    public static final RecipeIndex EMPTY = new RecipeIndex(Map.of(), Map.of(), Set.of());

    public static RecipeIndex of(Map<String, Map<Object, RecipeNode>> byCategory,
            Map<String, Set<ResourceLocation>> catalysts)
    {
        Set<String> produced = new HashSet<>();
        byCategory.values().forEach(nodes -> nodes.values()
                .forEach(node -> produced.addAll(bootstrappable(node))));
        return new RecipeIndex(Map.copyOf(byCategory), Map.copyOf(catalysts), Set.copyOf(produced));
    }

    // Only what a recipe makes *without already needing it*. Create's brewing both consumes and
    // produces potion fluid, so counting that as "produced" would gate every brewing recipe behind
    // a fluid nothing could ever bootstrap — a cycle with no way in. A fluid reachable only from
    // itself is left unjudged instead, exactly like one nothing produces at all.
    private static Set<String> bootstrappable(RecipeNode node)
    {
        Set<String> consumed = new HashSet<>();
        node.inputSlots().forEach(slot -> consumed.addAll(slot.fluids()));

        Set<String> made = new HashSet<>(node.outputKeys());
        made.removeAll(consumed);
        return made;
    }

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
