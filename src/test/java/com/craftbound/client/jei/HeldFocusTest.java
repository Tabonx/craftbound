package com.craftbound.client.jei;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class HeldFocusTest
{
    @Test
    void aSlotShowsOnlyTheAlternativesHeld()
    {
        HeldFocus.Narrowing<String> narrowing = HeldFocus.of(List.of(List.of("oak", "birch", "spruce")),
                Set.of("birch")::contains);

        assertTrue(narrowing.narrowed());
        assertEquals(List.of("birch"), narrowing.focuses());
    }

    // Hiding every alternative would leave a recipe that does not say what it needs.
    @Test
    void aSlotWithNoneHeldKeepsAll()
    {
        HeldFocus.Narrowing<String> narrowing = HeldFocus.of(List.of(List.of("breeze_rod", "blaze_rod")),
                Set.<String>of()::contains);

        assertFalse(narrowing.narrowed());
        assertEquals(List.of(), narrowing.focuses());
    }

    @Test
    void holdingEveryAlternativeHidesNothing()
    {
        HeldFocus.Narrowing<String> narrowing = HeldFocus.of(List.of(List.of("oak", "birch")),
                Set.of("oak", "birch")::contains);

        assertFalse(narrowing.narrowed());
    }

    @Test
    void oneNarrowedSlotKeepsTheHeldOfTheOthersToo()
    {
        HeldFocus.Narrowing<String> narrowing = HeldFocus.of(
                List.of(List.of("stick"), List.of("oak", "birch")),
                Set.of("stick", "oak")::contains);

        assertTrue(narrowing.narrowed());
        assertEquals(List.of("stick", "oak"), narrowing.focuses());
    }
}
