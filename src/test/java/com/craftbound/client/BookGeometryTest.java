package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BookGeometryTest
{
    private static final int RIGHT = 400;

    @Test
    void browsingPageKeepsVanillaWidthLeftOfItsRightEdge()
    {
        BookGeometry page = BookGeometry.anchored(RIGHT, 20, RecipeBookLayout.BOOK_HEIGHT, false);
        assertEquals(new Rect(RIGHT - BookGeometry.WIDTH, 20, BookGeometry.WIDTH, RecipeBookLayout.BOOK_HEIGHT),
                page.bounds());
    }

    @Test
    void recipePageWidensLeftFromTheSameEdge()
    {
        BookGeometry page = BookGeometry.anchored(RIGHT, 20, RecipeBookLayout.BOOK_HEIGHT, true);
        assertEquals(RecipeBookLayout.RECIPE_WIDTH, page.width());
        assertEquals(RIGHT, page.bounds().right());
    }

    @Test
    void vanillaSpotsOnAVanillaSizedPage()
    {
        BookGeometry page = new BookGeometry(0, 0, BookGeometry.WIDTH, BookGeometry.HEIGHT);
        assertEquals(new Rect(25, 13, 81, 14), page.search());
        assertEquals(new Rect(110, 12, 26, 16), page.filter());
        assertEquals(page.filter(), page.place());
        assertEquals(new Rect(80, 12, 26, 16), page.bookmark(true));
        assertEquals(page.place(), page.bookmark(false));
        assertEquals(new Rect(38, 137, 12, 17), page.backArrow());
        assertEquals(new Rect(93, 137, 12, 17), page.forwardArrow());
        assertEquals(new Rect(8, 30, 131, 103), page.body());
    }

    @Test
    void pagerAndBodyFollowTheBottomEdge()
    {
        BookGeometry page = new BookGeometry(0, 10, BookGeometry.WIDTH, BookGeometry.HEIGHT + 50);
        assertEquals(10 + 137 + 50, page.pagerY());
        assertEquals(103 + 50, page.body().height());
    }

    @Test
    void edgeInsetsStretchWithTheWiderPage()
    {
        BookGeometry page = new BookGeometry(0, 0, 200, BookGeometry.HEIGHT);
        assertEquals(11, page.back().x());
        assertEquals(200 - 15 - 26, page.place().x());
        assertEquals(15, page.carriedX());
        assertEquals(new Rect(11, 10, 189 - 11, 20), page.searchCover());
    }
}
