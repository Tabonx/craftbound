package com.craftbound.client.book;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

class CraftableTrackerTest
{
    private Set<String> craftable = Set.of("stick");
    private int version = 0;
    private int reads = 0;

    private CraftableTracker<String> bound()
    {
        CraftableTracker<String> tracker = new CraftableTracker<>();
        tracker.bind(() ->
        {
            reads++;
            return craftable;
        }, () -> version);
        return tracker;
    }

    @Test
    void anUnboundTrackerHasNothingToFilterAgainst()
    {
        CraftableTracker<String> tracker = new CraftableTracker<>();
        assertFalse(tracker.bound());
        assertFalse(tracker.refresh());
        assertFalse(tracker.contains("stick"));
    }

    @Test
    void readsAgainOnlyWhenTheVersionMoves()
    {
        CraftableTracker<String> tracker = bound();
        assertTrue(tracker.refresh());
        assertTrue(tracker.contains("stick"));
        craftable = Set.of("planks");
        assertFalse(tracker.refresh());
        assertTrue(tracker.contains("stick"));
        version++;
        assertTrue(tracker.refresh());
        assertTrue(tracker.contains("planks"));
    }

    @Test
    void anUnchangedSetIsNoChange()
    {
        CraftableTracker<String> tracker = bound();
        tracker.refresh();
        version++;
        assertFalse(tracker.refresh());
        assertTrue(reads == 2);
    }

    @Test
    void reloadReadsAtOnceAndCatchesUpWithTheVersion()
    {
        CraftableTracker<String> tracker = bound();
        version = 4;
        craftable = Set.of("table");
        tracker.reload();
        assertTrue(tracker.contains("table"));
        assertFalse(tracker.refresh());
    }
}
