package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SearchAliasesTest
{
    private static final String HOPPER = "item|minecraft:hopper";

    @Test
    void matchesAPrefixOfTheAlias()
    {
        assertTrue(SearchAliases.matches(HOPPER, "tom"));
        assertTrue(SearchAliases.matches(HOPPER, "tom holland"));
    }

    @Test
    void ignoresCase()
    {
        assertTrue(SearchAliases.matches(HOPPER, "Tom Hol"));
    }

    @Test
    void rejectsUnrelatedTermsAndOtherEntries()
    {
        assertFalse(SearchAliases.matches(HOPPER, "spider"));
        assertFalse(SearchAliases.matches("item|minecraft:chest", "tom"));
    }

    @Test
    void declarationsAccumulatePerEntryAndKeyOnKind()
    {
        SearchAliases aliases = SearchAliases.builder()
                .item("create:mechanical_press", "squisher", "stamper")
                .item("create:mechanical_press", "flattener")
                .fluid("lava", "hot juice")
                .build();

        assertTrue(aliases.hasMatch("item|create:mechanical_press", "squish"));
        assertTrue(aliases.hasMatch("item|create:mechanical_press", "flat"));
        assertTrue(aliases.hasMatch("fluid|minecraft:lava", "juice"));
        assertFalse(aliases.hasMatch("item|minecraft:lava", "juice"));
    }
}
