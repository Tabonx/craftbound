package com.craftbound.client;

import static com.craftbound.client.RecipeBookLayout.BOOK_HEIGHT;
import static com.craftbound.client.RecipeBookLayout.EDGE;
import static com.craftbound.client.RecipeBookLayout.GAP;
import static com.craftbound.client.RecipeBookLayout.RAIL_WIDTH;
import static com.craftbound.client.RecipeBookLayout.ROW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.craftbound.client.RecipeBookLayout.Placement;

class RecipeBookLayoutTest
{
    private static final int INVENTORY_WIDTH = 176;
    private static final int STOCK_KEEPER_WIDTH = 226;

    @Test
    void closedBookCentersTheScreen()
    {
        assertEquals((640 - INVENTORY_WIDTH) / 2, RecipeBookLayout.leftPos(640, INVENTORY_WIDTH, GAP, false));
    }

    @Test
    void openBookCentersTheBrowsingBookAndInventoryAsAPair()
    {
        int leftPos = RecipeBookLayout.leftPos(800, INVENTORY_WIDTH, GAP, true);
        Placement placement = place(800, 400, leftPos, INVENTORY_WIDTH, 166, GAP);
        int bookLeft = placement.bookRight() - RecipeBookLayout.BOOK_WIDTH;
        int rightMargin = 800 - (leftPos + INVENTORY_WIDTH);
        assertTrue(Math.abs(bookLeft - rightMargin) <= 1, "margins " + bookLeft + " and " + rightMargin);
    }

    @Test
    void fullRecipePanelAndRailFitWhereThereIsRoom()
    {
        int leftPos = RecipeBookLayout.leftPos(480, INVENTORY_WIDTH, GAP, true);
        int bookRight = place(480, 300, leftPos, INVENTORY_WIDTH, 166, GAP).bookRight();
        assertEquals(RecipeBookLayout.RECIPE_WIDTH, RecipeBookLayout.recipeWidth(bookRight));
        assertTrue(bookRight - RecipeBookLayout.RECIPE_WIDTH - RAIL_WIDTH >= EDGE);
    }

    @Test
    void narrowWindowNarrowsTheRecipePanelInsteadOfPushingTheScreenOff()
    {
        for (int width : new int[] {430, 450})
        {
            int leftPos = RecipeBookLayout.leftPos(width, STOCK_KEEPER_WIDTH, GAP, true);
            int bookRight = place(width, 300, leftPos, STOCK_KEEPER_WIDTH, 245, GAP).bookRight();
            int recipeLeft = bookRight - RecipeBookLayout.recipeWidth(bookRight);
            assertEquals(width - EDGE, leftPos + STOCK_KEEPER_WIDTH);
            assertEquals(EDGE, recipeLeft - RAIL_WIDTH);
        }
    }

    // A 2880 px window at gui scale 7 is 411 wide, which the stock keeper and the book do not share.
    @Test
    void windowTooNarrowForBothPutsTheBookOverTheCenteredScreen()
    {
        assertFalse(RecipeBookLayout.fits(411, STOCK_KEEPER_WIDTH, GAP));
        assertEquals((411 - STOCK_KEEPER_WIDTH) / 2, RecipeBookLayout.leftPos(411, STOCK_KEEPER_WIDTH, GAP, true));

        Placement placement = place(411, 255, (411 - STOCK_KEEPER_WIDTH) / 2, STOCK_KEEPER_WIDTH, 245, GAP);
        assertTrue(placement.overlay());
        int railLeft = placement.bookRight() - RecipeBookLayout.BOOK_WIDTH - RAIL_WIDTH;
        assertTrue(railLeft >= 0 && placement.bookRight() <= 411);
    }

    @Test
    void whatTheScreenDrawsLeftOfItselfCountsAgainstTheRoom()
    {
        assertTrue(RecipeBookLayout.fits(400, INVENTORY_WIDTH, GAP));
        assertFalse(RecipeBookLayout.fits(400, INVENTORY_WIDTH, GAP + 40));
    }

    @Test
    void closedBookLeavesTheScreenWhereItCentersItself()
    {
        for (int width : new int[] {411, 425, 480, 640})
            for (boolean open : new boolean[] {false, true})
                assertEquals(RecipeBookLayout.leftPos(width, INVENTORY_WIDTH, GAP, open),
                        (RecipeBookLayout.screenWidthFor(width, INVENTORY_WIDTH, GAP, open) - INVENTORY_WIDTH) / 2);
    }

    @Test
    void bookMatchesATallerScreen()
    {
        assertEquals(BOOK_HEIGHT, RecipeBookLayout.bookHeight(300, 133, Integer.MAX_VALUE));
        assertEquals(245, RecipeBookLayout.bookHeight(600, 245, Integer.MAX_VALUE));
    }

    @Test
    void bookStaysInsideTheWindowAndAboveWhatTheScreenKeepsBelowIt()
    {
        assertEquals(250 - 2 * EDGE, RecipeBookLayout.bookHeight(250, 400, Integer.MAX_VALUE));
        assertEquals(BOOK_HEIGHT + ROW, RecipeBookLayout.bookHeight(600, 400, BOOK_HEIGHT + ROW));
        assertEquals(BOOK_HEIGHT, RecipeBookLayout.bookHeight(600, 400, 100));
    }

    @Test
    void bookTopFollowsTheScreenButNeverLeavesTheWindow()
    {
        assertEquals(40, RecipeBookLayout.bookTop(300, 40, BOOK_HEIGHT));
        assertEquals(0, RecipeBookLayout.bookTop(300, -20, BOOK_HEIGHT));
        assertEquals(200 - EDGE - BOOK_HEIGHT, RecipeBookLayout.bookTop(200, 60, BOOK_HEIGHT));
    }

    private static Placement place(int width, int height, int leftPos, int imageWidth, int imageHeight, int gap)
    {
        return RecipeBookLayout.place(width, height, leftPos, (height - imageHeight) / 2, imageWidth, imageHeight,
                gap, Integer.MAX_VALUE);
    }
}
