package com.craftbound.client.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

class RecipeNavigatorTest
{
    private final List<String> built = new ArrayList<>();

    private List<Supplier<String>> recipes(String... names)
    {
        return List.of(names).stream().<Supplier<String>>map(name -> () ->
        {
            built.add(name);
            return name;
        }).toList();
    }

    private final Map<String, List<Supplier<String>>> focused = Map.of(
            "crafting", recipes("planks"),
            "smelting", recipes("charcoal", "glass"));
    private final Map<String, List<Supplier<String>>> whole = Map.of(
            "crafting", recipes("planks", "stick", "table"),
            "smelting", List.of());

    private RecipeNavigator<String, String> opened(int start)
    {
        RecipeNavigator<String, String> navigator = new RecipeNavigator<>(focused::get, whole::get);
        navigator.open(List.of("crafting", "smelting"), start);
        return navigator;
    }

    @Test
    void opensOnTheChosenGroupAtItsFirstRecipe()
    {
        RecipeNavigator<String, String> navigator = opened(1);
        assertTrue(navigator.isOpen());
        assertEquals(1, navigator.group());
        assertEquals(0, navigator.index());
        assertEquals("charcoal", navigator.current());
    }

    @Test
    void groupsAndRecipesStayInRange()
    {
        RecipeNavigator<String, String> navigator = opened(7);
        assertEquals(1, navigator.group());
        navigator.step(5);
        assertEquals("glass", navigator.current());
        navigator.step(-9);
        assertEquals("charcoal", navigator.current());
        navigator.selectGroup(-3);
        assertEquals("planks", navigator.current());
    }

    @Test
    void recipesAreBuiltOnlyOnceShownAndOnlyOnce()
    {
        RecipeNavigator<String, String> navigator = opened(0);
        navigator.showAll(0);
        assertEquals(List.of(), built);
        navigator.step(2);
        navigator.current();
        navigator.current();
        assertEquals(List.of("table"), built);
        navigator.step(-1);
        navigator.current();
        navigator.step(1);
        navigator.current();
        assertEquals(List.of("table", "stick"), built);
    }

    @Test
    void showingAllOfAGroupWithNothingInItStaysPut()
    {
        RecipeNavigator<String, String> navigator = opened(0);
        navigator.step(0);
        navigator.showAll(1);
        assertEquals(0, navigator.group());
        assertEquals("planks", navigator.current());
        assertEquals(1, navigator.size());
    }

    @Test
    void closingLeavesNothingOpen()
    {
        RecipeNavigator<String, String> navigator = opened(1);
        navigator.close();
        assertFalse(navigator.isOpen());
        assertEquals(0, navigator.size());
    }
}
