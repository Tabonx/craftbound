package com.craftbound.client.book;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class CraftableFirstTest
{
    private static List<String> rank(Map<String, List<List<String>>> recipes, Set<String> held)
    {
        return CraftableFirst.sort(List.copyOf(recipes.keySet()), recipes::get, held);
    }

    @Test
    void craftableRecipesMoveAheadKeepingTheirOrder()
    {
        var recipes = new LinkedHashMap<String, List<List<String>>>();
        recipes.put("gold", List.of(List.of("gold_ingot")));
        recipes.put("iron", List.of(List.of("iron_ingot")));
        recipes.put("copper", List.of(List.of("copper_ingot")));
        recipes.put("iron_again", List.of(List.of("iron_ingot"), List.of("stick")));

        assertEquals(List.of("iron", "iron_again", "gold", "copper"),
                rank(recipes, Set.of("iron_ingot", "stick")));
    }

    @Test
    void anyAlternativeSatisfiesASlot()
    {
        var recipes = new LinkedHashMap<String, List<List<String>>>();
        recipes.put("stone", List.of(List.of("cobblestone")));
        recipes.put("planks", List.of(List.of("oak_log", "birch_log")));

        assertEquals(List.of("planks", "stone"), rank(recipes, Set.of("birch_log")));
    }

    @Test
    void aSlotWithNoItemsDoesNotHoldARecipeBack()
    {
        var recipes = new LinkedHashMap<String, List<List<String>>>();
        recipes.put("stone", List.of(List.of("cobblestone")));
        recipes.put("mixed", List.of(List.of(), List.of("sugar")));

        assertEquals(List.of("mixed", "stone"), rank(recipes, Set.of("sugar")));
    }

    @Test
    void nothingHeldKeepsJeiOrder()
    {
        var recipes = new LinkedHashMap<String, List<List<String>>>();
        recipes.put("b", List.of(List.of("x")));
        recipes.put("a", List.of());

        assertEquals(List.of("b", "a"), rank(recipes, Set.of()));
    }
}
