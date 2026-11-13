package com.craftbound.progression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
// `producedKeys` is every output anything makes. A fluid outside it, water for example, is not produced
// by any recipe, so requiring it to be "unlocked" would be a gate that can never open; slots
// demanding one are left unjudged instead.
//
// `usedBy` maps each input key to the recipes that accept it, so search can find what an item is
// used for without scanning every recipe on each keystroke.
//
// `unjudgedOutputs` is what recipes the index could not read are known to make. Nothing can say
// whether those recipes are unlocked, so their outputs always are: showing a recipe early is
// recoverable, hiding one forever is not.
public record RecipeIndex(Map<String, Map<Object, RecipeNode>> byCategory,
        Map<String, Set<ResourceLocation>> catalysts, Set<UnlockKey> producedKeys,
        Map<UnlockKey, List<RecipeNode>> usedBy, Set<UnlockKey> outputKeys, Set<UnlockKey> unjudgedOutputs)
{
    public static final RecipeIndex EMPTY = new RecipeIndex(Map.of(), Map.of(), Set.of(), Map.of(), Set.of(), Set.of());

    public static RecipeIndex of(Map<String, Map<Object, RecipeNode>> byCategory,
            Map<String, Set<ResourceLocation>> catalysts)
    {
        return of(byCategory, catalysts, Set.of());
    }

    public static RecipeIndex of(Map<String, Map<Object, RecipeNode>> byCategory,
            Map<String, Set<ResourceLocation>> catalysts, Set<UnlockKey> unjudgedOutputs)
    {
        Set<UnlockKey> produced = new HashSet<>();
        Set<UnlockKey> outputs = new HashSet<>(unjudgedOutputs);
        byCategory.values().forEach(nodes -> nodes.values().forEach(node ->
        {
            produced.addAll(bootstrappable(node));
            outputs.addAll(node.outputKeys());
        }));
        return new RecipeIndex(Map.copyOf(byCategory), Map.copyOf(catalysts), Set.copyOf(produced),
                usedBy(byCategory), Set.copyOf(outputs), Set.copyOf(unjudgedOutputs));
    }

    private static Map<UnlockKey, List<RecipeNode>> usedBy(Map<String, Map<Object, RecipeNode>> byCategory)
    {
        Map<UnlockKey, List<RecipeNode>> usedBy = new HashMap<>();
        byCategory.values().forEach(nodes -> nodes.values().forEach(node ->
        {
            Set<UnlockKey> keys = new HashSet<>();
            for (InputSlot slot : node.inputSlots())
            {
                slot.items().forEach(item -> keys.add(UnlockKey.ofItem(item)));
                keys.addAll(slot.fluids());
                keys.addAll(slot.variants());
            }
            keys.forEach(key -> usedBy.computeIfAbsent(key, unused -> new ArrayList<>()).add(node));
        }));
        return usedBy;
    }

    // Only what a recipe makes *without already needing it*. Create's brewing both consumes and
    // produces potion fluid, so counting that as "produced" would gate every brewing recipe behind
    // a fluid nothing could ever bootstrap, a cycle with no way in. A fluid reachable only from
    // itself is left unjudged instead, exactly like one nothing produces at all.
    private static Set<UnlockKey> bootstrappable(RecipeNode node)
    {
        Set<UnlockKey> consumed = new HashSet<>();
        node.inputSlots().forEach(slot -> consumed.addAll(slot.fluids()));

        Set<UnlockKey> made = new HashSet<>(node.outputKeys());
        made.removeAll(consumed);
        return made;
    }

    public boolean isEmpty()
    {
        return byCategory.isEmpty() && unjudgedOutputs.isEmpty();
    }

    // Whether any recipe makes this, read or not.
    public boolean makes(UnlockKey outputKey)
    {
        return outputKeys.contains(outputKey);
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

    public List<RecipeNode> recipesUsing(UnlockKey inputKey)
    {
        return usedBy.getOrDefault(inputKey, List.of());
    }

    public Set<ResourceLocation> catalystsFor(String categoryUid)
    {
        return catalysts.getOrDefault(categoryUid, Set.of());
    }
}
