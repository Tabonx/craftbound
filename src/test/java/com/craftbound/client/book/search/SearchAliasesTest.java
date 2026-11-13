package com.craftbound.client.book.search;

import static com.craftbound.progression.Keys.key;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.craftbound.progression.UnlockKey;

class SearchAliasesTest
{
    private static final UnlockKey HOPPER = key("item|minecraft:hopper");

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
        assertFalse(SearchAliases.matches(key("item|minecraft:chest"), "tom"));
    }

    @Test
    void declarationsAccumulatePerEntryAndKeyOnKind()
    {
        SearchAliases aliases = SearchAliases.builder()
                .item("create:mechanical_press", "squisher", "stamper")
                .item("create:mechanical_press", "flattener")
                .fluid("lava", "hot juice")
                .build();

        assertTrue(aliases.hasMatch(key("item|create:mechanical_press"), "squish"));
        assertTrue(aliases.hasMatch(key("item|create:mechanical_press"), "flat"));
        assertTrue(aliases.hasMatch(key("fluid|minecraft:lava"), "juice"));
        assertFalse(aliases.hasMatch(key("item|minecraft:lava"), "juice"));
    }
}
