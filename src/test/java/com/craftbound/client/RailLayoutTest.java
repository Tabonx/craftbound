package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RailLayoutTest
{
    private static final RailLayout RAIL = RailLayout.forHeight(RecipeBookLayout.BOOK_HEIGHT);

    @Test
    void vanillaHeightFitsSixTabsOrFiveBetweenTheArrows()
    {
        assertEquals(new RailLayout(6, 5), RAIL);
    }

    @Test
    void tallerBookFitsMoreTabs()
    {
        RailLayout tall = RailLayout.forHeight(RecipeBookLayout.BOOK_HEIGHT + 4 * RecipeBookLayout.ROW);
        assertTrue(tall.maxTabs() > RAIL.maxTabs());
        assertTrue(tall.pagedTabs() > RAIL.pagedTabs());
    }

    @Test
    void shortRailsShowEveryTabAndNeverPage()
    {
        assertFalse(RAIL.paged(RAIL.maxTabs()));
        assertEquals(RAIL.maxTabs(), RAIL.visibleTabs(RAIL.maxTabs()));
        assertEquals(0, RAIL.maxOffset(RAIL.maxTabs()));
    }

    @Test
    void longRailsPageAndGiveUpARowToTheArrows()
    {
        assertTrue(RAIL.paged(RAIL.maxTabs() + 1));
        assertEquals(RAIL.pagedTabs(), RAIL.visibleTabs(9));
        assertEquals(9 - RAIL.pagedTabs(), RAIL.maxOffset(9));
    }

    @Test
    void offsetStaysWithinRange()
    {
        assertEquals(0, RAIL.clampOffset(-3, 9));
        assertEquals(RAIL.maxOffset(9), RAIL.clampOffset(99, 9));
        assertEquals(0, RAIL.clampOffset(2, 3));
    }

    @Test
    void scrollingToATabMovesAsLittleAsPossible()
    {
        assertEquals(2, RAIL.offsetShowing(2, 4, 9));                    // above the window
        assertEquals(9 - RAIL.pagedTabs(), RAIL.offsetShowing(8, 0, 9)); // below the window
        assertEquals(1, RAIL.offsetShowing(3, 1, 9));                    // already visible
    }
}
