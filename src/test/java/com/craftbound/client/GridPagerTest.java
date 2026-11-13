package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GridPagerTest
{
    private static GridPager pager(int bookHeight, int count)
    {
        GridPager pager = new GridPager();
        pager.fit(bookHeight);
        pager.fill(count, "query");
        return pager;
    }

    @Test
    void vanillaHeightShowsFourRowsOfFive()
    {
        GridPager pager = pager(RecipeBookLayout.BOOK_HEIGHT, 41);
        assertEquals(20, pager.perPage());
        assertEquals(3, pager.pageCount());
    }

    @Test
    void tallerBookShowsARowMoreForEveryRowItGrows()
    {
        assertEquals(30, pager(RecipeBookLayout.BOOK_HEIGHT + 2 * RecipeBookLayout.ROW, 0).perPage());
        assertEquals(20, pager(RecipeBookLayout.BOOK_HEIGHT + RecipeBookLayout.ROW - 1, 0).perPage());
    }

    @Test
    void anEmptyGridStillHasOnePage()
    {
        assertEquals(1, pager(RecipeBookLayout.BOOK_HEIGHT, 0).pageCount());
    }

    @Test
    void stepsStayWithinThePages()
    {
        GridPager pager = pager(RecipeBookLayout.BOOK_HEIGHT, 41);
        pager.step(-1);
        assertEquals(0, pager.page());
        pager.step(5);
        assertEquals(2, pager.page());
        assertEquals(40, pager.first());
        assertEquals(1, pager.shown());
    }

    @Test
    void aNewQueryStartsOnTheFirstPage()
    {
        GridPager pager = pager(RecipeBookLayout.BOOK_HEIGHT, 100);
        pager.step(3);
        pager.fill(100, "other");
        assertEquals(0, pager.page());
    }

    @Test
    void theSameQueryFilledAgainKeepsItsPage()
    {
        GridPager pager = pager(RecipeBookLayout.BOOK_HEIGHT, 100);
        pager.step(3);
        pager.fill(90, "query");
        assertEquals(3, pager.page());
        pager.fill(30, "query");
        assertEquals(1, pager.page());
    }

    @Test
    void growingTheBookKeepsThePageWhereItStillExists()
    {
        GridPager pager = pager(RecipeBookLayout.BOOK_HEIGHT, 100);
        pager.step(4);
        pager.fit(RecipeBookLayout.BOOK_HEIGHT + 4 * RecipeBookLayout.ROW);
        assertEquals(2, pager.page());
    }

    @Test
    void cellsAreFoundByTheirSquareOnlyWhereAnEntryIs()
    {
        GridPager pager = pager(RecipeBookLayout.BOOK_HEIGHT, 7);
        assertEquals(0, pager.cellAt(0, 0));
        assertEquals(4, pager.cellAt(4 * GridPager.CELL + 24.9, 3));
        assertEquals(6, pager.cellAt(GridPager.CELL, GridPager.CELL));
        assertEquals(-1, pager.cellAt(2 * GridPager.CELL, GridPager.CELL));
        assertEquals(-1, pager.cellAt(5 * GridPager.CELL, 0));
        assertEquals(-1, pager.cellAt(-0.5, 0));
    }

    @Test
    void cellsSitInRowsOfFive()
    {
        assertEquals(GridPager.CELL * 2, GridPager.cellX(7));
        assertEquals(GridPager.CELL, GridPager.cellY(7));
    }
}
