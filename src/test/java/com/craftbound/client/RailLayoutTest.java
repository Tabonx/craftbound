package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RailLayoutTest
{
    @Test
    void shortRailsShowEveryTabAndNeverPage()
    {
        assertFalse(RailLayout.paged(RailLayout.MAX_TABS));
        assertEquals(RailLayout.MAX_TABS, RailLayout.visibleTabs(RailLayout.MAX_TABS));
        assertEquals(0, RailLayout.maxOffset(RailLayout.MAX_TABS));
    }

    @Test
    void longRailsPageAndGiveUpARowToTheArrows()
    {
        assertTrue(RailLayout.paged(RailLayout.MAX_TABS + 1));
        assertEquals(RailLayout.PAGED_TABS, RailLayout.visibleTabs(9));
        assertEquals(9 - RailLayout.PAGED_TABS, RailLayout.maxOffset(9));
    }

    @Test
    void offsetStaysWithinRange()
    {
        assertEquals(0, RailLayout.clampOffset(-3, 9));
        assertEquals(RailLayout.maxOffset(9), RailLayout.clampOffset(99, 9));
        assertEquals(0, RailLayout.clampOffset(2, 3));
    }

    @Test
    void scrollingToATabMovesAsLittleAsPossible()
    {
        assertEquals(2, RailLayout.offsetShowing(2, 4, 9));                    // above the window
        assertEquals(9 - RailLayout.PAGED_TABS, RailLayout.offsetShowing(8, 0, 9)); // below the window
        assertEquals(1, RailLayout.offsetShowing(3, 1, 9));                    // already visible
    }
}
