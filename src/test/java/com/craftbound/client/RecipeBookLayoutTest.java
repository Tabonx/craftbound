package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RecipeBookLayoutTest
{
    private static final int INVENTORY_WIDTH = 176;

    @Test
    void closedBookCentersInventory()
    {
        assertEquals((640 - INVENTORY_WIDTH) / 2,
                RecipeBookLayout.inventoryLeftPos(640, INVENTORY_WIDTH, false));
    }

    @Test
    void openBookCentersTheBrowsingBookAndInventoryAsAPair()
    {
        int leftPos = RecipeBookLayout.inventoryLeftPos(640, INVENTORY_WIDTH, true);
        int bookLeft = RecipeBookLayout.bookRight(leftPos) - RecipeBookLayout.BOOK_WIDTH;
        int rightMargin = 640 - (leftPos + INVENTORY_WIDTH);
        assertTrue(Math.abs(bookLeft - rightMargin) <= 1, "margins " + bookLeft + " and " + rightMargin);
    }

    @Test
    void recipePanelFitsLeftOfTheInventory()
    {
        for (int width : new int[] {200, 427, 480, 640})
        {
            int leftPos = RecipeBookLayout.inventoryLeftPos(width, INVENTORY_WIDTH, true);
            int recipeLeft = RecipeBookLayout.bookRight(leftPos) - RecipeBookLayout.RECIPE_WIDTH;
            assertTrue(recipeLeft >= 0, "recipe panel off screen at width " + width);
        }
    }

    @Test
    void narrowWindowClampsToTheRecipePanel()
    {
        assertEquals(RecipeBookLayout.RECIPE_WIDTH + RecipeBookLayout.GAP,
                RecipeBookLayout.inventoryLeftPos(200, INVENTORY_WIDTH, true));
    }
}
