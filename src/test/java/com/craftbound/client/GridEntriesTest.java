package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class GridEntriesTest
{
    private static final Map<String, String> GROUPS = Map.of(
            "oak_boat", "boat", "birch_boat", "boat");

    @Test
    void variantsShareTheSlotOfTheFirst()
    {
        assertEquals(List.of(List.of("oak_boat", "birch_boat"), List.of("stick")),
                GridEntries.of(List.of("oak_boat", "stick", "birch_boat"), GROUPS::get));
    }

    // A search for one wood keeps that boat on its own.
    @Test
    void aLoneVariantIsAnEntryOfItsOwn()
    {
        assertEquals(List.of(List.of("birch_boat")), GridEntries.of(List.of("birch_boat"), GROUPS::get));
    }

    @Test
    void itemsWithoutAGroupNeverMerge()
    {
        assertEquals(List.of(List.of("stick"), List.of("stick")),
                GridEntries.of(List.of("stick", "stick"), GROUPS::get));
    }
}
