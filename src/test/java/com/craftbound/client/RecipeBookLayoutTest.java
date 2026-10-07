package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RecipeBookLayoutTest
{
    private static final int INVENTORY_WIDTH = 176;
    private static final int STOCK_KEEPER_WIDTH = 226;

    @Test
    void closedBookCentersInventory()
    {
        assertEquals((640 - INVENTORY_WIDTH) / 2,
                RecipeBookLayout.inventoryLeftPos(640, INVENTORY_WIDTH, false));
    }

    @Test
    void openBookCentersTheBrowsingBookAndInventoryAsAPair()
    {
        int leftPos = RecipeBookLayout.inventoryLeftPos(800, INVENTORY_WIDTH, true);
        int bookLeft = RecipeBookLayout.bookRight(leftPos) - RecipeBookLayout.BOOK_WIDTH;
        int rightMargin = 800 - (leftPos + INVENTORY_WIDTH);
        assertTrue(Math.abs(bookLeft - rightMargin) <= 1, "margins " + bookLeft + " and " + rightMargin);
    }

    @Test
    void fullRecipePanelAndRailFitWhereThereIsRoom()
    {
        int leftPos = RecipeBookLayout.inventoryLeftPos(480, INVENTORY_WIDTH, true);
        int bookRight = RecipeBookLayout.bookRight(leftPos);
        assertEquals(RecipeBookLayout.RECIPE_WIDTH, RecipeBookLayout.recipeWidth(bookRight));
        assertTrue(bookRight - RecipeBookLayout.RECIPE_WIDTH - RecipeBookLayout.RAIL_WIDTH >= RecipeBookLayout.EDGE);
    }

    @Test
    void narrowWindowNarrowsTheRecipePanelInsteadOfPushingTheScreenOff()
    {
        for (int width : new int[] {440, 480})
        {
            int leftPos = RecipeBookLayout.besideLeftPos(width, STOCK_KEEPER_WIDTH, true);
            int bookRight = RecipeBookLayout.besideBookRight(leftPos);
            int recipeLeft = bookRight - RecipeBookLayout.recipeWidth(bookRight);
            assertEquals(width - RecipeBookLayout.EDGE, leftPos + STOCK_KEEPER_WIDTH);
            assertEquals(RecipeBookLayout.EDGE, recipeLeft - RecipeBookLayout.RAIL_WIDTH);
        }
    }

    @Test
    void closedBookLeavesAScreenWithoutARecipeBookCentered()
    {
        assertEquals((640 - STOCK_KEEPER_WIDTH) / 2, RecipeBookLayout.besideLeftPos(640, STOCK_KEEPER_WIDTH, false));
    }

    @Test
    void screenCenteringItselfOnTheBesideWidthLandsBesideTheBook()
    {
        for (int width : new int[] {425, 426, 480, 640})
            for (boolean open : new boolean[] {false, true})
                assertEquals(RecipeBookLayout.besideLeftPos(width, INVENTORY_WIDTH, open),
                        (RecipeBookLayout.besideScreenWidth(width, INVENTORY_WIDTH, open) - INVENTORY_WIDTH) / 2);
    }
}
